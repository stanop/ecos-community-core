model.journals = search.query({
    query: 'TYPE:"journal:journal"',
    language: 'fts-alfresco'
});
cache.maxAge = 600;