export function getPhotoSize(userNodeRef) {
    const url = Alfresco.constants.PROXY_URI + "/citeck/node?nodeRef=" + userNodeRef + "&props=ecos:photo";
    const request = new XMLHttpRequest();
    request.open('GET', url, false);
    request.send(null);

    if (request.status === 200 && request.responseText) {
        const data = JSON.parse(request.responseText);
        return data.props["ecos:photo"].size;
    }

    return 0;
}