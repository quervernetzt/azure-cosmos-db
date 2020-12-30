package labs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemResponse;

import labs.Models.Food;
import labs.Models.Tag;
import labs.Models.Enums.CosmosDBStatusCodes;
import services.CosmosDBService;

public class Lab10 {
	private static final Logger logger = LoggerFactory.getLogger(Lab10.class);
	private CosmosDBService cosmosDBService;

	/**
	 * Constructor
	 * 
	 * @param cosmosDBService The Cosmos DB service instance
	 * @throws Exception All possible exceptions that can be thrown
	 */
	public Lab10(final CosmosDBService cosmosDBService) throws Exception {
		this.cosmosDBService = cosmosDBService;
		String databaseId = "NutritionDatabase";
		String containerId = "FoodCollection";

		// Get database and container
		CosmosAsyncDatabase targetDatabase = this.cosmosDBService.getDatabase(databaseId);
		CosmosAsyncContainer targetContainer = this.cosmosDBService.getContainer(targetDatabase, containerId);

		// Observe the ETag Property
		String itemId = "21259";
		String partitionKeyValue = "Fast Foods";
		Food foodItem = this.cosmosDBService
				.executePointReadAsync(targetContainer, itemId, partitionKeyValue, Food.class).block();

		// Check ETag for upsert
		// Success
		foodItem.addTag(new Tag("Demo"));
		CosmosItemResponse<Food> upsertResponse = this.cosmosDBService.upsertItem(targetContainer, foodItem, true)
				.block();
		logger.info("Lab10: New ETag value for first try is '{}'...", upsertResponse.getETag());

		// Failure
		try {
			foodItem.addTag(new Tag("Failure"));
			upsertResponse = this.cosmosDBService.upsertItem(targetContainer, foodItem, true).block();
		} catch (Exception ex) {
			if (ex instanceof CosmosException) {
				CosmosException cex = (CosmosException) ex;
				int statusCode = cex.getStatusCode();
				if (statusCode == CosmosDBStatusCodes.PreconditionFailure.getValue()) {
					logger.error("Lab10: Precondition failed exception...");
				} else {
					logger.error("Lab10: Other Cosmos DB exception...");
					throw cex;
				}
			} else {
				logger.error("Lab10: Other exception...");
				throw ex;
			}
		}
	}
}
