const Dependencies = {

    getByPage: function (pageId, type) {

        var result = {
            js: [],
            css: []
        };

        var pageConfig = config.scoped["CiteckPage"][pageId];
        if (!pageConfig) {
            return result;
        }
        var pageDependencies = pageConfig.getChild("dependencies");
        if (!pageDependencies) {
            return result;
        }
        var typedDependencies;
        if (type) {
            typedDependencies = pageDependencies.getChildren(type);
        } else {
            typedDependencies = pageDependencies.getChildren();
        }
        if (typedDependencies) {
            for (var i = 0; i < typedDependencies.size(); i++) {
                var element = typedDependencies.get(i);
                var depsArr = result[element.getName()];
                if (!depsArr) {
                    depsArr = [];
                    result[element.getName()] = depsArr;
                }
                var src = element.getAttribute("src");
                if (src) {
                    depsArr.push(src);
                }
            }
        }
        return result;
    }
};