package labs;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;

import labs.Models.PurchaseFoodOrBeverage;
import labs.Models.ViewMap;
import labs.Models.WatchLiveTelevisionChannel;
import services.CosmosDBService;
import services.DataFaker;

public class Lab01 {

	private static final Logger logger = LoggerFactory.getLogger(Lab01.class);

	private CosmosDBService cosmosDBService;

	/**
	 * Constructor
	 * 
	 * @param cosmosDBService The Cosmos DB service instance
	 * @throws Exception All possible exceptions that can be thrown
	 */
	public Lab01(final CosmosDBService cosmosDBService) throws Exception {

		this.cosmosDBService = cosmosDBService;

		String databaseId = "EntertainmentDatabase";
		String containerId = "CustomCollection";
		String partitionKeyPath = "/type";
		int containerThroughput = 10000;

		// Get database and container
		CosmosAsyncDatabase targetDatabase = this.cosmosDBService.createDatabaseIfNotExistsAsync(databaseId);
		CosmosAsyncContainer targetContainer = this.cosmosDBService.createContainerWithManualThroughputIfNotExistsAsync(
				containerId, partitionKeyPath, containerThroughput, targetDatabase);

		// Scale container
		boolean enableScaling = false;
		if (enableScaling) {
			int targetThroughput = 400;
			this.cosmosDBService.changeThroughputForManualScaleOnContainerAsync(targetDatabase, containerId,
					targetThroughput, false);
		}

		// Change indexing policy
		boolean enableIndex = false;
		if (enableIndex) {
			IndexingPolicy indexingPolicy = new IndexingPolicy();
			indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
			indexingPolicy.setAutomatic(true);
			List<IncludedPath> includedPaths = new ArrayList<>();
			IncludedPath includedPathAll = new IncludedPath("/*");
			includedPaths.add(includedPathAll);
			IncludedPath includedPathTest = new IncludedPath("/test/?");
			includedPaths.add(includedPathTest);
			indexingPolicy.setIncludedPaths(includedPaths);

			this.cosmosDBService.changeIndexingPolicyAsync(targetDatabase, containerId, indexingPolicy).block();
		}

		// Create fake items
		boolean enablePurchaseFoodOrBeverageObjects = false;
		if (enablePurchaseFoodOrBeverageObjects) {
			DataFaker dataFaker = new DataFaker();
			ArrayList<PurchaseFoodOrBeverage> purchaseFoodOrBeverageObjects = dataFaker
					.generatePurchaseFoodOrBeverage(500);
			List<CosmosItemResponse<PurchaseFoodOrBeverage>> purchaseFoodOrBeverageItemCreationResults = cosmosDBService
					.createItemsAsync(targetContainer, purchaseFoodOrBeverageObjects).block();
			purchaseFoodOrBeverageItemCreationResults
					.forEach(result -> logger.info("Lab01: Item Created\t{}", result.getItem().getId()));
		}

		boolean enableTVObjects = false;
		if (enableTVObjects) {
			DataFaker dataFaker = new DataFaker();
			ArrayList<WatchLiveTelevisionChannel> tvObjects = dataFaker.generateWatchLiveTelevisionChannel(500);
			List<CosmosItemResponse<WatchLiveTelevisionChannel>> tvItemCreationResults = cosmosDBService
					.createItemsAsync(targetContainer, tvObjects).block();
			tvItemCreationResults.forEach(result -> logger.info("Lab01: Item Created\t{}", result.getItem().getId()));
		}

		boolean enableViewMapObjects = false;
		if (enableViewMapObjects) {
			DataFaker dataFaker = new DataFaker();
			ArrayList<ViewMap> viewMapObjects = dataFaker.generateViewMap(500);
			List<CosmosItemResponse<ViewMap>> viewMapItemCreationResults = cosmosDBService
					.createItemsAsync(targetContainer, viewMapObjects).block();
			viewMapItemCreationResults.forEach(result -> logger.info("Lab01: Item Created\t{}", result.getItem().getId()));
		}
	}
}
