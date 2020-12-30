package labs;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.ObjectMapper;

import labs.Models.DeleteStatus;
import labs.Models.Food;
import reactor.core.publisher.Mono;
import services.CosmosDBService;
import services.DataFaker;

public class Lab07 {
	private static final Logger logger = LoggerFactory.getLogger(Lab07.class);
	private CosmosDBService cosmosDBService;
	private static ObjectMapper mapper = new ObjectMapper();
	private static int pointer = 0;
	private static boolean resume = false;

	/**
	 * Constructor
	 * 
	 * @param cosmosDBService The Cosmos DB service instance
	 * @throws Exception All possible exceptions that can be thrown
	 */
	public Lab07(final CosmosDBService cosmosDBService) throws Exception {
		this.cosmosDBService = cosmosDBService;
		String databaseId = "NutritionDatabase";
		String containerId = "FoodCollection";

		// Get database and container
		CosmosAsyncDatabase targetDatabase = this.cosmosDBService.getDatabase(databaseId);
		CosmosAsyncContainer targetContainer = this.cosmosDBService.getContainer(targetDatabase, containerId);

		// Bulk Upload
		executeBulkUpload(targetContainer);

		// Bulk Delete
		executeBulkDelete(targetContainer);
	}

	/**
	 * Execute Bulk upload.
	 * 
	 * @param container The container to upload the items to
	 */
	private void executeBulkUpload(final CosmosAsyncContainer container) {
		DataFaker dataFaker = new DataFaker();
		List<Food> foods = dataFaker.generateFood(1000);

		String sprocName = "bulkUpload";
		CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
		options.setPartitionKey(new PartitionKey("Energy Bars"));

		while (pointer < foods.size()) {
			List<Object> sprocArgs = new ArrayList<Object>();
			List<Food> foodObjectsToUpload = foods.subList(pointer, foods.size());
			sprocArgs.add(foodObjectsToUpload);

			this.cosmosDBService.executeStoredProcedureAsync(container, sprocName, options, sprocArgs)
					.flatMap(response -> {
						int delta_items = Integer.parseInt(response.getResponseAsString());
						pointer += delta_items;
						logger.info("{} Total Items {} Items Uploaded in this Iteration...", pointer, delta_items);
						return Mono.empty();
					}).block();
		}
	}

	/**
	 * Execute Bulk delete.
	 * 
	 * @param container The container where to delete the items
	 */
	private void executeBulkDelete(final CosmosAsyncContainer container) {
		resume = true;
		String sprocName = "bulkDelete";
		String query = "SELECT * FROM foods f WHERE f.foodGroup = 'Energy Bars'";

		CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
		options.setPartitionKey(new PartitionKey("Energy Bars"));

		List<Object> sprocArgs = new ArrayList<Object>();
		sprocArgs.add(query);

		do {
			this.cosmosDBService.executeStoredProcedureAsync(container, sprocName, options, sprocArgs)
					.flatMap(executeResponse -> {

						DeleteStatus result = null;

						try {
							result = mapper.readValue(executeResponse.getResponseAsString(), DeleteStatus.class);
						} catch (Exception ex) {
							logger.error("Failed to parse bulkDelete response.", ex);
						}

						logger.info("Batch Delete Completed. Deleted: {} Continue: {} Items Uploaded in this Iteration",
								result.getDeleted(), result.isContinuation());

						resume = result.isContinuation();

						return Mono.empty();
					}).block();
		} while (resume);
	}
}
