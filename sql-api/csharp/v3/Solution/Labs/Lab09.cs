using System;
using System.Threading.Tasks;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics;
using Microsoft.Azure.Cosmos;
using Solution.Entities;
using Solution.Entities.Lab09;

namespace Solution.Labs
{
    public class Lab09
    {
        /// <summary>
        ///     Constructor
        /// </summary>
        /// <param name="configuration">
        ///     Configuration object
        /// </param>
        public Lab09(IConfiguration _configuration)
        {
            string cosmosDBURI = _configuration.CosmosDB.URI;
            string cosmosDBPrimaryKey = _configuration.CosmosDB.PrimaryKey;
            string cosmosDBConnectionString = _configuration.CosmosDB.PrimaryConnectionString;
            CosmosClient cosmosDBClient = new CosmosClient(cosmosDBURI, cosmosDBPrimaryKey);

            string databaseId = "FinancialDatabase";
            Database financialDatabase = cosmosDBClient.GetDatabase(databaseId);
            string peopleContainerId = "PeopleCollection";
            Container peopleContainer = cosmosDBClient.GetContainer(databaseId, peopleContainerId);
            string transactionContainerId = "TransactionCollection";
            Container transactionContainer = cosmosDBClient.GetContainer(databaseId, transactionContainerId);

            // CreateMember(peopleContainer).GetAwaiter().GetResult();
            // CreateTransactions(transactionContainer).GetAwaiter().GetResult();
            // QueryTransactions(transactionContainer).GetAwaiter().GetResult();
            // QueryTransactionsWithCustomOptions(transactionContainer).GetAwaiter().GetResult();
            // QueryMember(peopleContainer).GetAwaiter().GetResult();
            // ReadMember(peopleContainer).GetAwaiter().GetResult();
            // EstimateThroughput(peopleContainer).GetAwaiter().GetResult();
            // UpdateThroughput(peopleContainer).GetAwaiter().GetResult();
        }

        /// <summary>
        ///     Create member item
        /// </summary>
        /// <param name="peopleContainer">
        ///     People container reference
        /// </param>
        /// <returns>
        ///     Returns a task with the promise of the response request charge
        /// </returns>
        private static async Task<Double> CreateMember(Container peopleContainer)
        {
            object member = new Member
            {
                accountHolder = new Bogus.Person(),
                relatives = new Family
                {
                    spouse = new Bogus.Person(),
                    children = Enumerable.Range(0, 4).Select(r => new Bogus.Person())
                }
            };
            ItemResponse<object> response = await peopleContainer.CreateItemAsync(member);
            await Console.Out.WriteLineAsync($"{response.RequestCharge} RU/s");
            return response.RequestCharge;
        }

        /// <summary>
        ///     Create transaction items
        /// </summary>
        /// <param name="transactionContainer">
        ///     Transaction Container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private static async Task CreateTransactions(Container transactionContainer)
        {
            IEnumerable<Transaction> transactions = new Bogus.Faker<Transaction>()
                .RuleFor(t => t.id, (fake) => Guid.NewGuid().ToString())
                .RuleFor(t => t.amount, (fake) => Math.Round(fake.Random.Double(5, 500), 2))
                .RuleFor(t => t.processed, (fake) => fake.Random.Bool(0.6f))
                .RuleFor(t => t.paidBy, (fake) => $"{fake.Name.FirstName().ToLower()}.{fake.Name.LastName().ToLower()}")
                .RuleFor(t => t.costCenter, (fake) => fake.Commerce.Department(1).ToLower())
                .GenerateLazy(5000);

            List<Task<ItemResponse<Transaction>>> tasks = new List<Task<ItemResponse<Transaction>>>();
            foreach (Transaction transaction in transactions)
            {
                Task<ItemResponse<Transaction>> resultTask = transactionContainer.CreateItemAsync(transaction);
                tasks.Add(resultTask);
            }
            Task.WaitAll(tasks.ToArray());
            foreach (Task<ItemResponse<Transaction>> task in tasks)
            {
                await Console.Out.WriteLineAsync($"Item Created\t{task.Result.Resource.id}");
            }
        }

