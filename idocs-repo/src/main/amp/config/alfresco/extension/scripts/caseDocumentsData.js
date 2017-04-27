var containerKinds = [
    {
        "nodeRef": "sca:creditApplicationCase",
        "name": "Кредита",
        "documentKinds": [
            {
                "nodeRef": "workspace://SpacesStore/kind-d-credit-application",
                "mandatory": "credit-project",
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-credit-contract",
                "mandatory": "credit-agreement",
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-financial-report",
                "mandatory": null,
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-credit-committee",
                "mandatory": "credit-committee",
                "multiple": false
            },
            //{
            //    "nodeRef": "workspace://SpacesStore/kind-d-account",
            //    "mandatory": "credit-accounts",
            //    "multiple": false
            //},
            {
                "nodeRef": "workspace://SpacesStore/kind-d-order-of-opening-account",
                "mandatory": "credit-accounts",
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-optional",
                "mandatory": null,
                "multiple": true
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-financial-analysis",
                "mandatory": "credit-evaluation",
                "multiple": false,
                "loanType": [1, 2, 4, 5]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-warrant",
                "mandatory": null,
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-repayment-schedule",
                "mandatory": "credit-accounts",
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-prihodnik",
                "mandatory": "credit-accounts",
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-consumables",
                "mandatory": "credit-accounts",
                "multiple": false
            },

        ]
    },
    {
        "nodeRef": "sca:applicantCase",
        "name": "Заявителя",
        "documentKinds": [
            {
                "nodeRef": "workspace://SpacesStore/kind-d-passport-copy-applicant",
                "mandatory": "credit-project",
                "multiple": false,
                "containerType": ["workspace://SpacesStore/type-c-individual", "workspace://SpacesStore/type-c-chp", "workspace://SpacesStore/type-c-ip"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-ITN-applicant",
                "mandatory": "credit-project",
                "multiple": false,
                "containerType": ["workspace://SpacesStore/type-c-individual", "workspace://SpacesStore/type-c-chp", "workspace://SpacesStore/type-c-ip"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-balance",
                "mandatory": null,
                "multiple": true
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-credit-office-report-appl",
                "mandatory": "credit-project",
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-optional",
                "mandatory": null,
                "multiple": true
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-certificate-residence",
                "mandatory": null,
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-certificate-fam-composition",
                "mandatory": null,
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-certificate-workplace-appl",
                "mandatory": "credit-project",
                "multiple": true,
                "containerType": "workspace://SpacesStore/type-c-individual"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-certificate-salary-appl",
                "mandatory": "credit-project",
                "multiple": true,
                "containerType": "workspace://SpacesStore/type-c-individual"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-cert-reg-tax-committee",
                "mandatory": "credit-project",
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-legal"
            },{
                "nodeRef": "workspace://SpacesStore/kind-d-cert-reg-tax-committee-chp",
                "mandatory": "credit-project",
                "multiple": false,
                "containerType": ["workspace://SpacesStore/type-c-chp", "workspace://SpacesStore/type-c-ip"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-statement-to-testimony-rvnk",
                "mandatory": null,
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-cert-nodbt-wth-tax-committee",
                "mandatory": null,
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-ITN-legal-entity",
                "mandatory": "credit-project",
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-legal"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-passport-director",
                "mandatory": "credit-project",
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-legal"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-charter-company",
                "mandatory": null,
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-legal"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-protocol-consent-founders",
                "mandatory": null,
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-legal"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-order-appointment-director",
                "mandatory": "credit-project",
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-legal"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-ITN-director",
                "mandatory": "credit-project",
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-legal"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-order-appnt-chief-accountant",
                "mandatory": null,
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-legal"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-passport-chief-accountant",
                "mandatory": null,
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-legal"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-ITN-chief-accountant",
                "mandatory": null,
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-legal"
            }


        ]
    },
    {
        "nodeRef": "sca:guarantorCase",
        "name": "Поручителя",
        "documentKinds": [
            {
                "nodeRef": "workspace://SpacesStore/kind-d-passport-copy-guarantor",
                "mandatory": "credit-agreement",
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-ITN-guarantor",
                "mandatory": "credit-agreement",
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-guarantor-contract",
                "mandatory": "credit-agreement",
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-optional",
                "mandatory": null,
                "multiple": true
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-credit-office-report-grnt",
                "mandatory": "credit-project",
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-certificate-workplace-grnt",
                "mandatory": "credit-agreement",
                "multiple": true,
                "containerType":["workspace://SpacesStore/type-c-individual", "workspace://SpacesStore/type-c-legal", "workspace://SpacesStore/type-c-ip"]

            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-certificate-salary-grnt",
                "mandatory": "credit-agreement",
                "multiple": true,
                "containerType":["workspace://SpacesStore/type-c-individual", "workspace://SpacesStore/type-c-legal", "workspace://SpacesStore/type-c-ip"]

            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-guarantor-solvency",
                "mandatory": "credit-agreement",
                "multiple": true
            }

        ]
    },
    {
        "nodeRef": "sca:pledgeCase",
        "name": "Залога",
        "documentKinds": [
            {
                "nodeRef": "workspace://SpacesStore/kind-d-deposit-info",
                "mandatory": "credit-evaluation",
                "multiple": false,
                "containerType": ["workspace://SpacesStore/type-c-real-estate", "workspace://SpacesStore/type-c-real-estate-with-land"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-photo",
                "mandatory": "credit-evaluation",
                "multiple": true,
                "containerType": ["workspace://SpacesStore/type-c-goods-in-circulation", "workspace://SpacesStore/type-c-equipment", "workspace://SpacesStore/type-c-real-estate", "workspace://SpacesStore/type-c-securities", "workspace://SpacesStore/type-c-real-estate-with-land", "workspace://SpacesStore/type-c-household-goods"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-ownership-certificate",
                "mandatory": "credit-evaluation",
                "multiple": false,
                "containerType": ["workspace://SpacesStore/type-c-real-estate", "workspace://SpacesStore/type-c-real-estate-with-land"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-deposit-contract",
                "mandatory": null,
                "multiple": false,
                "containerType": ["workspace://SpacesStore/type-c-jewelry", "workspace://SpacesStore/type-c-vehicle", "workspace://SpacesStore/type-c-goods-in-circulation", "workspace://SpacesStore/type-c-equipment"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-deposit-contract-mandatory",
                "mandatory": "credit-agreement",
                "multiple": false,
                "containerType": ["workspace://SpacesStore/type-c-real-estate", "workspace://SpacesStore/type-c-real-estate-with-land", "workspace://SpacesStore/type-c-household-goods"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-optional",
                "mandatory": null,
                "multiple": true
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-certificate-Interpol",
                "mandatory": null,
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-vehicle"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-certificate-no-debt",
                "mandatory": null,
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-vehicle"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-techpassport-copy",
                "mandatory": "credit-evaluation",
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-vehicle"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-eval-real-estate-collateral",
                "mandatory": "credit-evaluation",
                "multiple": false,
                "containerType": ["workspace://SpacesStore/type-c-real-estate", "workspace://SpacesStore/type-c-real-estate-with-land"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-ITN-mortgagor",
                "mandatory": "credit-evaluation",
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-passport-mortgagor",
                "mandatory": "credit-evaluation",
                "multiple": false
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-legal-service-concl",
                "mandatory": "credit-evaluation",
                "multiple": false,
                "containerType": ["workspace://SpacesStore/type-c-real-estate", "workspace://SpacesStore/type-c-real-estate-with-land"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-risk-management-concl",
                "mandatory": "credit-evaluation",
                "multiple": false,
                "containerType": ["workspace://SpacesStore/type-c-real-estate", "workspace://SpacesStore/type-c-real-estate-with-land"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-technical-passport",
                "mandatory": "credit-evaluation",
                "multiple": false,
                "containerType": ["workspace://SpacesStore/type-c-real-estate", "workspace://SpacesStore/type-c-real-estate-with-land"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-house-book",
                "mandatory": null,
                "multiple": false,
                "containerType": ["workspace://SpacesStore/type-c-real-estate", "workspace://SpacesStore/type-c-real-estate-with-land"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-certificate-land",
                "mandatory": null,
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-real-estate-with-land"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-evaluation-tajikcrystal",
                "mandatory": "credit-evaluation",
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-jewelry"
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-documents-ministry-justice",
                "mandatory": "credit-agreement",
                "multiple": true,
                "containerType": ["workspace://SpacesStore/type-c-real-estate", "workspace://SpacesStore/type-c-real-estate-with-land"]
            },
            {
                "nodeRef": "workspace://SpacesStore/kind-d-vehicle-evaluation",
                "mandatory": "credit-evaluation",
                "multiple": false,
                "containerType": "workspace://SpacesStore/type-c-vehicle"
            }

        ]
    }
];

var documentTypes = [
    {
        "nodeRef": "workspace://SpacesStore/type-d-common",
        "name": "Общие документы"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-d-credit",
        "name": "Документы по заявке"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-d-personal",
        "name": "Личные документы"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-d-guarantor",
        "name": "Документы по поручительству"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-d-deposit",
        "name": "Документы по залогу"
    }
];

var containerTypes = [
    {
        "nodeRef": "workspace://SpacesStore/type-c-jewelry",
        "name": "Драгоцености",
        "type": "sca:pledgeCase"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-c-real-estate",
        "name": "Недвижимость",
        "type": "sca:pledgeCase"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-c-vehicle",
        "name": "Транспортное средство",
        "type": "sca:pledgeCase"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-c-securities",
        "name": "Ценные бумаги",
        "type": "sca:pledgeCase"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-c-real-estate-with-land",
        "name": "Недвижимость с землей",
        "type": "sca:pledgeCase"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-c-household-goods",
        "name": "Домашнее имущество",
        "type": "sca:pledgeCase"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-c-goods-in-circulation",
        "name": "Товар в обороте",
        "type": "sca:pledgeCase"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-c-equipment",
        "name": "Оборудование",
        "type": "sca:pledgeCase"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-c-individual",
        "name": "Физическое лицо",
        "type": "sca:applicantCase"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-c-legal",
        "name": "Юридическое лицо",
        "type": "sca:applicantCase"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-c-ip",
        "name": "ИП",
        "type": "sca:applicantCase"
    },
    {
        "nodeRef": "workspace://SpacesStore/type-c-chp",
        "name": "ЧП",
        "type": "sca:applicantCase"
    }
];

var documentKinds = [

    {
        "nodeRef": "workspace://SpacesStore/kind-d-optional",
        "name": "Дополнительный документ",
        "type": "workspace://SpacesStore/type-d-common"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-credit-application",
        "name": "Заявка на кредит",
        "type": "workspace://SpacesStore/type-d-credit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-credit-contract",
        "name": "Кредитный договор",
        "type": "workspace://SpacesStore/type-d-credit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-deposit-contract",
        "name": "Договор залога(залога ценностей, авто, товарооборота, оборудования)",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-deposit-contract-mandatory",
        "name": "Договор залога",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-financial-report",
        "name": "Финансовый отчет",
        "type": "workspace://SpacesStore/type-d-credit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-passport-copy-applicant",
        "name": "Паспорт заявителя",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-passport-copy-guarantor",
        "name": "Паспорт поручителя",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-balance",
        "name": "Годовой баланс предприятия, заверенный налоговым комитетом (акт сверки)",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-ITN-applicant",
        "name": "ИНН заявителя",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-ITN-guarantor",
        "name": "ИНН поручителя",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-guarantor-contract",
        "name": "Договор поручительства",
        "type": "workspace://SpacesStore/type-d-guarantor"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-deposit-info",
        "name": "Акт оценки залога",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-credit-committee",
        "name": "Заключение кредитного комитета",
        "type": "workspace://SpacesStore/type-d-credit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-order-of-opening-account",
        "name": "Распоряжение об открытии счета",
        "type": "workspace://SpacesStore/type-d-credit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-photo",
        "name": "Фотографии залога",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-account",
        "name": "Реквизиты счета",
        "type": "workspace://SpacesStore/type-d-credit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-ownership-certificate",
        "name": "Правоустанавливающие документы (или легализация)",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-certificate-residence",
        "name": "Справка с местожительства",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-certificate-fam-composition",
        "name": "Справка о составе семьи",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-certificate-workplace-appl",
        "name": "Справка с места работы заявителя",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-certificate-workplace-grnt",
        "name": "Справка с места работы поручителя",
        "type": "workspace://SpacesStore/type-d-guarantor"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-certificate-salary-appl",
        "name": "Справка о зарплате заявителя",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-certificate-salary-grnt",
        "name": "Справка о зарплате поручителя",
        "type": "workspace://SpacesStore/type-d-guarantor"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-certificate-Interpol",
        "name": "Справка из Интерпола",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-certificate-no-debt",
        "name": "Справка о не задолженности с ГАИ по камера",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-techpassport-copy",
        "name": "Копия техпаспорта",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-eval-real-estate-collateral",
        "name": "Оценка залога недвижимости",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-passport-mortgagor",
        "name": "Паспорт Залогодателя",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-ITN-mortgagor",
        "name": "ИНН залогодателя",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-legal-service-concl",
        "name": "Заключение Юр. Службы",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-risk-management-concl",
        "name": "Заключение Управление риск",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-technical-passport",
        "name": "Технический паспорт",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-house-book",
        "name": "Домовая книга",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-certificate-land",
        "name": "Сертификат земли",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-documents-solvency-guarantor",
        "name": "Документы подтверждающие платежеспособность поручителя",
        "type": "workspace://SpacesStore/type-d-guarantor"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-evaluation-tajikcrystal",
        "name": "Оценка из таджиккристала",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-financial-analysis",
        "name": "Финансовый анализ",
        "type": "workspace://SpacesStore/type-d-credit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-warrant",
        "name": "Доверенность",
        "type": "workspace://SpacesStore/type-d-credit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-documents-ministry-justice",
        "name": "Документы из мин. Юстиции",
        "type": "workspace://SpacesStore/type-d-deposit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-repayment-schedule",
        "name": "График погашения",
        "type": "workspace://SpacesStore/type-d-credit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-prihodnik",
        "name": "Приходник",
        "type": "workspace://SpacesStore/type-d-credit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-consumables",
        "name": "Расходник",
        "type": "workspace://SpacesStore/type-d-credit"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-cert-reg-tax-committee",
        "name": "Свидетельство о регистрации в налоговом комитете",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-cert-reg-tax-committee-chp",
        "name": "Свидетельство о регистрации в налоговом комитете (патент) выписка к свидетельству",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-statement-to-testimony-rvnk",
        "name": "Выписка к свидетельству о р.в.н.к",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-cert-nodbt-wth-tax-committee",
        "name": "Справка о не задолженности с налогового комитета",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-ITN-legal-entity",
        "name": "ИНН (организации)",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-charter-company",
        "name": "Устав фирмы",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-protocol-consent-founders",
        "name": "Протокол согласия учредителей",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-order-appointment-director",
        "name": "Приказ о назначении директора",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-passport-director",
        "name": "Паспорт директора",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-ITN-director",
        "name": "ИНН директора",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-order-appnt-chief-accountant",
        "name": "Приказ о назначении главного бухгалтера",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-passport-chief-accountant",
        "name": "Паспорт главного бухгалтера",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-ITN-chief-accountant",
        "name": "ИНН главного бухгалтера",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-credit-office-report-appl",
        "name": "Отчет кредитного бюро по заявителю",
        "type": "workspace://SpacesStore/type-d-personal"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-credit-office-report-grnt",
        "name": "Отчет кредитного бюро по поручителю",
        "type": "workspace://SpacesStore/type-d-guarantor"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-guarantor-solvency",
        "name": "Документы подтверждающие платежеспособность поручителя. (справка с место работы, справка о з/п или Свидетельство о регистрации в налоговом комитете (патент) )",
        "type": "workspace://SpacesStore/type-d-guarantor"
    },
    {
        "nodeRef": "workspace://SpacesStore/kind-d-vehicle-evaluation",
        "name": "Оценка автомобиля",
        "type": "workspace://SpacesStore/type-d-credit"
    }
];

var stages = ["credit-project", "credit-evaluation", "credit-committee", "credit-agreement", "credit-accounts"];

