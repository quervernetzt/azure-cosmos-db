function greetCaller(name) {
    var context = getContext();
    var response = context.getResponse();
    response.setBody("Hello " + name);
}

// -------------------------------------------------

function createDocument(doc) {
    var context = getContext();
    var container = context.getCollection();
    var accepted = container.createDocument(
        container.getSelfLink(),
        doc,
        function (err, newItem) {
            if (err) throw new Error('Error' + err.message);
            context.getResponse().setBody(newItem);
        }
    );
    if (!accepted) return;
}

// -------------------------------------------------

function createDocumentWithLogging(doc) {
    console.log("procedural-start");
    var context = getContext();
    var container = context.getCollection();
    console.log("metadata-retrieved");
    var accepted = container.createDocument(
        container.getSelfLink(),
        doc,
        function (err, newDoc) {
            console.log("callback-started");
            if (err) throw new Error('Error' + err.message);
            context.getResponse().setBody(newDoc.id);
        }
    );
    console.log("async-doc-creation-started");
    if (!accepted) return;
    console.log("procedural-end");
}

// -------------------------------------------------

function createDocumentWithFunction(document) {
    var context = getContext();
    var container = context.getCollection();
    if (!container.createDocument(container.getSelfLink(), document, itemCreated))
        return;
    function itemCreated(error, newItem) {
        if (error) throw new Error('Error' + error.message);
        context.getResponse().setBody(newItem);
    }
}

// -------------------------------------------------

function createTwoDocuments(foodGroupName, foodDescription, mealName) {
    var context = getContext();
    var container = context.getCollection();
    var firstItem = {
        foodGroup: foodGroupName,
        description: foodDescription
    };
    var secondItem = {
        foodGroup: foodGroupName,
        eaten: {
            meal: mealName
        }
    };
    var firstAccepted = container.createDocument(container.getSelfLink(), firstItem,
        function (firstError, newFirstItem) {
            if (firstError) throw new Error('Error' + firstError.message);
            var secondAccepted = container.createDocument(container.getSelfLink(), secondItem,
                function (secondError, newSecondItem) {
                    if (secondError) throw new Error('Error' + secondError.message);
                    context.getResponse().setBody({
                        foodRecord: newFirstItem,
                        mealRecord: newSecondItem
                    });
                }
            );
            if (!secondAccepted) return;
        }
    );
    if (!firstAccepted) return;
}

// -------------------------------------------------

function createTwoDocuments(foodGroupName, foodDescription, mealName) {
    var context = getContext();
    var container = context.getCollection();
    var firstItem = {
        foodGroup: foodGroupName,
        description: foodDescription
    };
    var secondItem = {
        foodGroup: foodGroupName + "_meal",
        eaten: {
            meal: mealName
        }
    };
    var firstAccepted = container.createDocument(container.getSelfLink(), firstItem,
        function (firstError, newFirstItem) {
            if (firstError) throw new Error('Error' + firstError.message);
            console.log('Created: ' + newFirstItem.id);
            var secondAccepted = container.createDocument(container.getSelfLink(), secondItem,
                function (secondError, newSecondItem) {
                    if (secondError) throw new Error('Error' + secondError.message);
                    console.log('Created: ' + newSecondItem.id);
                    context.getResponse().setBody({
                        foodRecord: newFirstItem,
                        mealRecord: newSecondItem
                    });
                }
            );
            if (!secondAccepted) return;
        }
    );
    if (!firstAccepted) return;
}