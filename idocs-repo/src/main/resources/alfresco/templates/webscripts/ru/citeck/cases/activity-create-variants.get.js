
function main() {
    var variants = caseActivityService.getCreateVariants();
    model.result = formatVariants(variants);
}

function formatVariants(variants) {
    var result = [];
    for (var i = 0; i < variants.size(); i++) {
        var variant = variants.get(i);
        result.push({
            id: variant.id,
            type: variant.prefixedType,
            title: variant.title,
            formId: variant.formId,
            viewParams: variant.viewParams,
            canBeCreated: variant.canBeCreated,
            children: formatVariants(variant.children)
        });
    }
    return result;
}

main();