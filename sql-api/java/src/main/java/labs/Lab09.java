package labs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;

import labs.Models.Family;
import labs.Models.Member;
import labs.Models.Person;
import labs.Models.Transaction;
import reactor.core.publisher.Mono;
import services.CosmosDBService;

public class Lab09 {
	private static final Logger logger = LoggerFactory.getLogger(Lab09.class);
	private CosmosDBService cosmosDBService;

	/**
	 * Constructor
	 * 
	 * @param cosmosDBService The Cosmos DB service instance
	 * @throws Exception All possible exceptions that can be thrown
	 */
	public Lab09(final CosmosDBService cosmosDBService) throws Exception {
		this.cosmosDBService = cosmosDBService;
		String databaseId = "FinancialDatabase";
		String containerPeopleId = "PeopleCollection";
		String containerTransactionId = "TransactionCollection";

		// Get database and container
		CosmosAsyncDatabase targetDatabase = this.cosmosDBService.getDatabase(databaseId);
		CosmosAsyncContainer peopleContainer = this.cosmosDBService.getContainer(targetDatabase, containerPeopleId);
		CosmosAsyncContainer transactionContainer = this.cosmosDBService.getContainer(targetDatabase,
				containerTransactionId);

		// ------------------------------------------
		// Observe RU Charge
		// Small item
		boolean enableCreateSmallItem = false;
		if (enableCreateSmallItem) {
			ArrayList<Person> personList = new ArrayList<Person>();
			personList.add(new Person());
			this.cosmosDBService.createItemsAsync(peopleContainer, personList).block();
		}

		// Large item
		boolean enableCreateLargeItem = false;
		if (enableCreateLargeItem) {
			List<Person> children = new ArrayList<Person>();
			for (int i = 0; i < 4; i++)
				children.add(new Person());
			ArrayList<Member> memberList = new ArrayList<Member>();
			memberList.add(new Member(UUID.randomUUID().toString(), new Person(), // accountHolder
					new Family(new Person(), // spouse
							children))); // children
			this.cosmosDBService.createItemsAsync(peopleContainer, memberList).block();
		}

		// Observing Throttling (HTTP 429)
		boolean enableObserveThrottling = false;
		if (enableObserveThrottling) {
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			for (int i = 0; i < 1000; i++) {
				transactions.add(new Transaction());
			}
			this.cosmosDBService.createItemsAsync(transactionContainer, transactions).block();
		}

		// ------------------------------------------
		// Tuning Queries and Reads
		// Measuring RU Charge
		boolean enableTuningMeasureRUCharge = false;
		if (enableTuningMeasureRUCharge) {
			String sqlQuery = "SELECT TOP 1000 * FROM c WHERE c.processed = true ORDER BY c.amount DESC";
			CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
			this.cosmosDBService.executeSQLQueryAsync(transactionContainer, requestOptions, sqlQuery, Transaction.class)
					.next().flatMap(page -> {
						logger.info("Lab09: Request Charge for order query: {} RUs...", page.getRequestCharge());
						return Mono.empty();
					}).block();

			sqlQuery = "SELECT * FROM c WHERE c.processed = true";
			requestOptions = new CosmosQueryRequestOptions();
			this.cosmosDBService.executeSQLQueryAsync(transactionContainer, requestOptions, sqlQuery, Transaction.class)
					.next().flatMap(page -> {
						logger.info("Lab09: Request Charge for query without order: {} RUs...",
								page.getRequestCharge());
						return Mono.empty();
					}).block();

			sqlQuery = "SELECT * FROM c ";
			requestOptions = new CosmosQueryRequestOptions();
			this.cosmosDBService.executeSQLQueryAsync(transactionContainer, requestOptions, sqlQuery, Transaction.class)
					.next().flatMap(page -> {
						logger.info("Lab09: Request Charge for query without filter: {} RUs...",
								page.getRequestCharge());
						return Mono.empty();
					}).block();
		}

		// Managing SDK Query Options
		boolean enableTuningQueryOptions = false;
		if (enableTuningQueryOptions) {
			int maxDegreeOfParallelism = 1;
			int maxBufferedItemCount = 0;
			CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
			requestOptions.setMaxBufferedItemCount(maxDegreeOfParallelism);
			requestOptions.setMaxDegreeOfParallelism(maxBufferedItemCount);

			logger.info("\n\n" + "MaxDegreeOfParallelism:\t{}\n" + "MaxBufferedItemCount:\t{}" + "\n\n",
					maxDegreeOfParallelism, maxBufferedItemCount);

			String sqlQuery = "SELECT * FROM c WHERE c.processed = true ORDER BY c.amount DESC";

			StopWatch timer = StopWatch.createStarted();
			this.cosmosDBService.executeSQLQueryAndGetAllItems(transactionContainer, requestOptions, sqlQuery,
					Transaction.class);
			timer.stop();
			logger.info("\n\nLab09: Elapsed Time:\t{}s\n\n", ((double) timer.getTime(TimeUnit.MILLISECONDS)) / 1000.0);

			// **********************************

			maxDegreeOfParallelism = 5;
			maxBufferedItemCount = 0;
			requestOptions = new CosmosQueryRequestOptions();
			requestOptions.setMaxBufferedItemCount(maxDegreeOfParallelism);
			requestOptions.setMaxDegreeOfParallelism(maxBufferedItemCount);

			logger.info("\n\n" + "MaxDegreeOfParallelism:\t{}\n" + "MaxBufferedItemCount:\t{}" + "\n\n",
					maxDegreeOfParallelism, maxBufferedItemCount);

			sqlQuery = "SELECT * FROM c WHERE c.processed = true ORDER BY c.amount DESC";

			timer = StopWatch.createStarted();
			this.cosmosDBService.executeSQLQueryAndGetAllItems(transactionContainer, requestOptions, sqlQuery,
					Transaction.class);
			timer.stop();
			logger.info("\n\nLab09: Elapsed Time:\t{}s\n\n", ((double) timer.getTime(TimeUnit.MILLISECONDS)) / 1000.0);

			// **********************************

			maxDegreeOfParallelism = 5;
			maxBufferedItemCount = -1;
			requestOptions = new CosmosQueryRequestOptions();
			requestOptions.setMaxBufferedItemCount(maxDegreeOfParallelism);
			requestOptions.setMaxDegreeOfParallelism(maxBufferedItemCount);

			logger.info("\n\n" + "MaxDegreeOfParallelism:\t{}\n" + "MaxBufferedItemCount:\t{}" + "\n\n",
					maxDegreeOfParallelism, maxBufferedItemCount);

			sqlQuery = "SELECT * FROM c WHERE c.processed = true ORDER BY c.amount DESC";

			timer = StopWatch.createStarted();
			this.cosmosDBService.executeSQLQueryAndGetAllItems(transactionContainer, requestOptions, sqlQuery,
					Transaction.class);
			timer.stop();
			logger.info("\n\nLab09: Elapsed Time:\t{}s\n\n", ((double) timer.getTime(TimeUnit.MILLISECONDS)) / 1000.0);

			// **********************************

			maxDegreeOfParallelism = -1;
			maxBufferedItemCount = -1;
			requestOptions = new CosmosQueryRequestOptions();
			requestOptions.setMaxBufferedItemCount(maxDegreeOfParallelism);
			requestOptions.setMaxDegreeOfParallelism(maxBufferedItemCount);

			logger.info("\n\n" + "MaxDegreeOfParallelism:\t{}\n" + "MaxBufferedItemCount:\t{}" + "\n\n",
					maxDegreeOfParallelism, maxBufferedItemCount);

			sqlQuery = "SELECT * FROM c WHERE c.processed = true ORDER BY c.amount DESC";

			timer = StopWatch.createStarted();
			this.cosmosDBService.executeSQLQueryAndGetAllItems(transactionContainer, requestOptions, sqlQuery,
					Transaction.class);
			timer.stop();
			logger.info("\n\nLab09: Elapsed Time:\t{}s\n\n", ((double) timer.getTime(TimeUnit.MILLISECONDS)) / 1000.0);

			// **********************************

			maxDegreeOfParallelism = -1;
			maxBufferedItemCount = 50000;
			requestOptions = new CosmosQueryRequestOptions();
			requestOptions.setMaxBufferedItemCount(maxDegreeOfParallelism);
			requestOptions.setMaxDegreeOfParallelism(maxBufferedItemCount);

			logger.info("\n\n" + "MaxDegreeOfParallelism:\t{}\n" + "MaxBufferedItemCount:\t{}" + "\n\n",
					maxDegreeOfParallelism, maxBufferedItemCount);

			sqlQuery = "SELECT * FROM c WHERE c.processed = true ORDER BY c.amount DESC";

			timer = StopWatch.createStarted();
			this.cosmosDBService.executeSQLQueryAndGetAllItems(transactionContainer, requestOptions, sqlQuery,
					Transaction.class);
			timer.stop();
			logger.info("\n\nLab09: Elapsed Time:\t{}s\n\n", ((double) timer.getTime(TimeUnit.MILLISECONDS)) / 1000.0);
		}

		// ------------------------------------------
		// Reading and Querying Items
		boolean enableTuningReadAndQuery = false;
		if (enableTuningReadAndQuery) {
			CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
			String itemId = "7b529ddf-dcab-42b1-ba05-2718b4c1dae9";
			String partitionKeyValue = "Lake Tomas";

			String sqlQuery = String.format("SELECT TOP 1 * FROM c WHERE c.costCenter = '%s' AND c.id = '%s'",
					partitionKeyValue, itemId);

			StopWatch timer = StopWatch.createStarted();
			this.cosmosDBService.executeSQLQueryAndGetAllItems(transactionContainer, requestOptions, sqlQuery,
					Transaction.class);
			timer.stop();
			logger.info("\n\nLab09: Elapsed Time:\t{}s\n\n", ((double) timer.getTime(TimeUnit.MILLISECONDS)) / 1000.0);

			timer = StopWatch.createStarted();
			this.cosmosDBService
					.executePointReadAsync(transactionContainer, itemId, partitionKeyValue, Transaction.class).block();
			timer.stop();
			logger.info("\n\nLab09: Elapsed Time:\t{}s\n\n", ((double) timer.getTime(TimeUnit.MILLISECONDS)) / 1000.0);
		}

		// ------------------------------------------
		// Setting Throughput for Expected Workloads
		// Estimating Throughput Needs
		boolean enableTuningSetThroughputEstimation = false;
		if (enableTuningSetThroughputEstimation) {
			int expectedWritesPerSec = 200;
			int expectedReadsPerSec = 800;

			ArrayList<Member> memberList = new ArrayList<Member>();
			memberList.add(new Member());
			List<CosmosItemResponse<Member>> response = this.cosmosDBService
					.createItemsAsync(peopleContainer, memberList).block();
			double charge = response.get(0).getRequestCharge();

			logger.info("\n\nLab09: Estimated load: {} RU per sec\n\n",
					charge * expectedReadsPerSec + charge * expectedWritesPerSec);
		}

		// Adjusting for Usage Patterns
		boolean enableTuningSetThroughput = true;
		if (enableTuningSetThroughput) {
			int baseThroughput = 400;
			int targetThroughput = 1000;

			this.cosmosDBService.changeThroughputForManualScaleOnContainerAsync(targetDatabase, containerPeopleId,
					targetThroughput, true);
			logger.info("Lab09: Changed throughput to {}...", targetThroughput);

			this.cosmosDBService.changeThroughputForManualScaleOnContainerAsync(targetDatabase, containerPeopleId,
					baseThroughput, true);
			logger.info("Lab09: Changed throughput to {}...", baseThroughput);
		}
	}
}