        /// <summary>
        ///     Execute queries against the transaction container
        /// </summary>
        /// <param name="transactionContainer">
        ///     Transaction container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private static async Task QueryTransactions(Container transactionContainer)
        {
            string sql = "SELECT TOP 1000 * FROM c WHERE c.processed = true ORDER BY c.amount DESC";
            FeedIterator<Transaction> query = transactionContainer.GetItemQueryIterator<Transaction>(sql);
            FeedResponse<Transaction> result = await query.ReadNextAsync();
            await Console.Out.WriteLineAsync($"Request Charge for '{sql}': {result.RequestCharge} RU/s...");

            sql = "SELECT * FROM c WHERE c.processed = true";
            query = transactionContainer.GetItemQueryIterator<Transaction>(sql);
            result = await query.ReadNextAsync();
            await Console.Out.WriteLineAsync($"Request Charge for '{sql}': {result.RequestCharge} RU/s...");

            sql = "SELECT * FROM c";
            query = transactionContainer.GetItemQueryIterator<Transaction>(sql);
            result = await query.ReadNextAsync();
            await Console.Out.WriteLineAsync($"Request Charge for '{sql}': {result.RequestCharge} RU/s...");

            sql = "SELECT c.id FROM c";
            query = transactionContainer.GetItemQueryIterator<Transaction>(sql);
            result = await query.ReadNextAsync();
            await Console.Out.WriteLineAsync($"Request Charge for '{sql}': {result.RequestCharge} RU/s...");
        }

        /// <summary>
        ///     Execute queries against the transaction container wit custom options
        /// </summary>
        /// <param name="transactionContainer">
        ///     Transaction container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task QueryTransactionsWithCustomOptions(Container transactionContainer)
        {
            int maxItemCount = 100;
            int maxDegreeOfParallelism = 1;
            int maxBufferedItemCount = 0;

            QueryRequestOptions options = new QueryRequestOptions
            {
                MaxItemCount = maxItemCount,
                MaxBufferedItemCount = maxBufferedItemCount,
                MaxConcurrency = maxDegreeOfParallelism
            };

            await ExecuteQueryWithCustomOptions(transactionContainer, options);

            await Console.Out.WriteLineAsync("-------------------------------------");

            maxItemCount = 100;
            maxDegreeOfParallelism = 5;
            maxBufferedItemCount = 0;

            options = new QueryRequestOptions
            {
                MaxItemCount = maxItemCount,
                MaxBufferedItemCount = maxBufferedItemCount,
                MaxConcurrency = maxDegreeOfParallelism
            };

            await ExecuteQueryWithCustomOptions(transactionContainer, options);

            await Console.Out.WriteLineAsync("-------------------------------------");

            maxItemCount = 100;
            maxDegreeOfParallelism = 5;
            maxBufferedItemCount = -1;

            options = new QueryRequestOptions
            {
                MaxItemCount = maxItemCount,
                MaxBufferedItemCount = maxBufferedItemCount,
                MaxConcurrency = maxDegreeOfParallelism
            };

            await ExecuteQueryWithCustomOptions(transactionContainer, options);

            await Console.Out.WriteLineAsync("-------------------------------------");

            maxItemCount = 100;
            maxDegreeOfParallelism = -1;
            maxBufferedItemCount = -1;

            options = new QueryRequestOptions
            {
                MaxItemCount = maxItemCount,
                MaxBufferedItemCount = maxBufferedItemCount,
                MaxConcurrency = maxDegreeOfParallelism
            };

            await ExecuteQueryWithCustomOptions(transactionContainer, options);

            await Console.Out.WriteLineAsync("-------------------------------------");

            maxItemCount = 500;
            maxDegreeOfParallelism = -1;
            maxBufferedItemCount = -1;

            options = new QueryRequestOptions
            {
                MaxItemCount = maxItemCount,
                MaxBufferedItemCount = maxBufferedItemCount,
                MaxConcurrency = maxDegreeOfParallelism
            };

            await ExecuteQueryWithCustomOptions(transactionContainer, options);

            await Console.Out.WriteLineAsync("-------------------------------------");

            maxItemCount = 1000;
            maxDegreeOfParallelism = -1;
            maxBufferedItemCount = -1;

            options = new QueryRequestOptions
            {
                MaxItemCount = maxItemCount,
                MaxBufferedItemCount = maxBufferedItemCount,
                MaxConcurrency = maxDegreeOfParallelism
            };

            await ExecuteQueryWithCustomOptions(transactionContainer, options);

            await Console.Out.WriteLineAsync("-------------------------------------");

            maxItemCount = 1000;
            maxDegreeOfParallelism = -1;
            maxBufferedItemCount = 50000;

            options = new QueryRequestOptions
            {
                MaxItemCount = maxItemCount,
                MaxBufferedItemCount = maxBufferedItemCount,
                MaxConcurrency = maxDegreeOfParallelism
            };

            await ExecuteQueryWithCustomOptions(transactionContainer, options);
        }

