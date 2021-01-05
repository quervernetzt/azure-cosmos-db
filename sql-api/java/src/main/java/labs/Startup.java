package labs;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import labs.Interfaces.IConfiguration;
import labs.Models.Configuration;
import labs.Models.CosmosDBConfig;
import services.CosmosDBService;

/**
 * Startup class that is initiated at startup of the application.
 *
 */
public class Startup {

	private IConfiguration applicationConfig;
	private CosmosDBService cosmosDBService;

	/**
	 * @param configFileName
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public Startup(final String configFileName) throws JsonParseException, JsonMappingException, IOException {

		applicationConfig = this.loadApplicationConfig(configFileName);

		CosmosDBConfig cosmosDBConfig = applicationConfig.getCosmosDBConfig();
		cosmosDBService = new CosmosDBService(cosmosDBConfig);
	}

	/**
	 * Load the application configuration into an object.
	 *
	 * @param configFileName The name of the YAML configuration file
	 * @return The populated Config object
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private Configuration loadApplicationConfig(final String configFileName)
			throws JsonParseException, JsonMappingException, IOException {
		// Loading the YAML file from the /resources folder
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		File file = new File(classLoader.getResource(configFileName).getFile());

		// Instantiating a new ObjectMapper as a YAMLFactory
		ObjectMapper om = new ObjectMapper(new YAMLFactory());

		// Mapping the employee from the YAML file to the Employee class
		Configuration config = om.readValue(file, Configuration.class);

		return config;
	}

	/**
	 * Get the application configuration instance.
	 *
	 * @return The Application configuration instance
	 */
	public IConfiguration getApplicationConfig() {
		return this.applicationConfig;
	}

	/**
	 * Get the CosmosDBService instance
	 * 
	 * @return The CosmosDBService instance
	 */
	public CosmosDBService getCosmosDBService() {
		return this.cosmosDBService;
	}
}
