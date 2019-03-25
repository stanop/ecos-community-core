export function fetchAllDocumentsNodeRefs(parentNodeRef) {
    return fetch('/share/proxy/alfresco/citeck/ecos/records/query', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json;charset=UTF-8'
        },
        body: JSON.stringify({
            query: {
                language: 'children',
                query: {
                    parent: parentNodeRef,
                    assocName: 'icase:documents',
                },
            }
        })
    })
        .then(resp => resp.json())
        .then(resp => resp.records);
}

export function getFolderNodeRef(userRef) {
    return fetch('/share/proxy/alfresco/citeck/ecos/records/query', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json;charset=UTF-8'
        },
        body: JSON.stringify({
            record: userRef,
            attributes: {
                docs: '.att(n:"org:personalDocuments"){id}'
            }
        })
    })
        .then(resp => resp.json())
        .then(resp => {
            if (resp.attributes && resp.attributes.docs) {
                return resp.attributes.docs;
            }

            return null;
        });
}