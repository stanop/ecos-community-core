<import resource="classpath:alfresco/module/idocs-repo/scripts/completeness-levels.lib.js">

createOrUpdateLevels([
    {
        uuid: "cl-questionnaire",
        name: "questionnaire",
        titles: [
            {locale: "en", value: "Questionnaire"},
            {
                locale: "ru",
                value: "\u0410\u043d\u043a\u0435\u0442\u0430"
            }
        ],
        req: [
            {
                name: "questionnaire",
                titles: [
                    {locale: "en", value: "Questionnaire"},
                    {
                        locale: "ru",
                        value: "\u0410\u043d\u043a\u0435\u0442\u0430"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-questionnaire"}
            }
        ]
    },
    {
        uuid: "cl-cv",
        name: "cv",
        titles: [
            {locale: "en", value: "CV"},
            {
                locale: "ru",
                value: "\u0420\u0435\u0437\u044e\u043c\u0435"
            }
        ],
        req: [
            {
                name: "cv",
                titles: [
                    {locale: "en", value: "CV"},
                    {
                        locale: "ru",
                        value: "\u0420\u0435\u0437\u044e\u043c\u0435"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-cv"}
            }
        ]
    },
    {
        uuid: "cl-recommendations",
        name: "recommendations",
        titles: [
            {locale: "en", value: "Recommendations and Characteristics"},
            {
                locale: "ru",
                value: "\u0420\u0435\u043a\u043e\u043c\u0435\u043d\u0434\u0430\u0446\u0438\u0438 \u0438 \u0445\u0430\u0440\u0430\u043a\u0442\u0435\u0440\u0438\u0441\u0442\u0438\u043a\u0438"
            }
        ],
        req: [
            {
                name: "recommendations",
                titles: [
                    {locale: "en", value: "Recommendations and Characteristics"},
                    {
                        locale: "ru",
                        value: "\u0420\u0435\u043a\u043e\u043c\u0435\u043d\u0434\u0430\u0446\u0438\u0438 \u0438 \u0445\u0430\u0440\u0430\u043a\u0442\u0435\u0440\u0438\u0441\u0442\u0438\u043a\u0438"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-recommendations"}
            }
        ]
    },
    {
        uuid: "cl-conviction-cert",
        name: "conviction-cert",
        titles: [
            {locale: "en", value: "Certificate of conviction"},
            {
                locale: "ru",
                value: "\u0421\u043f\u0440\u0430\u0432\u043a\u0430 \u043e \u043d\u0430\u043b\u0438\u0447\u0438\u0438 \u0028\u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0438\u0438\u0029 \u0441\u0443\u0434\u0438\u043c\u043e\u0441\u0442\u0438"
            }
        ],
        req: [
            {
                name: "conviction-cert",
                titles: [
                    {locale: "en", value: "Certificate of conviction"},
                    {
                        locale: "ru",
                        value: "\u0421\u043f\u0440\u0430\u0432\u043a\u0430 \u043e \u043d\u0430\u043b\u0438\u0447\u0438\u0438 \u0028\u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0438\u0438\u0029 \u0441\u0443\u0434\u0438\u043c\u043e\u0441\u0442\u0438"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-conviction-cert"}
            }
        ]
    },
    {
        uuid: "cl-health-cert",
        name: "health-cert",
        titles: [
            {locale: "en", value: "Health certificate"},
            {
                locale: "ru",
                value: "\u041c\u0435\u0434\u0438\u0446\u0438\u043d\u0441\u043a\u0430\u044f \u0441\u043f\u0440\u0430\u0432\u043a\u0430"
            }
        ],
        req: [
            {
                name: "health-cert",
                titles: [
                    {locale: "en", value: "Health certificate"},
                    {
                        locale: "ru",
                        value: "\u041c\u0435\u0434\u0438\u0446\u0438\u043d\u0441\u043a\u0430\u044f \u0441\u043f\u0440\u0430\u0432\u043a\u0430"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-health-cert"}
            }
        ]
    },
    {
        uuid: "cl-marriage-cert",
        name: "marriage-cert",
        titles: [
            {locale: "en", value: "Marriage certificate"},
            {
                locale: "ru",
                value: "\u0421\u0432\u0438\u0434\u0435\u0442\u0435\u043b\u044c\u0441\u0442\u0432\u043e \u043e \u0431\u0440\u0430\u043a\u0435 \u0028\u0440\u0430\u0437\u0432\u043e\u0434\u0435\u0029"
            }
        ],
        req: [
            {
                name: "marriage-cert",
                titles: [
                    {locale: "en", value: "Marriage certificate"},
                    {
                        locale: "ru",
                        value: "\u0421\u0432\u0438\u0434\u0435\u0442\u0435\u043b\u044c\u0441\u0442\u0432\u043e \u043e \u0431\u0440\u0430\u043a\u0435 \u0028\u0440\u0430\u0437\u0432\u043e\u0434\u0435\u0029"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-marriage-cert"}
            }
        ]
    },
    {
        uuid: "cl-birth-cert",
        name: "birth-cert",
        titles: [
            {locale: "en", value: "Certificate of birth"},
            {
                locale: "ru",
                value: "\u0421\u0432\u0438\u0434\u0435\u0442\u0435\u043b\u044c\u0441\u0442\u0432\u043e \u043e \u0440\u043e\u0436\u0434\u0435\u043d\u0438\u0438 \u0434\u0435\u0442\u0435\u0439"
            }
        ],
        req: [
            {
                name: "birth-cert",
                titles: [
                    {locale: "en", value: "Certificate of birth"},
                    {
                        locale: "ru",
                        value: "\u0421\u0432\u0438\u0434\u0435\u0442\u0435\u043b\u044c\u0441\u0442\u0432\u043e \u043e \u0440\u043e\u0436\u0434\u0435\u043d\u0438\u0438 \u0434\u0435\u0442\u0435\u0439"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-birth-cert"}
            }
        ]
    },
    {
        uuid: "cl-enc-or-punish-order",
        name: "enc-or-punish-order",
        titles: [
            {locale: "en", value: "Encouragement or punishment order"},
            {
                locale: "ru",
                value: "\u041f\u0440\u0438\u043a\u0430\u0437 \u043e \u043f\u043e\u043e\u0449\u0440\u0435\u043d\u0438\u0438 \u0438\u043b\u0438 \u0432\u0437\u044b\u0441\u043a\u0430\u043d\u0438\u0438"
            }
        ],
        req: [
            {
                name: "enc-or-punish-order",
                titles: [
                    {locale: "en", value: "Encouragement or punishment order"},
                    {
                        locale: "ru",
                        value: "\u041f\u0440\u0438\u043a\u0430\u0437 \u043e \u043f\u043e\u043e\u0449\u0440\u0435\u043d\u0438\u0438 \u0438\u043b\u0438 \u0432\u0437\u044b\u0441\u043a\u0430\u043d\u0438\u0438"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-enc-or-punish-order"}
            }
        ]
    },
    {
        uuid: "cl-disability-docs",
        name: "disability-docs",
        titles: [
            {locale: "en", value: "Documents of disability"},
            {
                locale: "ru",
                value: "\u0414\u043e\u043a\u0443\u043c\u0435\u043d\u0442\u044b \u043e\u0431 \u0438\u043d\u0432\u0430\u043b\u0438\u0434\u043d\u043e\u0441\u0442\u0438"
            }
        ],
        req: [
            {
                name: "disability-docs",
                titles: [
                    {locale: "en", value: "Documents of disability"},
                    {
                        locale: "ru",
                        value: "\u0414\u043e\u043a\u0443\u043c\u0435\u043d\u0442\u044b \u043e\u0431 \u0438\u043d\u0432\u0430\u043b\u0438\u0434\u043d\u043e\u0441\u0442\u0438"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-disability-docs"}
            }
        ]
    },
    {
        uuid: "cl-explaining-note",
        name: "explaining-note",
        titles: [
            {locale: "en", value: "Note explaining"},
            {
                locale: "ru",
                value: "\u041e\u0431\u044a\u044f\u0441\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u0430\u044f \u0437\u0430\u043f\u0438\u0441\u043a\u0430"
            }
        ],
        req: [
            {
                name: "explaining-note",
                titles: [
                    {locale: "en", value: "Note explaining"},
                    {
                        locale: "ru",
                        value: "\u041e\u0431\u044a\u044f\u0441\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u0430\u044f \u0437\u0430\u043f\u0438\u0441\u043a\u0430"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-explaining-note"}
            }
        ]
    },
    {
        uuid: "cl-official-note",
        name: "official-note",
        titles: [
            {locale: "en", value: "Official note"},
            {
                locale: "ru",
                value: "\u0421\u043b\u0443\u0436\u0435\u0431\u043d\u0430\u044f \u0437\u0430\u043f\u0438\u0441\u043a\u0430"
            }
        ],
        req: [
            {
                name: "official-note",
                titles: [
                    {locale: "en", value: "Official note"},
                    {
                        locale: "ru",
                        value: "\u0421\u043b\u0443\u0436\u0435\u0431\u043d\u0430\u044f \u0437\u0430\u043f\u0438\u0441\u043a\u0430"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-official-note"}
            }
        ]
    },
    {
        uuid: "cl-acts-and-notif",
        name: "acts-and-notif",
        titles: [
            {locale: "en", value: "Acts and notifications"},
            {
                locale: "ru",
                value: "\u0421\u043b\u0443\u0436\u0435\u0431\u043d\u0430\u044f \u0437\u0430\u043f\u0438\u0441\u043a\u0430"
            }
        ],
        req: [
            {
                name: "acts-and-notif",
                titles: [
                    {locale: "en", value: "Acts and notifications"},
                    {
                        locale: "ru",
                        value: "\u0421\u043b\u0443\u0436\u0435\u0431\u043d\u0430\u044f \u0437\u0430\u043f\u0438\u0441\u043a\u0430"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-acts-and-notif"}
            }
        ]
    },
    {
        uuid: "cl-miscellaneous",
        name: "miscellaneous",
        titles: [
            {locale: "en", value: "Miscellaneous"},
            {
                locale: "ru",
                value: "\u041f\u0440\u043e\u0447\u0435\u0435"
            }
        ],
        req: [
            {
                name: "miscellaneous",
                titles: [
                    {locale: "en", value: "Miscellaneous"},
                    {
                        locale: "ru",
                        value: "\u041f\u0440\u043e\u0447\u0435\u0435"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/cat-personal-documents", requiredKind: "workspace://SpacesStore/kind-d-miscellaneous"}
            }
        ]
    }
]);