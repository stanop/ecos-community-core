
type = orgmeta.createSubType("branch", "company");
type.properties["cm:title"] = "Организация";
type.save();
type = orgmeta.createSubType("branch", "department");
type.properties["cm:title"] = "Департамент";
type.save();
type = orgmeta.createSubType("branch", "division");
type.properties["cm:title"] = "Отдел";
type.save();
type = orgmeta.createSubType("branch", "branch");
type = orgmeta.createSubType("branch", "subdivision");
type = orgmeta.createSubType("branch", "organization");
type = orgmeta.createSubType("branch", "section");
type = orgmeta.createSubType("role", "director-general");
type.properties["cm:title"] = "director-general";
type.properties["org:roleIsManager"] = true;
type.save();
type = orgmeta.createSubType("role", "department_head");
type.properties["org:roleIsManager"] = false;
type.save();
type = orgmeta.createSubType("role", "staff");
type.properties["org:roleIsManager"] = false;
type.save();
type = orgmeta.createSubType("role", "technical_director");
type.properties["org:roleIsManager"] = false;
type.save();
type = orgmeta.createSubType("role", "branch_manager");
type.properties["org:roleIsManager"] = false;
type.save();
type = orgmeta.createSubType("role", "manager");
type.properties["cm:title"] = "Руководитель";
type.properties["org:roleIsManager"] = true;
type.save();
type = orgmeta.createSubType("role", "employee");
type.properties["cm:title"] = "Работник";
type.properties["org:roleIsManager"] = false;
type.save();
type = orgmeta.createSubType("role", "executive_director");
type.properties["org:roleIsManager"] = false;
type.save();
type = orgmeta.createSubType("role", "director");
type.properties["cm:title"] = "Директор";
type.properties["org:roleIsManager"] = true;
type.save();
if(groups.getGroup("_orgstruct_home_") == null) {
groups.createRootGroup("_orgstruct_home_", "_orgstruct_home_");
}
if(groups.getGroup("company1") == null) {
groups.createRootGroup("company1", "Организация");
}
orgstruct.createTypedGroup("branch", "company", "company1");
if(groups.getGroup("department1") == null) {
groups.createRootGroup("department1", "Бухгалтерия");
}
orgstruct.createTypedGroup("branch", "department", "department1");
if(groups.getGroup("employee2") == null) {
groups.createRootGroup("employee2", "Бухгалтер");
}
orgstruct.createTypedGroup("role", "employee", "employee2");
if(people.getPerson("griboedov") == null) {
people.createPerson("griboedov", "Александр", "Грибоедов", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee2").addAuthority("griboedov");
groups.getGroup("department1").addAuthority("GROUP_employee2");
if(groups.getGroup("manager2") == null) {
groups.createRootGroup("manager2", "Главный бухгалтер");
}
orgstruct.createTypedGroup("role", "manager", "manager2");
if(people.getPerson("esenin") == null) {
people.createPerson("esenin", "Сергей", "Есенин", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager2").addAuthority("esenin");
groups.getGroup("department1").addAuthority("GROUP_manager2");
groups.getGroup("company1").addAuthority("GROUP_department1");
if(groups.getGroup("department2") == null) {
groups.createRootGroup("department2", "Отдел делопроизводства");
}
orgstruct.createTypedGroup("branch", "department", "department2");
if(groups.getGroup("employee3") == null) {
groups.createRootGroup("employee3", "Делопроизводитель");
}
orgstruct.createTypedGroup("role", "employee", "employee3");
if(people.getPerson("nekrasov") == null) {
people.createPerson("nekrasov", "Николай", "Некрасов", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee3").addAuthority("nekrasov");
groups.getGroup("department2").addAuthority("GROUP_employee3");
if(groups.getGroup("employee4") == null) {
groups.createRootGroup("employee4", "Архивариус");
}
orgstruct.createTypedGroup("role", "employee", "employee4");
if(people.getPerson("leskov") == null) {
people.createPerson("leskov", "Николай", "Лесков", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee4").addAuthority("leskov");
groups.getGroup("department2").addAuthority("GROUP_employee4");
if(groups.getGroup("manager3") == null) {
groups.createRootGroup("manager3", "Руководитель отдела делопроизводства");
}
orgstruct.createTypedGroup("role", "manager", "manager3");
if(people.getPerson("fonvisin") == null) {
people.createPerson("fonvisin", "Денис", "Фонвизин", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager3").addAuthority("fonvisin");
groups.getGroup("department2").addAuthority("GROUP_manager3");
groups.getGroup("company1").addAuthority("GROUP_department2");
if(groups.getGroup("department3") == null) {
groups.createRootGroup("department3", "Юридический отдел");
}
orgstruct.createTypedGroup("branch", "department", "department3");
if(groups.getGroup("department4") == null) {
groups.createRootGroup("department4", "Договорной отдел");
}
orgstruct.createTypedGroup("branch", "department", "department4");
if(groups.getGroup("employee8") == null) {
groups.createRootGroup("employee8", "Юрисконсульт");
}
orgstruct.createTypedGroup("role", "employee", "employee8");
if(people.getPerson("karamzin") == null) {
people.createPerson("karamzin", "Николай", "Карамзин", "test@test.ru", "one two three", true);
}
groups.getGroup("employee8").addAuthority("karamzin");
groups.getGroup("department4").addAuthority("GROUP_employee8");
if(groups.getGroup("manager9") == null) {
groups.createRootGroup("manager9", "Руководитель договорного отдела");
}
orgstruct.createTypedGroup("role", "manager", "manager9");
if(people.getPerson("pasternak") == null) {
people.createPerson("pasternak", "Борис", "Пастернак", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager9").addAuthority("pasternak");
groups.getGroup("department4").addAuthority("GROUP_manager9");
groups.getGroup("department3").addAuthority("GROUP_department4");
if(groups.getGroup("employee7") == null) {
groups.createRootGroup("employee7", "Юрист");
}
orgstruct.createTypedGroup("role", "employee", "employee7");
if(people.getPerson("gogol") == null) {
people.createPerson("gogol", "Николай", "Гоголь", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee7").addAuthority("gogol");
groups.getGroup("department3").addAuthority("GROUP_employee7");
if(groups.getGroup("manager8") == null) {
groups.createRootGroup("manager8", "Руководитель юридического отдела");
}
orgstruct.createTypedGroup("role", "manager", "manager8");
if(people.getPerson("pasternak") == null) {
people.createPerson("pasternak", "Борис", "Пастернак", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager8").addAuthority("pasternak");
groups.getGroup("department3").addAuthority("GROUP_manager8");
groups.getGroup("company1").addAuthority("GROUP_department3");
if(groups.getGroup("department5") == null) {
groups.createRootGroup("department5", "Служба безопасности");
}
orgstruct.createTypedGroup("branch", "department", "department5");
if(groups.getGroup("department8") == null) {
groups.createRootGroup("department8", "Отдел экономической безопасности");
}
orgstruct.createTypedGroup("branch", "department", "department8");
if(groups.getGroup("manager6") == null) {
groups.createRootGroup("manager6", "Руководитель отдела экономической безопасности");
}
orgstruct.createTypedGroup("role", "manager", "manager6");
if(people.getPerson("lermontov") == null) {
people.createPerson("lermontov", "Михаил", "Лермонтов", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager6").addAuthority("lermontov");
groups.getGroup("department8").addAuthority("GROUP_manager6");
groups.getGroup("department5").addAuthority("GROUP_department8");
if(groups.getGroup("manager5") == null) {
groups.createRootGroup("manager5", "Руководитель службы безопасности");
}
orgstruct.createTypedGroup("role", "manager", "manager5");
if(people.getPerson("dostoyevsky") == null) {
people.createPerson("dostoyevsky", "Фёдор", "Достоевский", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager5").addAuthority("dostoyevsky");
groups.getGroup("department5").addAuthority("GROUP_manager5");
groups.getGroup("company1").addAuthority("GROUP_department5");
if(groups.getGroup("department6") == null) {
groups.createRootGroup("department6", "Отдел кадров");
}
orgstruct.createTypedGroup("branch", "department", "department6");
if(groups.getGroup("employee5") == null) {
groups.createRootGroup("employee5", "Менеджер по управлению персоналом");
}
orgstruct.createTypedGroup("role", "employee", "employee5");
if(people.getPerson("bunin") == null) {
people.createPerson("bunin", "Иван", "Бунин", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee5").addAuthority("bunin");
groups.getGroup("department6").addAuthority("GROUP_employee5");
if(groups.getGroup("manager4") == null) {
groups.createRootGroup("manager4", "Руководитель отдела кадров");
}
orgstruct.createTypedGroup("role", "manager", "manager4");
if(people.getPerson("bulgakov") == null) {
people.createPerson("bulgakov", "Михаил", "Булгаков", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager4").addAuthority("bulgakov");
groups.getGroup("department6").addAuthority("GROUP_manager4");
groups.getGroup("company1").addAuthority("GROUP_department6");
if(groups.getGroup("department7") == null) {
groups.createRootGroup("department7", "Финансово-экономический отдел");
}
orgstruct.createTypedGroup("branch", "department", "department7");
if(groups.getGroup("employee6") == null) {
groups.createRootGroup("employee6", "Экономист");
}
orgstruct.createTypedGroup("role", "employee", "employee6");
if(people.getPerson("nekrasov") == null) {
people.createPerson("nekrasov", "Николай", "Некрасов", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee6").addAuthority("nekrasov");
groups.getGroup("department7").addAuthority("GROUP_employee6");
if(groups.getGroup("manager7") == null) {
groups.createRootGroup("manager7", "Руководитель финансово-экономического отдела");
}
orgstruct.createTypedGroup("role", "manager", "manager7");
if(people.getPerson("fet") == null) {
people.createPerson("fet", "Афанасий", "Фет", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager7").addAuthority("fet");
groups.getGroup("department7").addAuthority("GROUP_manager7");
groups.getGroup("company1").addAuthority("GROUP_department7");
if(groups.getGroup("department9") == null) {
groups.createRootGroup("department9", "Отдел информационных технологий");
}
orgstruct.createTypedGroup("branch", "department", "department9");
if(groups.getGroup("employee1") == null) {
groups.createRootGroup("employee1", "Технический специалист");
}
orgstruct.createTypedGroup("role", "employee", "employee1");
if(people.getPerson("blok") == null) {
people.createPerson("blok", "Александр", "Блок", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee1").addAuthority("blok");
groups.getGroup("department9").addAuthority("GROUP_employee1");
if(groups.getGroup("manager10") == null) {
groups.createRootGroup("manager10", "Руководитель отдела информационных технологий");
}
orgstruct.createTypedGroup("role", "manager", "manager10");
if(people.getPerson("ahmatova") == null) {
people.createPerson("ahmatova", "Анна", "Ахматова", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager10").addAuthority("ahmatova");
groups.getGroup("department9").addAuthority("GROUP_manager10");
groups.getGroup("company1").addAuthority("GROUP_department9");
if(groups.getGroup("manager1") == null) {
groups.createRootGroup("manager1", "Генеральный директор");
}
orgstruct.createTypedGroup("role", "director-general", "manager1");
if(people.getPerson("pushkin") == null) {
people.createPerson("pushkin", "Александр", "Пушкин", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager1").addAuthority("pushkin");
groups.getGroup("company1").addAuthority("GROUP_manager1");
groups.getGroup("_orgstruct_home_").addAuthority("GROUP_company1");
