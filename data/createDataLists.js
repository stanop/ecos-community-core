/*fill data lists*/

// createFileRegister("FilerRegister2", "111", "Register2", "36");
// createContractor("Contractor2", "Full name of Organization 2", "Petrov Petr", "Saint-Peterburg");
// createLegalEntities("LegalEntites", "FullNameLE", "123456", "Saratov");
// createBudgetItem("BudgetItem2", "BedgetName2", "122112");
// createUnit("Unit 1", "Unit Full Name 1", "Unit Short Name 1");
// createPaymentPeriod("Period 1", new Date(), new Date());
// createPaymentPeriod("Period 2");
// createContractsSubjects("contractsSubjects 1", "cosub1234", "subjectName1234");
// createDocumentType("Document type 1");

/*File Register*/
function createFileRegister(name, fileIndex, fileName, storagePeriod) {
    pathToContractors = "/app:company_home/cm:dataLists/cm:filesNomenclature";
    var folder = search.selectNodes(pathToContractors)[0];

    var newNode;
    var properties = [];
    properties['idocs:fileIndex'] = fileIndex;
    properties['idocs:fileName'] = fileName;
    properties['dms:storagePeriod'] = storagePeriod;

    newNode = folder.createNode(name, "idocs:filesNomenclature", properties);
}

/*Contractor*/
function createContractor(name, fullName, ceoName, jurAddress) {
    pathToContractors = "/app:company_home/app:dictionary/cm:dataLists/cm:contractors";
    var folder = search.selectNodes(pathToContractors)[0];

    var newNode;
    var properties = [];
    properties['idocs:fullOrganizationName'] = fullName;
    properties['idocs:CEOname'] = ceoName;
    properties['idocs:juridicalAddress'] = jurAddress;

    newNode = folder.createNode(name, "idocs:contractor", properties);
}

/*Legal Entities*/
function createLegalEntities(name, fullName, postAddress, jurAddress) {
    pathToContractors = "/app:company_home/app:dictionary/cm:dataLists/cm:legal-entities";
    var folder = search.selectNodes(pathToContractors)[0];

    var newNode;
    var properties = [];
    properties['idocs:fullOrganizationName'] = fullName;
    properties['idocs:postAddress'] = postAddress;
    properties['idocs:juridicalAddress'] = jurAddress;

    newNode = folder.createNode(name, "idocs:legalEntity", properties);
}

/*Budget Item*/
function createBudgetItem(name, budgetName, budgetCode) {
    pathToContractors = "/app:company_home/app:dictionary/cm:dataLists/cm:budget-items";
    var folder = search.selectNodes(pathToContractors)[0];

    var newNode;
    var properties = [];
    properties['budget:itemName'] = budgetName;
    properties['budget:itemCode'] = budgetCode;

    newNode = folder.createNode(name, "budget:item", properties);
}

/*Unit*/
function createUnit(name, unitFullName, unitShortName) {
    pathToContractors = "/app:company_home/app:dictionary/cm:dataLists/cm:units";
    var folder = search.selectNodes(pathToContractors)[0];

    var newNode;
    var properties = [];
    properties['pas:unitName'] = unitFullName;
    properties['pas:unitShortName'] = unitShortName;

    newNode = folder.createNode(name, "pas:unit", properties);
}

/*Payment Periods*/
function createPaymentPeriod(name, startDate, endDate) {
    pathToContractors = "/app:company_home/st:sites/cm:contracts/cm:dataLists/cm:payment-periods";
    var folder = search.selectNodes(pathToContractors)[0];

    var newNode;
    var properties = [];
    properties['payments:periodStart'] = startDate;
    properties['payments:periodEnd'] = endDate;

    newNode = folder.createNode(name, "payments:period", properties);
}
/*Payment Period (currentDate - (currentDate + 1 month))*/
function createPaymentPeriod(name) {
    pathToContractors = "/app:company_home/st:sites/cm:contracts/cm:dataLists/cm:payment-periods";
    var folder = search.selectNodes(pathToContractors)[0];
    var futureDay = new Date();
    futureDay.setMonth(futureDay.getMonth() + 1);

    var newNode;
    var properties = [];
    properties['payments:periodStart'] = new Date();
    properties['payments:periodEnd'] = futureDay;

    newNode = folder.createNode(name, "payments:period", properties);
}

/*Subjects of Contracts*/
function createContractsSubjects(name, subjectCode, subjectName) {
    pathToContractors = "/app:company_home/st:sites/cm:contracts/cm:dataLists/cm:subjects";
    var folder = search.selectNodes(pathToContractors)[0];

    var newNode;
    var properties = [];
    properties['contracts:subjectCode'] = subjectCode;
    properties['contracts:subjectName'] = subjectName;

    newNode = folder.createNode(name, "contracts:subject", properties);
}

/*Document Type*/
function createDocumentType(name) {
    pathToContractors = "/app:company_home/st:sites/cm:contracts/cm:dataLists/cm:closing-document-type";
    var folder = search.selectNodes(pathToContractors)[0];
    var newNode;
    newNode = folder.createNode(name, "contracts:documentType");
}

/*Bank Accounts*/
/*Products and services*/
/*Passports*/