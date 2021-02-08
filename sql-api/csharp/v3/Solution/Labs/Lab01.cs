using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Microsoft.Azure.Cosmos;
using Solution.Entities;
using Solution.Entities.Lab01;

namespace Solution.Labs
{
    public class Lab01
    {
        /// <summary>
        ///     Constructor
        /// </summary>
        /// <param name="configuration">
        ///     Configuration object
        /// </param>
        public Lab01(IConfiguration _configuration)
        {
            string cosmosDBURI = _configuration.CosmosDB.URI;
            string cosmosDBPrimaryKey = _configuration.CosmosDB.PrimaryKey;
            string cosmosDBConnectionString = _configuration.CosmosDB.PrimaryConnectionString;
            CosmosClient cosmosDBClient = new CosmosClient(cosmosDBURI, cosmosDBPrimaryKey);

            string databaseId = "EntertainmentDatabase";
            Database entertainmentDatabase = InitializeDatabase(cosmosDBClient, databaseId).GetAwaiter().GetResult();
            string containerId = "EntertainmentContainer";
            Container entertainmentContainer = InitializeContainer(entertainmentDatabase, containerId).GetAwaiter().GetResult();

            // LoadFoodAndBeverage(entertainmentContainer).GetAwaiter().GetResult();
            // LoadTelevision(entertainmentContainer).GetAwaiter().GetResult();
            // LoadMapViews(entertainmentContainer).GetAwaiter().GetResult();
        }

        /// <summary>
        ///     Create databse if it does not exist
        /// </summary>
        /// <param name="client">
        ///     The CosmosDB client
        /// </param>
        /// <param name="databaseId">
        ///     The database id
        /// </param>
        /// <returns>
        ///     Returns the database reference
        /// </returns>
        private async Task<Database> InitializeDatabase(CosmosClient client, string databaseId)
        {
            Database database = await client.CreateDatabaseIfNotExistsAsync(databaseId);
            await Console.Out.WriteLineAsync($"Database Id:\t{database.Id}");
            return database;
        }

        /// <summary>
        ///     Create container if it does not exist
        /// </summary>
        /// <param name="database">
        ///     The database reference
        /// </param>
        /// <param name="containerId">
        ///     The container id
        /// </param>
        /// <returns>
        ///     Returns the container reference
        /// </returns>
        private async Task<Container> InitializeContainer(Database database, string containerId)
        {
            // Create indexing policy
            IndexingPolicy indexingPolicy = new IndexingPolicy
            {
                IndexingMode = IndexingMode.Consistent,
                Automatic = true,
                IncludedPaths =
                {
                    new IncludedPath
                    {
                        Path = "/*"
                    }
                },
                ExcludedPaths =
                {
                    new ExcludedPath
                    {
                        Path = "/\"_etag\"/?"
                    }
                }
            };

            // Create container
            string partitionKeyPath = "/type";
            int requestUnits = 400;
            ContainerProperties containerProperties = new ContainerProperties(containerId, partitionKeyPath)
            {
                IndexingPolicy = indexingPolicy
            };
            Container container = await database.CreateContainerIfNotExistsAsync(containerProperties, requestUnits);

            await Console.Out.WriteLineAsync($"Container Id:\t{container.Id}");
            return container;
        }

        /// <summary>
        ///     Generate fake FoodAndBeverage data
        /// </summary>
        /// <param name="container">
        ///     The container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task LoadFoodAndBeverage(Container container)
        {
            // Generate data
            IEnumerable<PurchaseFoodOrBeverage> foodInteractions = new Bogus.Faker<PurchaseFoodOrBeverage>()
                .RuleFor(i => i.id, (fake) => Guid.NewGuid().ToString())
                .RuleFor(i => i.type, (fake) => nameof(PurchaseFoodOrBeverage))
                .RuleFor(i => i.unitPrice, (fake) => Math.Round(fake.Random.Decimal(1.99m, 15.99m), 2))
                .RuleFor(i => i.quantity, (fake) => fake.Random.Number(1, 5))
                .RuleFor(i => i.totalPrice, (fake, user) => Math.Round(user.unitPrice * user.quantity, 2))
                .GenerateLazy(100);

            // Write data to container
            foreach (var interaction in foodInteractions)
            {
                ItemResponse<PurchaseFoodOrBeverage> result = await container.CreateItemAsync(interaction, new PartitionKey(interaction.type));
                await Console.Out.WriteLineAsync($"Item Created\t{result.Resource.id}");
            }
        }

        /// <summary>
        ///     Generate fake Television data
        /// </summary>
        /// <param name="container">
        ///     The container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private static async Task LoadTelevision(Container container)
        {
            // Generate data
            IEnumerable<WatchLiveTelevisionChannel> tvInteractions = new Bogus.Faker<WatchLiveTelevisionChannel>()
                .RuleFor(i => i.id, (fake) => Guid.NewGuid().ToString())
                .RuleFor(i => i.type, (fake) => nameof(WatchLiveTelevisionChannel))
                .RuleFor(i => i.minutesViewed, (fake) => fake.Random.Number(1, 45))
                .RuleFor(i => i.channelName, (fake) => fake.PickRandom(new List<string> { "NEWS-6", "DRAMA-15", "ACTION-12", "DOCUMENTARY-4", "SPORTS-8" }))
                .GenerateLazy(100);

            // Write data to container
            foreach (var interaction in tvInteractions)
            {
                ItemResponse<WatchLiveTelevisionChannel> result = await container.CreateItemAsync(interaction, new PartitionKey(interaction.type));
                await Console.Out.WriteLineAsync($"Item Created\t{result.Resource.id}");
            }
        }

        /// <summary>
        ///     Generate fake MapView data
        /// </summary>
        /// <param name="container">
        ///     The container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private static async Task LoadMapViews(Container container)
        {
            // Generate data
            IEnumerable<ViewMap> mapInteractions = new Bogus.Faker<ViewMap>()
                .RuleFor(i => i.id, (fake) => Guid.NewGuid().ToString())
                .RuleFor(i => i.type, (fake) => nameof(ViewMap))
                .RuleFor(i => i.minutesViewed, (fake) => fake.Random.Number(1, 45))
                .GenerateLazy(100);

            // Write data to container
            foreach (var interaction in mapInteractions)
            {
                ItemResponse<ViewMap> result = await container.CreateItemAsync(interaction);
                await Console.Out.WriteLineAsync($"Item Created\t{result.Resource.id}");
            }
        }
    }
}