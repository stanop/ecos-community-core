const Dependencies = {

    getScoped: function (path) {

        var result = {
            js: [],
            css: [],
            dojo: []
        };

        var tokens = path.split("/");

        if (tokens.length < 2) {
            throw "Incorrect tokens count. Expected at least 2. Path: " + path;
        }

        var root = config.scoped[tokens[0]];
        if (!root) {
            return result;
        }
        root = root[tokens[1]];
        if (!root) {
            return result;
        }
        var i;
        for (i = 2; i < tokens.length; i++) {
            root = root.getChild(tokens[i]);
            if (!root) {
                return result;
            }
        }

        var children = root.getChildren();
        for (i = 0; i < children.size(); i++) {
            var element = children.get(i);
            var depsArr = result[element.getName()];
            if (!depsArr) {
                depsArr = [];
                result[element.getName()] = depsArr;
            }
            var src = element.getAttribute("src");
            if (src) {
                if (src.startsWith('/')) {
                    src = url.context + '/res' + src;
                } else if (src.endsWith('.js') || src.endsWith('.css')) {
                    src = url.context + "/res/" + src;
                }
                if (element.name == 'js') {
                    result.dojo.push(src);
                } else if (element.name == 'css') {
                    result.dojo.push("xstyle!" + src);
                }
                depsArr.push(src);
            }
        }
        return result;
    }
};
