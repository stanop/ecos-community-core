<import resource="classpath:alfresco/module/idocs-repo/scripts/completeness-levels.lib.js">

createOrUpdateLevels([
    {
        uuid: "cl-scan-documents",
        name: "scan-documents",
        titles: [
            {locale: "en", value: "Scan documents"},
            {
                locale: "ru",
                value: "\u0414\u043E\u043A\u0443\u043C\u0435\u043D\u0442\u044B \u0441\u043A\u0430\u043D\u0438\u0440\u043E\u0432\u0430\u043D\u0438\u044F"
            }
        ],
        req: [
            {
                name: "scan-documents",
                titles: [
                    {locale: "en", value: "Scan documents"},
                    {
                        locale: "ru",
                        value: "\u0414\u043E\u043A\u0443\u043C\u0435\u043D\u0442\u044B \u0441\u043A\u0430\u043D\u0438\u0440\u043E\u0432\u0430\u043D\u0438\u044F"
                    }
                ],
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/category-document-type", requiredKind: "workspace://SpacesStore/kind-d-scan-documents"}
            }
        ]
    },
    {
        uuid: "cl-counterparty-docs",
        name: "counterparty-documents",
        titles: [
            {locale: "en", value: "Counterparty documents"},
            {
                locale: "ru",
                value: "\u0414\u043E\u043A\u0443\u043C\u0435\u043D\u0442\u044B \u043A\u043E\u043D\u0442\u0440\u0430\u0433\u0435\u043D\u0442\u0430"
            }
        ],
        req: [
            {
                name: "counterparty-documents",
                titles: [
                    {locale: "en", value: "Counterparty documents"},
                    {
                        locale: "ru",
                        value: "\u0414\u043E\u043A\u0443\u043C\u0435\u043D\u0442\u044B \u043A\u043E\u043D\u0442\u0440\u0430\u0433\u0435\u043D\u0442\u0430"
                    }
                ],
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/category-document-type", requiredKind: "workspace://SpacesStore/kind-d-counterparty-documents"}
            }
        ]
    },
    {
        uuid: "cl-other-docs",
        name: "other-documents",
        titles: [
            {locale: "en", value: "Other documents"},
            {
                locale: "ru",
                value: "\u041F\u0440\u043E\u0447\u0435\u0435"
            }
        ],
        req: [
            {
                name: "other-documents",
                titles: [
                    {locale: "en", value: "Other documents"},
                    {
                        locale: "ru",
                        value: "\u041F\u0440\u043E\u0447\u0435\u0435"
                    }
                ],
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/category-document-type", requiredKind: "workspace://SpacesStore/cat-document-other"}
            }
        ]
    }
]);