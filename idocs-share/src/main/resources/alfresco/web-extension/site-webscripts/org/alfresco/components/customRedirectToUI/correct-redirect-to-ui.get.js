(function () {
    var result = remote.call("/citeck/ecos/custom-url-for-redirect-to-ui");

    if (result.status == 200 && result != "{}") {
        status.location = eval('(' + result + ')').url;
    } else {
        status.location = "/share/page";
    }
    status.code = 303;
})();
