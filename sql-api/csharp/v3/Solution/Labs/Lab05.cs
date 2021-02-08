using System;
using System.Threading.Tasks;
using Microsoft.Azure.Cosmos;
using Solution.Entities;
using Solution.Entities.Lab05;


namespace Solution.Labs
{
    public class Lab05
    {
        /// <summary>
        ///     Constructor
        /// </summary>
        /// <param name="configuration">
        ///     Configuration object
        /// </param>
        public Lab05(IConfiguration _configuration)
        {
            string cosmosDBURI = _configuration.CosmosDB.URI;
            string cosmosDBPrimaryKey = _configuration.CosmosDB.PrimaryKey;
            string cosmosDBConnectionString = _configuration.CosmosDB.PrimaryConnectionString;
            CosmosClient cosmosDBClient = new CosmosClient(cosmosDBURI, cosmosDBPrimaryKey);

            string databaseId = "NutritionDatabase";
            Database nutritionDatabase = cosmosDBClient.GetDatabase(databaseId);
            string containerId = "FoodCollection";
            Container foodContainer = cosmosDBClient.GetContainer(databaseId, containerId);

            // Point Read
            DoPointRead(foodContainer).GetAwaiter().GetResult();

            // SQL Query
            DoSQLQuery(foodContainer).GetAwaiter().GetResult();
        }

        /// <summary>
        ///     Do a point read
        /// </summary>
        /// <param name="container">
        ///     Container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task DoPointRead(Container container)
        {
            ItemResponse<Food> candyResponse = await container.ReadItemAsync<Food>("19130", new PartitionKey("Sweets"));
            Food candy = candyResponse.Resource;
            Console.Out.WriteLine($"Read {candy.Description}");
        }

        /// <summary>
        ///     Do a SQL query
        /// </summary>
        /// <param name="container">
        ///     Container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task DoSQLQuery(Container container)
        {
            string sqlA = "SELECT f.description, f.manufacturerName, f.servings FROM foods f WHERE f.foodGroup = 'Sweets' and IS_DEFINED(f.description) and IS_DEFINED(f.manufacturerName) and IS_DEFINED(f.servings)";
            FeedIterator<Food> queryA = container.GetItemQueryIterator<Food>(new QueryDefinition(sqlA), requestOptions: new QueryRequestOptions { MaxConcurrency = 1 });
            foreach (Food food in await queryA.ReadNextAsync())
            {
                await Console.Out.WriteLineAsync($"{food.Description} by {food.ManufacturerName}");
                foreach (Serving serving in food.Servings)
                {
                    await Console.Out.WriteLineAsync($"\t{serving.Amount} {serving.Description}");
                }
                await Console.Out.WriteLineAsync();
            }

            string sqlB = @"SELECT f.id, f.description, f.manufacturerName, f.servings FROM foods f WHERE IS_DEFINED(f.manufacturerName)";
            FeedIterator<Food> queryB = container.GetItemQueryIterator<Food>(sqlB, requestOptions: new QueryRequestOptions { MaxConcurrency = 5, MaxItemCount = 100 });
            int pageCount = 0;
            while (queryB.HasMoreResults)
            {
                Console.Out.WriteLine($"---Page #{++pageCount:0000}---");
                foreach (var food in await queryB.ReadNextAsync())
                {
                    Console.Out.WriteLine($"\t[{food.Id}]\t{food.Description,-20}\t{food.ManufacturerName,-40}");
                }
            }
        }
    }
}