        /// <summary>
        ///     Helper method for QueryTransactionsWithCustomOptions to execute query
        /// </summary>
        /// <param name="transactionContainer">
        ///     Transaction container reference
        /// </param>
        /// <param name="options">
        ///     The query requestion options
        /// </param>
        /// <returns></returns>
        private async Task ExecuteQueryWithCustomOptions(Container transactionContainer, QueryRequestOptions options)
        {
            await Console.Out.WriteLineAsync($"MaxItemCount:\t{options.MaxItemCount}");
            await Console.Out.WriteLineAsync($"MaxDegreeOfParallelism:\t{options.MaxConcurrency}");
            await Console.Out.WriteLineAsync($"MaxBufferedItemCount:\t{options.MaxBufferedItemCount}");

            string sql = "SELECT * FROM c WHERE c.processed = true ORDER BY c.amount DESC";

            Stopwatch timer = Stopwatch.StartNew();

            FeedIterator<Transaction> query = transactionContainer.GetItemQueryIterator<Transaction>(sql, requestOptions: options);

            while (query.HasMoreResults)
            {
                FeedResponse<Transaction> result = await query.ReadNextAsync();
            }

            timer.Stop();
            await Console.Out.WriteLineAsync($"Elapsed Time:\t{timer.Elapsed.TotalSeconds}");
        }

        /// <summary>
        ///     Query member
        /// </summary>
        /// <param name="peopleContainer">
        ///     People container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task QueryMember(Container peopleContainer)
        {
            string sql = "SELECT TOP 1 * FROM c WHERE c.id = 'fa8740a9-ef10-455a-b4e0-6ea43ce22469'";
            FeedIterator<object> query = peopleContainer.GetItemQueryIterator<object>(sql);
            FeedResponse<object> response = await query.ReadNextAsync();

            await Console.Out.WriteLineAsync($"{response.Resource.First()}");
            await Console.Out.WriteLineAsync($"{response.RequestCharge} RU/s");
        }

        /// <summary>
        ///     Read member item
        /// </summary>
        /// <param name="peopleContainer">
        ///     People container reference
        /// </param>
        /// <returns>
        ///     Returns a task with the promise of the request charge
        /// </returns>
        private async Task<double> ReadMember(Container peopleContainer)
        {
            ItemResponse<object> response = await peopleContainer.ReadItemAsync<object>("fa8740a9-ef10-455a-b4e0-6ea43ce22469", new PartitionKey("Romaguera"));
            await Console.Out.WriteLineAsync($"{response.RequestCharge} RU/s");
            return response.RequestCharge;
        }

        /// <summary>
        ///     Estimate required throughput
        /// </summary>
        /// <param name="peopleContainer">
        ///     People container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task EstimateThroughput(Container peopleContainer)
        {
            int expectedWritesPerSec = 200;
            int expectedReadsPerSec = 800;

            double writeCost = await CreateMember(peopleContainer);
            double readCost = await ReadMember(peopleContainer);

            await Console.Out.WriteLineAsync($"Estimated load: {writeCost * expectedWritesPerSec + readCost * expectedReadsPerSec} RU/s");
        }

        /// <summary>
        ///     Update available throughput on container level
        /// </summary>
        /// <param name="peopleContainer">
        ///     People container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task UpdateThroughput(Container peopleContainer)
        {
            int? throughput = await peopleContainer.ReadThroughputAsync();
            await Console.Out.WriteLineAsync($"Current Throughput {throughput} RU/s");

            ThroughputResponse throughputResponse = await peopleContainer.ReadThroughputAsync(new RequestOptions());
            int? minThroughput = throughputResponse.MinThroughput;
            await Console.Out.WriteLineAsync($"Minimum Throughput {minThroughput} RU/s");

            await peopleContainer.ReplaceThroughputAsync(1000);
            throughput = await peopleContainer.ReadThroughputAsync();
            await Console.Out.WriteLineAsync($"New Throughput {throughput} RU/s");
        }
    }
}