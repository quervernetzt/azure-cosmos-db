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