# General

- Code for labs [1] structured in a more reusable way

- Tested for JavaSE-14 and

```
<dependency>
	<groupId>com.azure</groupId>
	<artifactId>azure-cosmos</artifactId>
	<version>4.3.0</version>
</dependency>
```

- Exception handling is not implemented respectively only in a rudimentary way

- No Unit Tests are added so far


# Prerequisites

- IDE
    - Example: Eclipse IDE for Java Developers, Extensions: YAML Editor, Checkstyle Plugin


# Getting Started

- Go to `src/main/resources/config.yaml` and update the config data

- Then go to `src/main/java/labs/Main.java` and run whichever lab you want to run



# Resources

[1] [Labs](https://github.com/AzureCosmosDB/labs)

[2] [Azure Cosmos DB Java SDK v4 Landing Page](https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-sdk-java-v4)

[3] [Azure Cosmos DB Java SDK v4 ASYNC examples](https://github.com/Azure-Samples/azure-cosmos-java-sql-api-samples/blob/master/src/main/java/com/azure/cosmos/examples/crudquickstart/async/SampleCRUDQuickstartAsync.java)

[4] [Azure Cosmos DB Java SDK v4 repository](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/cosmos/azure-cosmos)

[5] [Azure CosmosDB Client Library for Java - Version 4.3.0](https://docs.microsoft.com/en-us/java/api/overview/azure/cosmos-readme?view=azure-java-stable)

[6] [Reactor 3 Reference Guide](https://projectreactor.io/docs/core/release/reference/)