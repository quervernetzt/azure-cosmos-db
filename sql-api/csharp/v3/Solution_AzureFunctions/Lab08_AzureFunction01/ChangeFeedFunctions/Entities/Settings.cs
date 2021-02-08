namespace ChangeFeedFunctions.Entities
{
    public class Settings
    {
        public string SourceCosmosDBAccountConnectionString { get; set; }
        public string TargetCosmosDBAccountEndpointUrl { get; set; }
        public string TargetCosmosDBAccountPrimaryKey { get; set; }
        public string TargetCosmosDBAccountDatabaseId { get; set; }
        public string TargetCosmosDBAccountContainerId { get; set; }
    }
}