using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Azure.Cosmos;
using Solution.Entities;
using Solution.Entities.Lab08;

namespace Solution.Labs
{
    class Lab08_ChangeFeedConsole
    {
        /// <summary>
        ///     Constructor
        /// </summary>
        /// <param name="configuration">
        ///     Configuration object
        /// </param>
        public Lab08_ChangeFeedConsole(IConfiguration _configuration)
        {
            string cosmosDBURI = _configuration.CosmosDB.URI;
            string cosmosDBPrimaryKey = _configuration.CosmosDB.PrimaryKey;
            string cosmosDBConnectionString = _configuration.CosmosDB.PrimaryConnectionString;
            CosmosClient cosmosDBClient = new CosmosClient(cosmosDBURI, cosmosDBPrimaryKey);

            string databaseId = "StoreDatabase";
            Database storeDatabase = cosmosDBClient.GetDatabase(databaseId);
            string containerIdSource = "CartContainer";
            Container cartContainerSource = cosmosDBClient.GetContainer(databaseId, containerIdSource);
            string containerIdDestination = "CartContainerByState";
            Container cartContainerDestination = cosmosDBClient.GetContainer(databaseId, containerIdDestination);

            Container leaseContainer = CreateLeaseContainer(storeDatabase).GetAwaiter().GetResult();

            RunChangeFeedProcessor(cartContainerSource, cartContainerDestination, leaseContainer).GetAwaiter().GetResult();
        }

        /// <summary>
        ///     Create the lease container for the change feed
        /// </summary>
        /// <param name="database">
        ///     Database reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns> 
        private async Task<Container> CreateLeaseContainer(Database database)
        {
            ContainerProperties leaseContainerProperties = new ContainerProperties("consoleLeases", "/id");
            Container leaseContainer = await database.CreateContainerIfNotExistsAsync(leaseContainerProperties, throughput: 400);
            Console.WriteLine("Lease container has been created...");

            return leaseContainer;
        }

        /// <summary>
        ///     Run the change feed processor
        /// </summary>
        /// <param name="sourceContainer">
        ///     Source container reference
        /// </param>
        /// <param name="destinationContainer">
        ///     Destination container reference
        /// </param>
        /// <param name="leaseContainer">
        ///     Lease container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task RunChangeFeedProcessor(
            Container sourceContainer,
            Container destinationContainer,
            Container leaseContainer)
        {
            ChangeFeedProcessorBuilder builder = sourceContainer.GetChangeFeedProcessorBuilder("migrationProcessor",
                           (IReadOnlyCollection<CartAction> input, CancellationToken cancellationToken) =>
                           {
                               Console.WriteLine(input.Count + " Changes Received");
                               var tasks = new List<Task>();

                               foreach (var doc in input)
                               {
                                   tasks.Add(destinationContainer.CreateItemAsync(doc, new PartitionKey(doc.BuyerState)));
                               }

                               return Task.WhenAll(tasks);
                           });

            ChangeFeedProcessor processor = builder
                            .WithInstanceName("changeFeedConsole")
                            .WithLeaseContainer(leaseContainer)
                            .Build();

            await processor.StartAsync();
            Console.WriteLine("Started Change Feed Processor");
            Console.WriteLine("Press any key to stop the processor...");

            Console.ReadKey();

            Console.WriteLine("Stopping Change Feed Processor");
            await processor.StopAsync();
        }
    }
}