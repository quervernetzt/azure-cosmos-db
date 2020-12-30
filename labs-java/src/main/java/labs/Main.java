package labs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.CosmosDBService;

public class Main {

	private static final String configFileName = "config.yaml";
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * Default private constructor.
	 */
	private Main() {
	}

	/**
	 * Main method.
	 *
	 * @param args Arguments passed to the execution of the application
	 */
	public static void main(final String[] args) {
		try {
			Startup startup = new Startup(configFileName);

			CosmosDBService cosmosDbService = startup.getCosmosDBService();
			
//			new Lab01(cosmosDbService);
//			new Lab05(cosmosDbService);
//			new Lab07(cosmosDbService);
//			new Lab09(cosmosDbService);
//			new Lab10(cosmosDbService);

			cosmosDbService.getCosmosAsyncClient().close();
			logger.info("Done...");
		} catch (Exception exception) {
			logger.error("main: Exception...", exception);
		}
	}
}
