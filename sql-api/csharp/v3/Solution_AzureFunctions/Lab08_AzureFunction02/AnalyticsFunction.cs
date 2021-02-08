using System.Collections.Generic;
using Microsoft.Azure.Documents;
using Microsoft.Azure.WebJobs;
using Microsoft.Extensions.Logging;
using Microsoft.Azure.EventHubs;
using System.Threading.Tasks;
using System.Text;

namespace ChangeFeedFunctions
{
    public static class AnalyticsFunction
    {
        private static readonly string _eventHubConnection = "Endpoint=sb://xxx.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=xxx";
        private static readonly string _eventHubName = "carteventhub";

        [FunctionName("AnalyticsFunction")]
        public static async Task Run([CosmosDBTrigger(
            databaseName: "StoreDatabase",
            collectionName: "CartContainer",
            ConnectionStringSetting = "DBConnection",
            CreateLeaseCollectionIfNotExists = true,
            LeaseCollectionName = "analyticsLeases")]IReadOnlyList<Document> input, ILogger log)
        {
            if (input != null && input.Count > 0)
            {
                EventHubsConnectionStringBuilder sbEventHubConnection = new EventHubsConnectionStringBuilder(_eventHubConnection)
                {
                    EntityPath = _eventHubName
                };

                EventHubClient eventHubClient = EventHubClient.CreateFromConnectionString(sbEventHubConnection.ToString());

                List<Task> tasks = new List<Task>();

                foreach (Document doc in input)
                {
                    string json = doc.ToString();

                    EventData eventData = new EventData(Encoding.UTF8.GetBytes(json));

                    log.LogInformation("Writing to Event Hub");
                    tasks.Add(eventHubClient.SendAsync(eventData));
                }

                await Task.WhenAll(tasks);
            }
        }
    }
}