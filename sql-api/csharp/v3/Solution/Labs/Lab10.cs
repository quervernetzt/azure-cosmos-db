using System;
using System.Threading.Tasks;
using Microsoft.Azure.Cosmos;
using Solution.Entities;
using Solution.Entities.Lab10;

namespace Solution.Labs
{
    public class Lab10
    {
        /// <summary>
        ///     Constructor
        /// </summary>
        /// <param name="configuration">
        ///     Configuration object
        /// </param>
        public Lab10(IConfiguration _configuration)
        {
            string cosmosDBURI = _configuration.CosmosDB.URI;
            string cosmosDBPrimaryKey = _configuration.CosmosDB.PrimaryKey;
            string cosmosDBConnectionString = _configuration.CosmosDB.PrimaryConnectionString;
            CosmosClient cosmosDBClient = new CosmosClient(cosmosDBURI, cosmosDBPrimaryKey);

            string databaseId = "NutritionDatabase";
            Database nutritionDatabase = cosmosDBClient.GetDatabase(databaseId);
            string containerId = "FoodCollection";
            Container foodContainer = cosmosDBClient.GetContainer(databaseId, containerId);

            TestETag(foodContainer).GetAwaiter().GetResult();
        }

        /// <summary>
        ///     Test ETag
        /// </summary>
        /// <param name="container">
        ///     Container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task TestETag(Container container)
        {
            ItemResponse<Food> response = await container.ReadItemAsync<Food>("21083", new PartitionKey("Fast Foods"));
            await Console.Out.WriteLineAsync($"Existing ETag:\t{response.ETag}");

            ItemRequestOptions requestOptions = new ItemRequestOptions { IfMatchEtag = response.ETag };

            response.Resource.tags.Add(new Tag { name = "Demo" });
            response = await container.UpsertItemAsync(response.Resource, requestOptions: requestOptions);
            await Console.Out.WriteLineAsync($"New ETag:\t{response.ETag}");

            response.Resource.tags.Add(new Tag { name = "Failure" });
            try
            {
                response = await container.UpsertItemAsync(response.Resource, requestOptions: requestOptions);
            }
            catch (Exception ex)
            {
                await Console.Out.WriteLineAsync($"Update error:\t{ex.Message}");
            }
        }
    }
}
