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
    },
    {
        uuid: "cl-passport-rus",
        name: "passport-rus",
        titles: [
            {locale: "en", value: "Russian passport"},
            {
                locale: "ru",
                value: "\u041f\u0430\u0441\u043f\u043e\u0440\u0442\u0020\u0433\u0440\u0430\u0436\u0434\u0430\u043d\u0438\u043d\u0430\u0020\u0420\u0424"
            }
        ],
        req: [
            {
                name: "passport.rus",
                titles: [
                    {locale: "en", value: "Russian passport"},
                    {
                        locale: "ru",
                        value: "\u041f\u0430\u0441\u043f\u043e\u0440\u0442\u0020\u0433\u0440\u0430\u0436\u0434\u0430\u043d\u0438\u043d\u0430\u0020\u0420\u0424"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/idocs-cat-doctype-passport", requiredKind: "workspace://SpacesStore/idocs-cat-dockind-passport-rus"}
            }
        ]
    },
    {
        uuid: "cl-foundDoc-charter",
        name: "foundDoc-charter",
        titles: [
            {locale: "en", value: "Charter"},
            {
                locale: "ru",
                value: "\u0423\u0441\u0442\u0430\u0432"
            }
        ],
        req: [
            {
                name: "foundDoc-charter",
                titles: [
                    {locale: "en", value: "Charter"},
                    {
                        locale: "ru",
                        value: "\u0423\u0441\u0442\u0430\u0432"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/idocs-cat-doctype-foundDoc", requiredKind: "workspace://SpacesStore/idocs-cat-dockind-foundDoc-charter"}
            }
        ]
    },
    {
        uuid: "cl-foundDoc-ceOrder",
        name: "foundDoc-ceOrder",
        titles: [
            {locale: "en", value: "CEO appointment order"},
            {
                locale: "ru",
                value: "\u041f\u0440\u0438\u043a\u0430\u0437\u0020\u043e\u0020\u043d\u0430\u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0438\u0020\u0433\u0435\u043d\u002e\u0434\u0438\u0440\u0435\u043a\u0442\u043e\u0440\u0430"
            }
        ],
        req: [
            {
                name: "foundDoc-ceOrder",
                titles: [
                    {locale: "en", value: "CEO appointment order"},
                    {
                        locale: "ru",
                        value: "\u041f\u0440\u0438\u043a\u0430\u0437\u0020\u043e\u0020\u043d\u0430\u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0438\u0020\u0433\u0435\u043d\u002e\u0434\u0438\u0440\u0435\u043a\u0442\u043e\u0440\u0430"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/idocs-cat-doctype-foundDoc", requiredKind: "workspace://SpacesStore/idocs-cat-dockind-foundDoc-ceOrder"}
            }
        ]
    },
    {
        uuid: "cl-foundDoc-attScan",
        name: "foundDoc-attScan",
        titles: [
            {locale: "en", value: "Power of attorney scan"},
            {
                locale: "ru",
                value: "\u0421\u043a\u0430\u043d\u002d\u043a\u043e\u043f\u0438\u044f\u0020\u0434\u043e\u0432\u0435\u0440\u0435\u043d\u043d\u043e\u0441\u0442\u0438"
            }
        ],
        req: [
            {
                name: "foundDoc-attScan",
                titles: [
                    {locale: "en", value: "Power of attorney scan"},
                    {
                        locale: "ru",
                        value: "\u0421\u043a\u0430\u043d\u002d\u043a\u043e\u043f\u0438\u044f\u0020\u0434\u043e\u0432\u0435\u0440\u0435\u043d\u043d\u043e\u0441\u0442\u0438"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/idocs-cat-doctype-foundDoc", requiredKind: "workspace://SpacesStore/idocs-cat-dockind-foundDoc-attScan"}
            }
        ]
    },
    {
        uuid: "cl-foundDoc-innScan",
        name: "foundDoc-innScan",
        titles: [
            {locale: "en", value: "INN scan"},
            {
                locale: "ru",
                value: "\u0421\u043a\u0430\u043d\u002d\u043a\u043e\u043f\u0438\u044f\u0020\u0418\u041d\u041d"
            }
        ],
        req: [
            {
                name: "foundDoc-innScan",
                titles: [
                    {locale: "en", value: "INN scan"},
                    {
                        locale: "ru",
                        value: "\u0421\u043a\u0430\u043d\u002d\u043a\u043e\u043f\u0438\u044f\u0020\u0418\u041d\u041d"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/idocs-cat-doctype-foundDoc", requiredKind: "workspace://SpacesStore/idocs-cat-dockind-foundDoc-innScan"}
            }
        ]
    },
    {
        uuid: "cl-foundDoc-ogrnScan",
        name: "foundDoc-ogrnScan",
        titles: [
            {locale: "en", value: "OGRN scan"},
            {
                locale: "ru",
                value: "\u0421\u043a\u0430\u043d\u002d\u043a\u043e\u043f\u0438\u044f\u0020\u041e\u0413\u0420\u041d"
            }
        ],
        req: [
            {
                name: "foundDoc-ogrnScan",
                titles: [
                    {locale: "en", value: "OGRN scan"},
                    {
                        locale: "ru",
                        value: "\u0421\u043a\u0430\u043d\u002d\u043a\u043e\u043f\u0438\u044f\u0020\u041e\u0413\u0420\u041d"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/idocs-cat-doctype-foundDoc", requiredKind: "workspace://SpacesStore/idocs-cat-dockind-foundDoc-ogrnScan"}
            }
        ]
    },
    {
        uuid: "cl-foundDoc-egrul",
        name: "foundDoc-egrul",
        titles: [
            {locale: "en", value: "Certificate of Incorporation"},
            {
                locale: "ru",
                value: "\u0412\u044b\u043f\u0438\u0441\u043a\u0430\u0020\u0438\u0437\u0020\u0415\u0413\u0420\u042e\u041b\u0020\u0437\u0430\u0020\u043f\u043e\u0441\u043b\u002e\u043c\u0435\u0441\u044f\u0446"
            }
        ],
        req: [
            {
                name: "foundDoc-egrul",
                titles: [
                    {locale: "en", value: "Certificate of Incorporation"},
                    {
                        locale: "ru",
                        value: "\u0412\u044b\u043f\u0438\u0441\u043a\u0430\u0020\u0438\u0437\u0020\u0415\u0413\u0420\u042e\u041b\u0020\u0437\u0430\u0020\u043f\u043e\u0441\u043b\u002e\u043c\u0435\u0441\u044f\u0446"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/idocs-cat-doctype-foundDoc", requiredKind: "workspace://SpacesStore/idocs-cat-dockind-foundDoc-egrul"}
            }
        ]
    },
    {
        uuid: "cl-foundDoc-orgCard",
        name: "foundDoc-orgCard",
        titles: [
            {locale: "en", value: "Organization card"},
            {
                locale: "ru",
                value: "\u041a\u0430\u0440\u0442\u043e\u0447\u043a\u0430\u0020\u043e\u0440\u0433\u0430\u043d\u0438\u0437\u0430\u0446\u0438\u0438"
            }
        ],
        req: [
            {
                name: "foundDoc-orgCard",
                titles: [
                    {locale: "en", value: "Organization card"},
                    {
                        locale: "ru",
                        value: "\u041a\u0430\u0440\u0442\u043e\u0447\u043a\u0430\u0020\u043e\u0440\u0433\u0430\u043d\u0438\u0437\u0430\u0446\u0438\u0438"
                    }
                ],
                scope: "associations",
                consequent: {predicate: "kind", requiredType: "workspace://SpacesStore/idocs-cat-doctype-foundDoc", requiredKind: "workspace://SpacesStore/idocs-cat-dockind-foundDoc-orgCard"}
            }
        ]
    }
]);