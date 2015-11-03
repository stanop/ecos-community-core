
orgmeta.createSubType("branch", "company");
orgmeta.createSubType("branch", "department");
orgmeta.createSubType("branch", "division");
orgmeta.createSubType("branch", "branch");
orgmeta.createSubType("branch", "subdivision");
orgmeta.createSubType("branch", "organization");
orgmeta.createSubType("branch", "section");
orgmeta.createSubType("role", "director-general");
orgmeta.createSubType("role", "department_head");
orgmeta.createSubType("role", "staff");
orgmeta.createSubType("role", "technical_director");
orgmeta.createSubType("role", "branch_manager");
orgmeta.createSubType("role", "manager");
orgmeta.createSubType("role", "employee");
orgmeta.createSubType("role", "executive_director");

if(groups.getGroup("_orgstruct_home_") == null) {
    groups.createRootGroup("_orgstruct_home_", "_orgstruct_home_");
}
if(groups.getGroup("pharmacy") == null) {
    groups.createRootGroup("pharmacy", "Фармацевтика");
}
orgstruct.createTypedGroup("branch", "organization", "pharmacy");
if(groups.getGroup("pharmacy_accounts_department") == null) {
    groups.createRootGroup("pharmacy_accounts_department", "Бухгалтерия");
}
orgstruct.createTypedGroup("branch", "department", "pharmacy_accounts_department");
if(groups.getGroup("pharmacy_accountant") == null) {
    groups.createRootGroup("pharmacy_accountant", "Бухгалтер");
}
orgstruct.createTypedGroup("role", "employee", "pharmacy_accountant");
if(people.getPerson("griboedov") == null) {
    people.createPerson("griboedov", "Александр", "Грибоедов", "test@citeck.ru", "griboedov", true);
}
groups.getGroup("pharmacy_accountant").addAuthority("griboedov");
groups.getGroup("pharmacy_accounts_department").addAuthority("GROUP_pharmacy_accountant");
if(groups.getGroup("pharmacy_chief_accountant") == null) {
    groups.createRootGroup("pharmacy_chief_accountant", "Главный бухгалтер");
}
orgstruct.createTypedGroup("role", "manager", "pharmacy_chief_accountant");
if(people.getPerson("esenin") == null) {
    people.createPerson("esenin", "Сергей", "Есенин", "test@citeck.ru", "esenin", true);
}
groups.getGroup("pharmacy_chief_accountant").addAuthority("esenin");
groups.getGroup("pharmacy_accounts_department").addAuthority("GROUP_pharmacy_chief_accountant");
groups.getGroup("pharmacy").addAuthority("GROUP_pharmacy_accounts_department");
if(groups.getGroup("pharmacy_director") == null) {
    groups.createRootGroup("pharmacy_director", "Генеральный директор");
}
orgstruct.createTypedGroup("role", "manager", "pharmacy_director");
if(people.getPerson("pushkin") == null) {
    people.createPerson("pushkin", "Александр", "Пушкин", "test@citeck.ru", "pushkin", true);
}
groups.getGroup("pharmacy_director").addAuthority("pushkin");
groups.getGroup("pharmacy").addAuthority("GROUP_pharmacy_director");
if(groups.getGroup("pharmacy_economic_security") == null) {
    groups.createRootGroup("pharmacy_economic_security", "Отдел экономической безопасности");
}
orgstruct.createTypedGroup("branch", "subdivision", "pharmacy_economic_security");
if(groups.getGroup("pharmacy_economic_security_manager") == null) {
    groups.createRootGroup("pharmacy_economic_security_manager", "Начальник ОЭБ");
}
orgstruct.createTypedGroup("role", "manager", "pharmacy_economic_security_manager");
if(people.getPerson("lermontov") == null) {
    people.createPerson("lermontov", "Михаил", "Лермонтов", "test@citeck.ru", "lermontov", true);
}
groups.getGroup("pharmacy_economic_security_manager").addAuthority("lermontov");
groups.getGroup("pharmacy_economic_security").addAuthority("GROUP_pharmacy_economic_security_manager");
groups.getGroup("pharmacy").addAuthority("GROUP_pharmacy_economic_security");
if(groups.getGroup("pharmacy_executive_director") == null) {
    groups.createRootGroup("pharmacy_executive_director", "Исполнительный директор");
}
orgstruct.createTypedGroup("role", "executive_director", "pharmacy_executive_director");
if(people.getPerson("tolstoy") == null) {
    people.createPerson("tolstoy", "Лев", "Толстой", "test@citeck.ru", "tolstoy", true);
}
groups.getGroup("pharmacy_executive_director").addAuthority("tolstoy");
groups.getGroup("pharmacy").addAuthority("GROUP_pharmacy_executive_director");
if(groups.getGroup("pharmacy_finance_department") == null) {
    groups.createRootGroup("pharmacy_finance_department", "Департамент финансов");
}
orgstruct.createTypedGroup("branch", "department", "pharmacy_finance_department");
if(groups.getGroup("pharmacy_finance_director") == null) {
    groups.createRootGroup("pharmacy_finance_director", "Финансовый директор");
}
orgstruct.createTypedGroup("role", "manager", "pharmacy_finance_director");
if(people.getPerson("blok") == null) {
    people.createPerson("blok", "Александр", "Блок", "test@citeck.ru", "blok", true);
}
groups.getGroup("pharmacy_finance_director").addAuthority("blok");
groups.getGroup("pharmacy_finance_department").addAuthority("GROUP_pharmacy_finance_director");
if(groups.getGroup("pharmacy_finance_subdivision") == null) {
    groups.createRootGroup("pharmacy_finance_subdivision", "Финансово-экономический отдел");
}
orgstruct.createTypedGroup("branch", "subdivision", "pharmacy_finance_subdivision");
if(groups.getGroup("pharmacy_finance_subdivision_manager") == null) {
    groups.createRootGroup("pharmacy_finance_subdivision_manager", "Начальник ФЭО");
}
orgstruct.createTypedGroup("role", "manager", "pharmacy_finance_subdivision_manager");
if(people.getPerson("bunin") == null) {
    people.createPerson("bunin", "Иван", "Бунин", "test@citeck.ru", "bunin", true);
}
groups.getGroup("pharmacy_finance_subdivision_manager").addAuthority("bunin");
groups.getGroup("pharmacy_finance_subdivision").addAuthority("GROUP_pharmacy_finance_subdivision_manager");
groups.getGroup("pharmacy_finance_department").addAuthority("GROUP_pharmacy_finance_subdivision");
groups.getGroup("pharmacy").addAuthority("GROUP_pharmacy_finance_department");
if(groups.getGroup("pharmacy_it_department") == null) {
    groups.createRootGroup("pharmacy_it_department", "Департамент ИТ");
}
orgstruct.createTypedGroup("branch", "department", "pharmacy_it_department");
if(groups.getGroup("pharmacy_it_director") == null) {
    groups.createRootGroup("pharmacy_it_director", "Заместитель генерального директора по ИТ");
}
orgstruct.createTypedGroup("role", "manager", "pharmacy_it_director");
if(people.getPerson("ahmatova") == null) {
    people.createPerson("ahmatova", "Анна", "Ахматова", "test@citeck.ru", "ahmatova", true);
}
groups.getGroup("pharmacy_it_director").addAuthority("ahmatova");
groups.getGroup("pharmacy_it_department").addAuthority("GROUP_pharmacy_it_director");
groups.getGroup("pharmacy").addAuthority("GROUP_pharmacy_it_department");
if(groups.getGroup("pharmacy_legal_department") == null) {
    groups.createRootGroup("pharmacy_legal_department", "Юридический департамент");
}
orgstruct.createTypedGroup("branch", "department", "pharmacy_legal_department");
if(groups.getGroup("pharmacy_contract_subdivision") == null) {
    groups.createRootGroup("pharmacy_contract_subdivision", "Отдел договоров");
}
orgstruct.createTypedGroup("branch", "subdivision", "pharmacy_contract_subdivision");
groups.getGroup("pharmacy_legal_department").addAuthority("GROUP_pharmacy_contract_subdivision");
if(groups.getGroup("pharmacy_legal_director") == null) {
    groups.createRootGroup("pharmacy_legal_director", "Директор юридического департамента");
}
orgstruct.createTypedGroup("role", "manager", "pharmacy_legal_director");
if(people.getPerson("pasternak") == null) {
    people.createPerson("pasternak", "Борис", "Пастернак", "test@citeck.ru", "pasternak", true);
}
groups.getGroup("pharmacy_legal_director").addAuthority("pasternak");
groups.getGroup("pharmacy_legal_department").addAuthority("GROUP_pharmacy_legal_director");
if(groups.getGroup("pharmacy_legal_subdivision") == null) {
    groups.createRootGroup("pharmacy_legal_subdivision", "Юридический отдел");
}
orgstruct.createTypedGroup("branch", "subdivision", "pharmacy_legal_subdivision");
if(groups.getGroup("pharmacy_chief_lawyer") == null) {
    groups.createRootGroup("pharmacy_chief_lawyer", "Главный юрист");
}
orgstruct.createTypedGroup("role", "department_head", "pharmacy_chief_lawyer");
if(people.getPerson("nekrasov") == null) {
    people.createPerson("nekrasov", "Николай", "Некрасов", "test@citeck.ru", "nekrasov", true);
}
groups.getGroup("pharmacy_chief_lawyer").addAuthority("nekrasov");
groups.getGroup("pharmacy_legal_subdivision").addAuthority("GROUP_pharmacy_chief_lawyer");
if(groups.getGroup("pharmacy_lawyer") == null) {
    groups.createRootGroup("pharmacy_lawyer", "Юрист");
}
orgstruct.createTypedGroup("role", "staff", "pharmacy_lawyer");
if(people.getPerson("gogol") == null) {
    people.createPerson("gogol", "Николай", "Гоголь", "test@citeck.ru", "gogol", true);
}
groups.getGroup("pharmacy_lawyer").addAuthority("gogol");
groups.getGroup("pharmacy_legal_subdivision").addAuthority("GROUP_pharmacy_lawyer");
groups.getGroup("pharmacy_legal_department").addAuthority("GROUP_pharmacy_legal_subdivision");
groups.getGroup("pharmacy").addAuthority("GROUP_pharmacy_legal_department");
if(groups.getGroup("pharmacy_pharmacy_department") == null) {
    groups.createRootGroup("pharmacy_pharmacy_department", "Департамент лекарственных средств");
}
orgstruct.createTypedGroup("branch", "department", "pharmacy_pharmacy_department");
if(groups.getGroup("pharmacy_pharmacy_director") == null) {
    groups.createRootGroup("pharmacy_pharmacy_director", "Директор департамента ЛС");
}
orgstruct.createTypedGroup("role", "manager", "pharmacy_pharmacy_director");
if(people.getPerson("bulgakov") == null) {
    people.createPerson("bulgakov", "Михаил", "Булгаков", "test@citeck.ru", "bulgakov", true);
}
groups.getGroup("pharmacy_pharmacy_director").addAuthority("bulgakov");
groups.getGroup("pharmacy_pharmacy_department").addAuthority("GROUP_pharmacy_pharmacy_director");
groups.getGroup("pharmacy").addAuthority("GROUP_pharmacy_pharmacy_department");
if(groups.getGroup("pharmacy_riui_subdivision") == null) {
    groups.createRootGroup("pharmacy_riui_subdivision", "Отдел РиУИ");
}
orgstruct.createTypedGroup("branch", "subdivision", "pharmacy_riui_subdivision");
if(groups.getGroup("pharmacy_riui_manager") == null) {
    groups.createRootGroup("pharmacy_riui_manager", "Начальник отдела РиУИ");
}
orgstruct.createTypedGroup("role", "manager", "pharmacy_riui_manager");
if(people.getPerson("dostoyevsky") == null) {
    people.createPerson("dostoyevsky", "Фёдор", "Достоевский", "test@citeck.ru", "dostoyevsky", true);
}
groups.getGroup("pharmacy_riui_manager").addAuthority("dostoyevsky");
groups.getGroup("pharmacy_riui_subdivision").addAuthority("GROUP_pharmacy_riui_manager");
groups.getGroup("pharmacy").addAuthority("GROUP_pharmacy_riui_subdivision");
if(groups.getGroup("pharmacy_trademark_department") == null) {
    groups.createRootGroup("pharmacy_trademark_department", "Департамент собственной торговой марки");
}
orgstruct.createTypedGroup("branch", "department", "pharmacy_trademark_department");
if(groups.getGroup("pharmacy_trademark_director") == null) {
    groups.createRootGroup("pharmacy_trademark_director", "Директор департамента СТМ");
}
orgstruct.createTypedGroup("role", "manager", "pharmacy_trademark_director");
if(people.getPerson("leskov") == null) {
    people.createPerson("leskov", "Николай", "Лесков", "test@citeck.ru", "leskov", true);
}
groups.getGroup("pharmacy_trademark_director").addAuthority("leskov");
groups.getGroup("pharmacy_trademark_department").addAuthority("GROUP_pharmacy_trademark_director");
groups.getGroup("pharmacy").addAuthority("GROUP_pharmacy_trademark_department");
if(groups.getGroup("pharmacy_un_subdivision") == null) {
    groups.createRootGroup("pharmacy_un_subdivision", "Отдел УН");
}
orgstruct.createTypedGroup("branch", "subdivision", "pharmacy_un_subdivision");
if(groups.getGroup("pharmacy_un_manager") == null) {
    groups.createRootGroup("pharmacy_un_manager", "Начальник отдела УН");
}
orgstruct.createTypedGroup("role", "manager", "pharmacy_un_manager");
if(people.getPerson("fet") == null) {
    people.createPerson("fet", "Афанасий", "Фет", "test@citeck.ru", "fet", true);
}
groups.getGroup("pharmacy_un_manager").addAuthority("fet");
groups.getGroup("pharmacy_un_subdivision").addAuthority("GROUP_pharmacy_un_manager");
groups.getGroup("pharmacy").addAuthority("GROUP_pharmacy_un_subdivision");
if(groups.getGroup("pharmacy_up_department") == null) {
    groups.createRootGroup("pharmacy_up_department", "Департамент УП");
}
orgstruct.createTypedGroup("branch", "department", "pharmacy_up_department");
if(groups.getGroup("pharmacy_up_director") == null) {
    groups.createRootGroup("pharmacy_up_director", "Директор департамента УП");
}
orgstruct.createTypedGroup("role", "manager", "pharmacy_up_director");
if(people.getPerson("fonvisin") == null) {
    people.createPerson("fonvisin", "Денис", "Фонвизин", "test@citeck.ru", "fonvisin", true);
}
groups.getGroup("pharmacy_up_director").addAuthority("fonvisin");
groups.getGroup("pharmacy_up_department").addAuthority("GROUP_pharmacy_up_director");
groups.getGroup("pharmacy").addAuthority("GROUP_pharmacy_up_department");
groups.getGroup("_orgstruct_home_").addAuthority("GROUP_pharmacy");