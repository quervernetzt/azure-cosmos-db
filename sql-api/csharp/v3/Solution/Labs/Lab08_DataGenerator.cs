using System;
using System.Threading.Tasks;
using Microsoft.Azure.Cosmos;
using System.Collections.Generic;
using Solution.Entities;
using Solution.Entities.Lab08;

namespace Solution.Labs
{
    public class Lab08_DataGenerator
    {
        /// <summary>
        ///     Constructor
        /// </summary>
        /// <param name="configuration">
        ///     Configuration object
        /// </param>
        public Lab08_DataGenerator(IConfiguration _configuration)
        {
            string cosmosDBURI = _configuration.CosmosDB.URI;
            string cosmosDBPrimaryKey = _configuration.CosmosDB.PrimaryKey;
            string cosmosDBConnectionString = _configuration.CosmosDB.PrimaryConnectionString;
            CosmosClient cosmosDBClient = new CosmosClient(cosmosDBURI, cosmosDBPrimaryKey);

            string databaseId = "StoreDatabase";
            Database storeDatabase = cosmosDBClient.GetDatabase(databaseId);
            string containerId = "CartContainer";
            Container cartContainer = cosmosDBClient.GetContainer(databaseId, containerId);

            RunDataGenerator(cartContainer).GetAwaiter().GetResult();
        }

        /// <summary>
        ///     Generate and store data in container
        /// </summary>
        /// <param name="container">
        ///     Container reference
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task RunDataGenerator(Container container)
        {
            Console.WriteLine("Press any key to stop the console app...");

            var tasks = new List<Task>();

            while (!Console.KeyAvailable)
            {
                foreach (CartAction action in GenerateActions())
                {
                    tasks.Add(AddItem(container, action));
                    await Task.Delay(10);
                    Console.Write("*");
                }
            }

            Console.WriteLine("Stopping...");
            await Task.WhenAll(tasks);
        }

        /// <summary>
        ///     Add item to container
        /// </summary>
        /// <param name="container">
        ///     Container reference
        /// </param>
        /// <param name="item">
        ///     The item to add
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        private async Task AddItem(Container container, CartAction item)
        {
            await container.CreateItemAsync(item, new PartitionKey(item.Item));
            return;
        }

        /// <summary>
        ///     Generate data
        /// </summary>
        /// <returns>
        ///     Returns the generated data
        /// </returns>
        private List<CartAction> GenerateActions()
        {
            Random _random = new Random();

            string[] items = new string[]
            {
                "Unisex Socks", "Women's Earring", "Women's Necklace", "Unisex Beanie",
                "Men's Baseball Hat", "Unisex Gloves", "Women's Flip Flop Shoes", "Women's Silver Necklace",
                "Men's Black Tee", "Men's Black Hoodie", "Women's Blue Sweater", "Women's Sweatpants",
                "Men's Athletic Shorts", "Women's Athletic Shorts", "Women's White Sweater", "Women's Green Sweater",
                "Men's Windbreaker Jacket", "Women's Sandal", "Women's Rainjacket", "Women's Denim Shorts",
                "Men's Fleece Jacket", "Women's Denim Jacket", "Men's Walking Shoes", "Women's Crewneck Sweater",
                "Men's Button-Up Shirt", "Women's Flannel Shirt", "Women's Light Jeans", "Men's Jeans",
                "Women's Dark Jeans", "Women's Red Top", "Men's White Shirt", "Women's Pant", "Women's Blazer Jacket", "Men's Puffy Jacket",
                "Women's Puffy Jacket", "Women's Athletic Shoes", "Men's Athletic Shoes", "Women's Black Dress", "Men's Suit Jacket", "Men's Suit Pant",
                "Women's High Heel Shoe", "Women's Cardigan Sweater", "Men's Dress Shoes", "Unisex Puffy Jacket", "Women's Red Dress", "Unisex Scarf",
                "Women's White Dress", "Unisex Sandals", "Women's Bag"
            };

            string[] states = new string[]
            {
                "AL","AK","AS","AZ","AR","CA","CO","CT","DE","DC","FM","FL","GA","GU","HI","ID","IL","IN",
                "IA","KS","KY","LA","ME","MH","MD","MA","MI","MN","MS","MO","MT","NE","NV","NH","NJ","NM",
                "NY","NC","ND","MP","OH","OK","OR","PW","PA","PR","RI","SC","SD","TN","TX","UT","VT","VI",
                "VA","WA","WV","WI","WY"
            };

            double[] prices = new double[]
            {
               3.75, 8.00, 12.00, 10.00,
                17.00, 20.00, 14.00, 15.50,
                9.00, 25.00, 27.00, 21.00, 22.50,
                22.50, 32.00, 30.00, 49.99, 35.50,
                55.00, 50.00, 65.00, 31.99, 79.99,
                22.00, 19.99, 19.99, 80.00, 85.00,
                90.00, 33.00, 25.20, 40.00, 87.50, 99.99,
                95.99, 75.00, 70.00, 65.00, 92.00, 95.00,
                72.00, 25.00, 120.00, 105.00, 130.00, 29.99,
                84.99, 12.00, 37.50
            };

            List<CartAction> actions = new List<CartAction>();

            int itemIndex = _random.Next(0, items.Length - 1);
            int stateIndex = _random.Next(0, states.Length - 1);

            Array values = Enum.GetValues(typeof(ActionType));
            ActionType randomAction = (ActionType)values.GetValue(_random.Next(values.Length));

            var action = new CartAction
            {
                CartId = _random.Next(1000, 99999),
                Action = randomAction,
                Item = items[itemIndex],
                Price = prices[itemIndex],
                BuyerState = states[stateIndex]
            };

            if (action.Action != ActionType.Viewed)
            {
                var previousActions = new List<ActionType> { ActionType.Viewed };

                if (action.Action == ActionType.Purchased)
                {
                    previousActions.Add(ActionType.Added);
                }

                foreach (var previousAction in previousActions)
                {
                    var previous = new CartAction
                    {
                        CartId = action.CartId,
                        Action = previousAction,
                        Item = action.Item,
                        Price = action.Price,
                        BuyerState = action.BuyerState
                    };

                    actions.Add(previous);
                }
            }

            actions.Add(action);
            return actions;
        }
    }
}
