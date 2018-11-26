
function main() {

    var regions = services.get('ecos.cardlets.cardletsRegistry').getRegisteredRegions();

    var it = regions.iterator();
    var resultRegions = [];

    while (it.hasNext()) {
        var region = it.next();
        resultRegions.push({
            'region-id': region
        })
    }

    model.result = {
        regions: resultRegions
    };
}

main();
