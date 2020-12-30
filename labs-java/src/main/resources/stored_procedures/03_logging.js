function createDocumentWithLogging(doc) {
    console.log("procedural-start ");
    var context = getContext();
    var container = context.getCollection();
    console.log("metadata-retrieved ");
    var accepted = container.createDocument(
        container.getSelfLink(),
        doc,
        function (err, newDoc) {
            console.log("callback-started ");
            if (err) throw new Error('Error' + err.message);
            context.getResponse().setBody(newDoc.id);
        }
    );
    console.log("async-doc-creation-started ");
    if (!accepted) return;
    console.log("procedural-end");
}