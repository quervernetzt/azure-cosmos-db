package services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import labs.Models.BaseModel;
import labs.Models.CosmosDBConfig;

public class CosmosDBService {

	private static final Logger logger = LoggerFactory.getLogger(CosmosDBService.class);
	private CosmosDBConfig cosmosDBConfig = null;

	// Client should be singleton
	// Be careful with instantiating CosmosDBService multiple times as the client is
	// singleton
	// Support of multiple client currently not implemented
	private static CosmosAsyncClient cosmosDBClient = null;

	/**
	 * Constructor.
	 * 
	 * @param cosmosDBConfig The CosmosDBConfig object with related config data
	 */
	public CosmosDBService(final CosmosDBConfig cosmosDBConfig) {
		this.cosmosDBConfig = cosmosDBConfig;
		initializeCosmosDBClientAsync();
	}

	/**
	 * Initializes the CosmosDB client. Implements singleton pattern
	 */
	private void initializeCosmosDBClientAsync() {
		if (cosmosDBClient == null) {
			String endpointUri = cosmosDBConfig.endpointUri;
			String primaryKey = cosmosDBConfig.primaryKey;
			List<String> preferredRegions = cosmosDBConfig.preferredRegions;

			int throttelingRetryMaxCount = cosmosDBConfig.throttelingRetryMaxCount;
			ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
			throttlingRetryOptions.setMaxRetryAttemptsOnThrottledRequests(throttelingRetryMaxCount);

			cosmosDBClient = new CosmosClientBuilder()
					.endpoint(endpointUri)
					.key(primaryKey)
					.preferredRegions(preferredRegions)
					.directMode()
					.throttlingRetryOptions(throttlingRetryOptions)
					.consistencyLevel(this.getClientDefaultConsistencyLevel())
					// Setting content response on write enabled, which enables the SDK to return
					// response on write operations.
					.contentResponseOnWriteEnabled(true)
					.buildAsyncClient();
		} else {
			logger.warn("initializeCosmosDBClient: Client already initialized...");
		}
	}

	/**
	 * Get the Cosmos DB client instance.
	 * 
	 * @return The Cosmos DB client instance
	 */
	public CosmosAsyncClient getCosmosAsyncClient() {
		return cosmosDBClient;
	}

	/**
	 * Translate clientDefaultConsistencyLevel from config.yaml to ConsistencyLevel
	 * object.
	 *
	 * @return The ConsistencyLevel object based on the value of config.yaml
	 */
	private ConsistencyLevel getClientDefaultConsistencyLevel() {
		String configConsistencyLevel = this.cosmosDBConfig.clientDefaultConsistencyLevel;

		switch (configConsistencyLevel) {
		case "Strong":
			return ConsistencyLevel.STRONG;
		case "BoundedStaleness":
			return ConsistencyLevel.BOUNDED_STALENESS;
		case "Session":
			return ConsistencyLevel.SESSION;
		case "ConsistentPrefix":
			return ConsistencyLevel.CONSISTENT_PREFIX;
		case "Eventual":
			return ConsistencyLevel.EVENTUAL;
		default:
			return ConsistencyLevel.SESSION;
		}
	}

	/**
	 * Create a new database if it does not already exist.
	 *
	 * @param databaseName The id of the database
	 * @return The database object
	 */
	public CosmosAsyncDatabase createDatabaseIfNotExistsAsync(final String databaseName) throws Exception {
		logger.info(String.format("createDatabaseIfNotExists: Create database '%s' if not exists...", databaseName));

		// Create database if not exists
		Mono<CosmosDatabaseResponse> databaseIfNotExists = cosmosDBClient.createDatabaseIfNotExists(databaseName);
		CosmosAsyncDatabase database = databaseIfNotExists.flatMap(databaseResponse -> {
			CosmosAsyncDatabase databaseTemp = cosmosDBClient.getDatabase(databaseResponse.getProperties().getId());
			logger.info(String.format("createDatabaseIfNotExists: Checking database '%s' completed...",
					databaseTemp.getId()));
			return Mono.just(databaseTemp);
		}).block();

		return database;
	}

