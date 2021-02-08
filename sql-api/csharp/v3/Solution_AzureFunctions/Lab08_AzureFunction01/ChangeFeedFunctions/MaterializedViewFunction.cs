using System.Collections.Generic;
using System.Threading.Tasks;
using System.Linq;
using Newtonsoft.Json;
using Microsoft.Azure.Cosmos;
using ChangeFeedFunctions.Entities;
using Microsoft.Azure.Documents;
using Microsoft.Azure.WebJobs;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace ChangeFeedFunctions
{
    public class MaterializedViewFunction
    {
        private readonly Settings Settings;
        private CosmosClient CosmosClientTarget;

        public MaterializedViewFunction(
            IOptions<Settings> options,
            CosmosClient _cosmosClient)
        {
            Settings = options.Value;
            CosmosClientTarget = _cosmosClient;
        }


        [FunctionName("MaterializedViewFunction")]
        public async Task Run([CosmosDBTrigger(
            databaseName: "StoreDatabase",
            collectionName: "CartContainerByState",
            ConnectionStringSetting = "CosmosDB:SourceCosmosDBAccountConnectionString",
            CreateLeaseCollectionIfNotExists = true,
            LeaseCollectionName = "materializedViewLeases")]
            IReadOnlyList<Document> input, ILogger log, ExecutionContext context)
        {
            if (input != null && input.Count > 0)
            {
                Dictionary<string, List<double>> stateDict = CreateStateDictionary(input);
                await PrepareAndUpsertItem(log, stateDict);
            }
        }

        /// <summary>
        ///     Create state dictionary
        /// </summary>
        /// <param name="input">
        ///     The input documents
        /// </param>
        /// <returns>
        ///     Returns the state dictionary
        /// </returns>
        private Dictionary<string, List<double>> CreateStateDictionary(IReadOnlyList<Document> input)
        {
            Dictionary<string, List<double>> stateDict = new Dictionary<string, List<double>>();
            foreach (Document doc in input)
            {
                var action = JsonConvert.DeserializeObject<CartAction>(doc.ToString());

                if (action.Action != ActionType.Purchased)
                {
                    continue;
                }

                if (stateDict.ContainsKey(action.BuyerState))
                {
                    stateDict[action.BuyerState].Add(action.Price);
                }
                else
                {
                    stateDict.Add(action.BuyerState, new List<double> { action.Price });
                }
            }

            return stateDict;
        }

        /// <summary>
        ///     Prepare and upsert items
        /// </summary>
        /// <param name="log">
        ///     The ILogger incstance
        /// </param>
        /// <param name="stateDict">
        ///     The state dictionary
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task PrepareAndUpsertItem(
            ILogger log,
            Dictionary<string, List<double>> stateDict)
        {
            Microsoft.Azure.Cosmos.Database database = CosmosClientTarget.GetDatabase(Settings.TargetCosmosDBAccountDatabaseId);
            Microsoft.Azure.Cosmos.Container container = database.GetContainer(Settings.TargetCosmosDBAccountContainerId);

            var tasks = new List<Task>();

            foreach (var key in stateDict.Keys)
            {
                var query = new QueryDefinition("select * from StateSales s where s.State = @state").WithParameter("@state", key);

                var resultSet = container.GetItemQueryIterator<StateCount>(query, requestOptions: new QueryRequestOptions() { PartitionKey = new Microsoft.Azure.Cosmos.PartitionKey(key), MaxItemCount = 1 });

                while (resultSet.HasMoreResults)
                {
                    var stateCount = (await resultSet.ReadNextAsync()).FirstOrDefault();

                    if (stateCount == null)
                    {
                        stateCount = new StateCount();
                        stateCount.State = key;
                        stateCount.TotalSales = stateDict[key].Sum();
                        stateCount.Count = stateDict[key].Count;
                    }
                    else
                    {
                        stateCount.TotalSales += stateDict[key].Sum();
                        stateCount.Count += stateDict[key].Count;
                    }

                    log.LogInformation("Upserting materialized view document...");
                    tasks.Add(container.UpsertItemAsync(stateCount, new Microsoft.Azure.Cosmos.PartitionKey(stateCount.State)));
                }
            }

            await Task.WhenAll(tasks);
        }
    }
}
