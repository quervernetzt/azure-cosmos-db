namespace Solution.Entities
{
    public class Configuration : IConfiguration
    {
        public CosmosDB CosmosDB { get; set; }
    }

    public class CosmosDB
    {
        public string URI { get; set; }
        public string PrimaryKey { get; set; }
        public string PrimaryConnectionString { get; set; }
    }
}