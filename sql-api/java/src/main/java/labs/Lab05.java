package labs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosQueryRequestOptions;

import labs.Models.Food;
import services.CosmosDBService;

public class Lab05 {
	private static final Logger logger = LoggerFactory.getLogger(Lab05.class);

	private CosmosDBService cosmosDBService;

	/**
	 * Constructor
	 * 
	 * @param cosmosDBService The Cosmos DB service instance
	 * @throws Exception All possible exceptions that can be thrown
	 */
	public Lab05(final CosmosDBService cosmosDBService) throws Exception {

		this.cosmosDBService = cosmosDBService;

		String databaseId = "NutritionDatabase";
		String containerId = "FoodCollection";

		// Get database and container
		CosmosAsyncDatabase targetDatabase = this.cosmosDBService.getDatabase(databaseId);
		CosmosAsyncContainer targetContainer = this.cosmosDBService.getContainer(targetDatabase, containerId);

		// Read a single item
		boolean enableReadItem = false;
		if (enableReadItem) {
			String foodItemId = "19130";
			String foodPartitionKey = "Sweets";
			Food candy = this.cosmosDBService
					.executePointReadAsync(targetContainer, foodItemId, foodPartitionKey, Food.class).block();
			logger.info(String.format("Lab05: Executed Point Read: '%s'...", candy.getId()));
		}

		// Query for items on single partition
		boolean enableQueryItemsSinglePartition = false;
		if (enableQueryItemsSinglePartition) {
			CosmosQueryRequestOptions queryOptionsSingle = new CosmosQueryRequestOptions();
			queryOptionsSingle.setMaxDegreeOfParallelism(1);
			String sqlQuerySingle = "SELECT f.description, f.manufacturerName, f.servings " + "FROM foods f "
					+ "WHERE f.foodGroup = 'Sweets' " + "and IS_DEFINED(f.description) "
					+ "and IS_DEFINED(f.manufacturerName) " + "and IS_DEFINED(f.servings)";
			List<Food> foodItemsSingle = this.cosmosDBService.executeSQLQueryAndGetAllItems(targetContainer,
					queryOptionsSingle, sqlQuerySingle, Food.class);
			logger.info(String.format("Lab05: Retrieved '%s' items...", foodItemsSingle.size()));
			foodItemsSingle.forEach(item -> {
				logger.info(String.format("Lab05: '%s'...", item.getDescription()));
			});
		}

		// Query for items on single partition
		boolean enableQueryItemsMultiplePartitions = false;
		if (enableQueryItemsMultiplePartitions) {
			CosmosQueryRequestOptions queryOptionsMultiple = new CosmosQueryRequestOptions();
			queryOptionsMultiple.setMaxDegreeOfParallelism(5);
			queryOptionsMultiple.setMaxBufferedItemCount(100);
			String sqlQueryMultiple = "SELECT f.id, f.description, f.manufacturerName, f.servings " + "FROM foods f "
					+ "WHERE IS_DEFINED(f.manufacturerName)";
			List<Food> foodItemsMultiple = this.cosmosDBService.executeSQLQueryAndGetAllItems(targetContainer,
					queryOptionsMultiple, sqlQueryMultiple, Food.class);
			logger.info(String.format("Lab05: Retrieved '%s' items...", foodItemsMultiple.size()));
			foodItemsMultiple.forEach(item -> {
				logger.info(String.format("Lab05: '%s'...", item.getDescription()));
			});
		}
	}
}
