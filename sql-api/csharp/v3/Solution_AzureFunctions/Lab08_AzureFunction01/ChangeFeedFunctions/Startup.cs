using System;
using Microsoft.Azure.Functions.Extensions.DependencyInjection;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Configuration;
using ChangeFeedFunctions.Entities;
using Microsoft.Azure.Cosmos;


[assembly: FunctionsStartup(typeof(ChangeFeedFunctions.Startup))]

namespace ChangeFeedFunctions
{
    public class Startup : FunctionsStartup
    {
        public override void Configure(IFunctionsHostBuilder builder)
        {
            builder.Services.AddOptions<Settings>()
                .Configure<IConfiguration>((settings, configuration) =>
                {
                    configuration.GetSection("CosmosDB").Bind(settings);
                });

            builder.Services.AddSingleton((s) =>
            {
                string endpointUri = Environment.GetEnvironmentVariable("CosmosDB:TargetCosmosDBAccountEndpointUri");
                string primaryKey = Environment.GetEnvironmentVariable("CosmosDB:TargetCosmosDBAccountPrimaryKey");
                return new CosmosClient(endpointUri, primaryKey);
            });
        }
    }
}