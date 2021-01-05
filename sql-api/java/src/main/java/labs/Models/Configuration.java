package labs.Models;

import labs.Interfaces.IConfiguration;

/**
 * Entity class for the application configuration.
 */
public class Configuration implements IConfiguration {

	/**
	 * Empty constructor. Necessary so that Jackson works.
	 */
	public Configuration() {
	}

	public CosmosDBConfig cosmosDBConfig;

	public CosmosDBConfig getCosmosDBConfig() {
		return cosmosDBConfig;
	}

	@Override
	public String toString() {
		return cosmosDBConfig.endpointUri;
	}
}
