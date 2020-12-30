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