	/**
	 * @param containerName             The id of the container
	 * @param containerPartitionKeyPath The partition key path (e.g.
	 *                                  "/partitionKey")
	 * @param containerThroughput       The number of requests units
	 * @param database                  The database to create the container in
	 * @return The container object
	 * @throws Exception createContainerWithManualThroughputIfNotExists failed
	 */
	public CosmosAsyncContainer createContainerWithManualThroughputIfNotExistsAsync(
			final String containerName,
			final String containerPartitionKeyPath,
			final int containerThroughput,
			final CosmosAsyncDatabase database)
			throws Exception {

		logger.info(String.format("createContainerIfNotExists: Create container '%s' if not exists...", containerName));

		// Create container if not exists
		CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName,
				containerPartitionKeyPath);
		ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(containerThroughput);
		Mono<CosmosContainerResponse> containerIfNotExists = database.createContainerIfNotExists(containerProperties,
				throughputProperties);
		CosmosAsyncContainer container = containerIfNotExists.flatMap(containerResponse -> {
			CosmosAsyncContainer containerTemp = database.getContainer(containerResponse.getProperties().getId());
			logger.info(String.format("createDatabaseIfNotExists: Checking container '%s' completed...",
					containerTemp.getId()));
			return Mono.just(containerTemp);
		}).block();

