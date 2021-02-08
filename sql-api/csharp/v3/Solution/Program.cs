using System;
using System.IO;
using System.Text.Json;
using System.Threading.Tasks;
using System.Threading;
using Microsoft.Extensions.DependencyInjection;
using Solution.Entities;
using Solution.Labs;

namespace Solution
{
    public class Program
    {
        private static IServiceProvider serviceProvider;

        /// <summary>
        ///     Main method
        /// </summary>
        /// <param name="args">
        ///     Array of input arguments
        /// </param>
        /// <returns>
        ///     Returns a task
        /// </returns>
        public static void Main(string[] args)
        {
            Console.WriteLine("Starting CosmosDB Labs...");

            ConfigureServices();
            Console.WriteLine("Created service container...");

            IConfiguration configuration = serviceProvider.GetService<IConfiguration>();

            // new Lab01(configuration);
            // new Lab05(configuration);
            // new Lab07(configuration);

            // Lab08
            // Thread dataGeneratorThread = new Thread(() => new Lab08_DataGenerator(configuration));
            // dataGeneratorThread.Start();


            // Thread changeFeedConsoleThread = new Thread(() => new Lab08_ChangeFeedConsole(configuration));
            // changeFeedConsoleThread.Start();

            // new Lab09(configuration);

            new Lab10(configuration);
        }

        /// <summary>
        ///     Create the dependency injection container
        /// </summary>
        private static void ConfigureServices()
        {
            ServiceCollection services = new ServiceCollection();

            services.AddSingleton<IConfiguration>((s) =>
            {
                string fileName = "config.json";
                string jsonString = File.ReadAllText(fileName);
                IConfiguration configuration = JsonSerializer.Deserialize<Configuration>(jsonString);
                return configuration;
            });

            serviceProvider = services.BuildServiceProvider();
        }
    }
}
