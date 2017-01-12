<html>
<head>
<title><#if page.titleId??>${msg(page.titleId)}<#else>${page.title!}</#if></title>
<style>
iframe {
	border: none;
	height: 200px;
}
iframe:hover {
	outline: 1px solid lightgray;
}
</style>
</head>
<body>

    <h2>Repository Tools</h2>
    
    <form action="/alfresco/service/index" method="post" target="repo-webscripts">
        <input type="hidden" name="reset" value="on" />
        <input type="submit" name="submit" value="Refresh Web Scripts" />
    </form>
    <iframe name="repo-webscripts" width="500px"></iframe>
    <iframe name="repo-js-debugger" src="/alfresco/service/api/javascript/debugger"></iframe>
    <iframe name="repo-modules" src="/alfresco/service/modules/info.html" width="500px"></iframe>

    <h2>Share Tools</h2>
    
    <form action="${url.context}/page/index" method="post" target="share-webscripts">
        <input type="hidden" name="reset" value="on" />
        <input type="submit" name="submit" value="Refresh Web Scripts" />
    </form>
    <iframe name="share-webscripts" width="500px"></iframe>
    <iframe name="share-js-debugger" src="${url.context}/page/api/javascript/debugger"></iframe>
    <iframe name="share-modules" src="${url.context}/page/modules/info.html" width="500px"></iframe>
    
    <form action="${url.context}/page/caches/dependency/clear" method="post" target="share-caches">
        <input type="submit" name="submit" value="Clear Dependency Caches" />
    </form>
    <iframe name="share-caches"></iframe>
    
    <iframe name="share-surfbug" src="${url.context}/page/surfBugStatus"></iframe>

</body>
</html>