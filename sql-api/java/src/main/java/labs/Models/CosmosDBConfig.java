package labs.Models;

import java.util.List;

public class CosmosDBConfig {

	public String endpointUri;

	public String primaryKey;

	public List<String> preferredRegions;

	public String clientDefaultConsistencyLevel;

	public int throttelingRetryMaxCount;
}