		return container;
	}

	/**
	 * Get a database.
	 *
	 * @param databaseId The database id
	 * @return The database if it exists or null
	 */
	public CosmosAsyncDatabase getDatabase(final String databaseId) {
		CosmosAsyncDatabase database = cosmosDBClient.getDatabase(databaseId);
		return database;
	}

	/**
	 * Get a container.
	 *
	 * @param database    The database object
	 * @param containerId The container id
	 * @return The container if it exists or null
	 */
	public CosmosAsyncContainer getContainer(final CosmosAsyncDatabase database, final String containerId) {
		CosmosAsyncContainer container = database.getContainer(containerId);
		return container;
	}

	/**
	 * Change the manual throughput of a container.
	 *
	 * @param database                 The database object
	 * @param containerId              The container id
	 * @param targetThroughput         The target throughput
	 * @param waitForScalingToComplete Flag to indicate to wait until the scaling
	 *                                 has finished
	 * @throws InterruptedException Exception that occurs during sleep
	 */
	public void changeThroughputForManualScaleOnContainerAsync(
			final CosmosAsyncDatabase database,
			final String containerId,
			final int targetThroughput,
			final boolean waitForScalingToComplete)
			throws InterruptedException {

		logger.info(String.format(
				"changeThroughputForManualScaleOnContainerAsync: Scaling throughput for container with id '%s'...",
				containerId));

		int secondsToWaitWhilePending = 10;
		CosmosAsyncContainer container = this.getContainer(database, containerId);

		ThroughputResponse initialThroughputResponse = container.readThroughput().block();
		if (initialThroughputResponse.isReplacePending()) {
			logger.warn(
					"changeRequestUnitsOnContainerAsync: Another throughput change is still pending, please try again later...");
		} else {
			logger.info("changeRequestUnitsOnContainerAsync: No other throughput change is pending...");
			Integer currentThroughput = initialThroughputResponse.getProperties().getManualThroughput();
			if (currentThroughput != null) {
				logger.info(String.format(
						"changeThroughputForManualScaleOnContainerAsync: The current throughput is '%s'...",
						currentThroughput));
				if (currentThroughput != targetThroughput) {
					logger.info(String.format("changeThroughputForManualScaleOnContainerAsync: Scaling to '%s'...",
							targetThroughput));
					ThroughputProperties targetThroughputProperties = ThroughputProperties
							.createManualThroughput(targetThroughput);
					ThroughputResponse replaceThroughputResponse = container
							.replaceThroughput(targetThroughputProperties).block();

					boolean replaceIsPending = replaceThroughputResponse.isReplacePending();
					if (replaceIsPending && waitForScalingToComplete) {
						logger.info(
								"changeThroughputForManualScaleOnContainerAsync: Scaling in progress and waiting for it to complete...");
					} else if (replaceIsPending && !waitForScalingToComplete) {
						logger.info(
								"changeThroughputForManualScaleOnContainerAsync: Scaling in progress and but not waiting for it to complete...");
						while (replaceIsPending) {
							replaceThroughputResponse = container.readThroughput().block();
							replaceIsPending = replaceThroughputResponse.isReplacePending();
							TimeUnit.SECONDS.sleep(secondsToWaitWhilePending);
						}
					} else {
						logger.info("changeThroughputForManualScaleOnContainerAsync: Scaling already completed...");
					}
				} else {
					logger.info(String.format(
							"changeThroughputForManualScaleOnContainerAsync: Container has already throughput of '%s'...",
							targetThroughput));
				}
				logger.info(String.format(
						"changeThroughputForManualScaleOnContainerAsync: Scaling throughput for container with id '%s' finished...",
						containerId));
			} else {
				logger.warn("changeThroughputForManualScaleOnContainerAsync: Throughput on container level is null...");
			}
		}

//		Mono<Void> response = container.readThroughput()
//			.flatMap(initialThroughputResponse -> {
//				if (initialThroughputResponse.isReplacePending()) {
//					logger.warn("changeRequestUnitsOnContainerAsync: Another throughput change is still pending, please try again later...");
//					return Mono.empty();
//				} else {
//					logger.info("changeRequestUnitsOnContainerAsync: No other throughput change is pending...");
//					int currentThroughput = initialThroughputResponse.getProperties().getManualThroughput();
//					logger.info(String.format("changeThroughputForManualScaleOnContainerAsync: The current throughput is '%s'...", currentThroughput));
//					if (currentThroughput != targetThroughput) {
//						logger.info(String.format("changeThroughputForManualScaleOnContainerAsync: Scaling to '%s'...", targetThroughput));
//						ThroughputProperties targetThroughputProperties = ThroughputProperties.createManualThroughput(targetThroughput);
//						return container.replaceThroughput(targetThroughputProperties);
//					} else {
//						logger.info(String.format("changeThroughputForManualScaleOnContainerAsync: Container has already throughput of '%s'...", targetThroughput));
//						return Mono.empty();
//					}
//				}
//			})
//			.flatMap(replaceThroughputResponse -> {
//				if (replaceThroughputResponse != null) {
//					boolean replaceIsPending = replaceThroughputResponse.isReplacePending();
//					if (replaceIsPending && waitForScalingToComplete) {
//						logger.info("changeThroughputForManualScaleOnContainerAsync: Scaling in progress and waiting for it to complete...");
//						// TODO: Handle wait for completion
//						container.readThroughput().flatMap(replaceThroughputCheckResponse -> {
//							return Mono.just(replaceThroughputCheckResponse);
//						});
//						return Mono.empty();
//					} else if (replaceIsPending && !waitForScalingToComplete) {
//						logger.info("changeThroughputForManualScaleOnContainerAsync: Scaling in progress and but not waiting for it to complete...");
//						return Mono.empty();
//					} else {
//						logger.info("changeThroughputForManualScaleOnContainerAsync: Scaling already completed...");
//						return Mono.empty();
//					}
//				} else {
//					return Mono.empty();
//				}
//			});
//		
//		return response;
	}

	/**
	 * Update indexing policy on container.
	 * 
	 * @param database       The database object
	 * @param containerId    The container id
	 * @param indexingPolicy The indexing policy definition
	 * @return The container response
	 */
	public Mono<CosmosContainerResponse> changeIndexingPolicyAsync(
			final CosmosAsyncDatabase database,
			final String containerId,
			final IndexingPolicy indexingPolicy) {

		logger.info("changeIndexingPolicy: Updating indexing policy...");

		CosmosAsyncContainer container = this.getContainer(database, containerId);
		return container.read().flatMap(containerResponse -> {
			CosmosContainerProperties containerProperties = containerResponse.getProperties();
			containerProperties.setIndexingPolicy(indexingPolicy);
			return container.replace(containerProperties);
		});
	}

	/**
	 * Create one or more items in a container.
	 * 
	 * @param <T>       Type of the items to be created
	 * @param container The container
	 * @param itemsList The list of items to be created
	 * @return List of responses of the creation of the items
	 */
	public <T> Mono<List<CosmosItemResponse<T>>> createItemsAsync(
			final CosmosAsyncContainer container,
			final ArrayList<T> itemsList) {
		Flux<T> interactionsFlux = Flux.fromIterable(itemsList);
		Mono<List<CosmosItemResponse<T>>> results = interactionsFlux
				.flatMap(interaction -> container.createItem(interaction)).doOnNext(response -> {
					logger.info("createItemsAsync: The create item operation cost {} RUs...",
							response.getRequestCharge());
				}).doOnError(error -> {
					logger.error("createItemsAsync: Error...", error);
				}).collectList();

		return results;
	}

	/**
	 * @param <T>          Type of the item to retrieve
	 * @param container    The Container instance
	 * @param itemId       The id of the item to retrieve
	 * @param partitionKey The partition key value
	 * @param classType    The class type of the item to retrieve
	 * @return The item retrieved
	 */
	public <T> Mono<T> executePointReadAsync(
			final CosmosAsyncContainer container,
			final String itemId,
			final String partitionKey,
			final Class<T> classType) {

		Mono<T> response = container.readItem(itemId, new PartitionKey(partitionKey), classType)
				.flatMap(requestResponse -> {
					logger.info("executePointReadAsync: Read item cost {} RUs...", requestResponse.getRequestCharge());
					logger.info("executePointReadAsync: ETag value is '{}'...", requestResponse.getETag());
					T item = requestResponse.getItem();
					return Mono.just(item);
				});

		return response;
	}

	/**
	 * @param <T>          Type of the items to retrieve
	 * @param container    The Container instance
	 * @param queryOptions The query options related to the query
	 * @param sqlQuery     The SQL query
	 * @param classType    The class type of the item to retrieve
	 * @return List of returned items
	 */
	public <T> List<T> executeSQLQueryAndGetAllItems(
			final CosmosAsyncContainer container,
			final CosmosQueryRequestOptions queryOptions,
			final String sqlQuery,
			final Class<T> classType) {

		List<T> receivedItems = new ArrayList<T>();

		container.queryItems(sqlQuery, queryOptions, classType).byPage().flatMap(page -> {
			logger.info("executeSQLQueryAndGetAllItems: Query for page cost {} RUs...", page.getRequestCharge());
			List<T> pageResults = page.getResults();
			receivedItems.addAll(pageResults);
			return Mono.empty();
		}).blockLast();

		return receivedItems;
	}

	/**
	 * @param <T>          Type of the items to retrieve
	 * @param container    The Container instance
	 * @param queryOptions The query options related to the query
	 * @param sqlQuery     The SQL query
	 * @param classType    The class type of the item to retrieve
	 * @return Flux with promised FeedResponse object
	 */
	public <T> Flux<FeedResponse<T>> executeSQLQueryAsync(final CosmosAsyncContainer container,
			final CosmosQueryRequestOptions queryOptions, final String sqlQuery, final Class<T> classType) {

		Flux<FeedResponse<T>> response = container.queryItems(sqlQuery, queryOptions, classType).byPage();
		return response;
	}

	/**
	 * Execute a Stored Procedure.
	 * 
	 * @param container The Container id
	 * @param sprocName The Stored Procedure name
	 * @param options   The CosmosStoredProcedureResponse object
	 * @param sprocArgs The arguments passed to the Stored Procedure
	 * @return The response of the execution request
	 */
	public Mono<CosmosStoredProcedureResponse> executeStoredProcedureAsync(
			final CosmosAsyncContainer container,
			final String sprocName,
			CosmosStoredProcedureRequestOptions options,
			List<Object> sprocArgs) {

		Mono<CosmosStoredProcedureResponse> response = container.getScripts().getStoredProcedure(sprocName)
				.execute(sprocArgs, options);

		return response;
	}

	/**
	 * Upsert an item.
	 * 
	 * @param <T>          Type of the item to upsert
	 * @param container    The container to upsert to
	 * @param itemToUpsert The item to upsert
	 * @param enableOCC    Set to true when you want to enable ETag check
	 *                     (Optimistic Concurrency Control)
	 * @return The response of the upsert operation
	 */
	public <T extends BaseModel> Mono<CosmosItemResponse<T>> upsertItem(
			final CosmosAsyncContainer container,
			final T itemToUpsert,
			final boolean enableOCC) {
		CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
		if (enableOCC) {
			requestOptions.setIfMatchETag(itemToUpsert.getETag());
		}
		Mono<CosmosItemResponse<T>> response = container.upsertItem(itemToUpsert, requestOptions);
		return response;
	}
}
