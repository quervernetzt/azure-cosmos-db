using System;
using System.Linq;
using System.Threading.Tasks;
using System.Threading;
using System.Collections.Generic;
using Microsoft.Azure.Cosmos;
using Microsoft.Azure.Cosmos.Scripts;
using Solution.Entities;
using Solution.Entities.Lab07;


namespace Solution.Labs
{
    public class Lab07
    {
        private readonly string StoredProcedureBulkUpload = @"
function bulkUpload(docs) {
    var container = getContext().getCollection();
    var containerLink = container.getSelfLink();
    var count = 0;
    if (!docs) throw new Error('The array is undefined or null.');
    var docsLength = docs.length;
    if (docsLength == 0) {
        getContext()
            .getResponse()
            .setBody(0);
        return;
    }
    tryCreate(docs[count], callback);
    function tryCreate(doc, callback) {
        var isAccepted = container.createDocument(containerLink, doc, callback);
        if (!isAccepted)
            getContext()
                .getResponse()
                .setBody(count);
    }
    function callback(err, doc, options) {
        if (err) throw err;
        count++;
        if (count >= docsLength) {
            getContext()
                .getResponse()
                .setBody(count);
        } else {
            tryCreate(docs[count], callback);
        }
    }
}
        ";

        private readonly string StoredProcedureBulkDelete = @"
function bulkDelete(query) {
    var container = getContext().getCollection();
    var containerLink = container.getSelfLink();
    var response = getContext().getResponse();
    var responseBody = {
        deleted: 0,
        continuation: true
    };
    if (!query) throw new Error('The query is undefined or null.');
    tryQueryAndDelete();
    function tryQueryAndDelete(continuation) {
        var requestOptions = { continuation: continuation };
        var isAccepted = container.queryDocuments(
            containerLink,
            query,
            requestOptions,
            function (err, retrievedDocs, responseOptions) {
                if (err) throw err;
                if (retrievedDocs.length > 0) {
                    tryDelete(retrievedDocs);
                } else if (responseOptions.continuation) {
                    tryQueryAndDelete(responseOptions.continuation);
                } else {
                    responseBody.continuation = false;
                    response.setBody(responseBody);
                }
            }
        );
        if (!isAccepted) {
            response.setBody(responseBody);
        }
    }
    function tryDelete(documents) {
        if (documents.length > 0) {
            var isAccepted = container.deleteDocument(
                documents[0]._self,
                {},
                function (err, responseOptions) {
                    if (err) throw err;
                    responseBody.deleted++;
                    documents.shift();
                    tryDelete(documents);
                }
            );
            if (!isAccepted) {
                response.setBody(responseBody);
            }
        } else {
            tryQueryAndDelete();
        }
    }
}
        ";

        /// <summary>
        ///     Constructor
        /// </summary>
        /// <param name="configuration">
        ///     Configuration object
        /// </param>
        public Lab07(IConfiguration _configuration)
        {
            string cosmosDBURI = _configuration.CosmosDB.URI;
            string cosmosDBPrimaryKey = _configuration.CosmosDB.PrimaryKey;
            string cosmosDBConnectionString = _configuration.CosmosDB.PrimaryConnectionString;
            CosmosClient cosmosDBClient = new CosmosClient(cosmosDBURI, cosmosDBPrimaryKey);

            string databaseId = "NutritionDatabase";
            Database nutritionDatabase = cosmosDBClient.GetDatabase(databaseId);
            string containerId = "FoodCollection";
            Container foodContainer = cosmosDBClient.GetContainer(databaseId, containerId);

            // Register Stored Procedures
            UpsertStoredProcedure(foodContainer, "bulkUpload", StoredProcedureBulkUpload).GetAwaiter().GetResult();
            UpsertStoredProcedure(foodContainer, "bulkDelete", StoredProcedureBulkDelete).GetAwaiter().GetResult();
            Thread.Sleep(10000);

            // Execute Stored Procedures
            BulkUpload(foodContainer).GetAwaiter().GetResult();
            BulkDelete(foodContainer).GetAwaiter().GetResult();
        }

        /// <summary>
        ///     Upsert a Stored Procedure
        /// </summary>
        /// <param name="container">
        ///     Container Reference
        /// </param>
        /// <param name="storedProcedureId">
        ///     The id of the Stored Procedure
        /// </param>
        /// <param name="storedProcedureBody">
        ///     The body of the Stored Procedure
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task UpsertStoredProcedure(
            Container container,
            string storedProcedureId,
            string storedProcedureBody)
        {
            Console.WriteLine($"Processing Stored Procedure '{storedProcedureId}'...");

            StoredProcedureProperties bulkUploadProperties = new StoredProcedureProperties
            {
                Id = storedProcedureId,
                Body = storedProcedureBody
            };

            try
            {
                StoredProcedureResponse storedProcedureResponse = await container.Scripts.CreateStoredProcedureAsync(bulkUploadProperties);
                Console.WriteLine($"Stored Procedure Response Status Code for creation: {storedProcedureResponse.StatusCode}...");
            }
            catch
            {
                StoredProcedureResponse storedProcedureResponse = await container.Scripts.ReplaceStoredProcedureAsync(bulkUploadProperties);
                Console.WriteLine($"Stored Procedure Response Status Code for replacement: {storedProcedureResponse.StatusCode}...");
            }
        }

        /// <summary>
        ///     Bulk upload documents
        /// </summary>
        /// <param name="container">
        ///     Container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private static async Task BulkUpload(Container container)
        {
            List<Food> foods = new Bogus.Faker<Food>()
                .RuleFor(p => p.Id, f => (-1 - f.IndexGlobal).ToString())
                .RuleFor(p => p.Description, f => f.Commerce.ProductName())
                .RuleFor(p => p.ManufacturerName, f => f.Company.CompanyName())
                .RuleFor(p => p.FoodGroup, f => "Energy Bars")
                .Generate(10000);

            int pointer = 0;

            while (pointer < foods.Count)
            {
                StoredProcedureExecuteResponse<int> result = await container.Scripts.ExecuteStoredProcedureAsync<int>(
                    "bulkUpload",
                    new PartitionKey("Energy Bars"),
                    new dynamic[] { foods.Skip(pointer).Take(1000) });

                pointer += result.Resource;

                await Console.Out.WriteLineAsync($"{pointer} Total Items\t{result.Resource} Items Uploaded in this Iteration...");
            }
        }

        /// <summary>
        ///     Bulk delete documents
        /// </summary>
        /// <param name="container">
        ///     Container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private static async Task BulkDelete(Container container)
        {
            bool resume = true;
            do
            {
                string query = "SELECT * FROM foods f WHERE f.foodGroup = 'Energy Bars'";
                StoredProcedureExecuteResponse<DeleteStatus> result = await container.Scripts.ExecuteStoredProcedureAsync<DeleteStatus>("bulkDelete", new PartitionKey("Energy Bars"), new dynamic[] { query });

                await Console.Out.WriteLineAsync($"Batch Delete Completed.\tDeleted: {result.Resource.Deleted}\tContinue: {result.Resource.Continuation}");
                resume = result.Resource.Continuation;
            }
            while (resume);
        }
    }
}