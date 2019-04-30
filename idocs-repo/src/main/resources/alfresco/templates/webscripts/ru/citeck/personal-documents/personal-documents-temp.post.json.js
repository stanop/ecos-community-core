(function() {
    var tempDir = personalDocuments.ensureTempDirectory();
    model.nodeRef = tempDir ? tempDir.nodeRef.toString() : null;
})();