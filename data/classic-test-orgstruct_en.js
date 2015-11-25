
type = orgmeta.createSubType("branch", "company");
type.properties["cm:title"] = "Company";
type.save();
type = orgmeta.createSubType("branch", "department");
type.properties["cm:title"] = "Department";
type.save();
type = orgmeta.createSubType("branch", "division");
type.properties["cm:title"] = "Division";
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
type.properties["cm:title"] = "Manager";
type.properties["org:roleIsManager"] = true;
type.save();
type = orgmeta.createSubType("role", "employee");
type.properties["cm:title"] = "Employee";
type.properties["org:roleIsManager"] = false;
type.save();
type = orgmeta.createSubType("role", "executive_director");
type.properties["org:roleIsManager"] = false;
type.save();
type = orgmeta.createSubType("role", "director");
type.properties["cm:title"] = "Director";
type.properties["org:roleIsManager"] = true;
type.save();
if(groups.getGroup("_orgstruct_home_") == null) {
groups.createRootGroup("_orgstruct_home_", "_orgstruct_home_");
}
if(groups.getGroup("company1") == null) {
groups.createRootGroup("company1", "Company");
}
orgstruct.createTypedGroup("branch", "company", "company1");
if(groups.getGroup("department1") == null) {
groups.createRootGroup("department1", "Accountancy");
}
orgstruct.createTypedGroup("branch", "department", "department1");
if(groups.getGroup("employee2") == null) {
groups.createRootGroup("employee2", "Accountant");
}
orgstruct.createTypedGroup("role", "employee", "employee2");
if(people.getPerson("griboedov") == null) {
people.createPerson("griboedov", "Alexander", "Griboedov", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee2").addAuthority("griboedov");
groups.getGroup("department1").addAuthority("GROUP_employee2");
if(groups.getGroup("manager2") == null) {
groups.createRootGroup("manager2", "Chief Accountant");
}
orgstruct.createTypedGroup("role", "manager", "manager2");
if(people.getPerson("esenin") == null) {
people.createPerson("esenin", "Sergey", "Esenin", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager2").addAuthority("esenin");
groups.getGroup("department1").addAuthority("GROUP_manager2");
groups.getGroup("company1").addAuthority("GROUP_department1");
if(groups.getGroup("department2") == null) {
groups.createRootGroup("department2", "Clerks department");
}
orgstruct.createTypedGroup("branch", "department", "department2");
if(groups.getGroup("employee3") == null) {
groups.createRootGroup("employee3", "Clerk");
}
orgstruct.createTypedGroup("role", "employee", "employee3");
if(people.getPerson("nekrasov") == null) {
people.createPerson("nekrasov", "Nikolay", "Nekrasov", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee3").addAuthority("nekrasov");
groups.getGroup("department2").addAuthority("GROUP_employee3");
if(groups.getGroup("employee4") == null) {
groups.createRootGroup("employee4", "Archivist");
}
orgstruct.createTypedGroup("role", "employee", "employee4");
if(people.getPerson("leskov") == null) {
people.createPerson("leskov", "Nikolay", "Leskov", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee4").addAuthority("leskov");
groups.getGroup("department2").addAuthority("GROUP_employee4");
if(groups.getGroup("manager3") == null) {
groups.createRootGroup("manager3", "Clerks department manager");
}
orgstruct.createTypedGroup("role", "manager", "manager3");
if(people.getPerson("fonvisin") == null) {
people.createPerson("fonvisin", "Denis", "Fonvisin", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager3").addAuthority("fonvisin");
groups.getGroup("department2").addAuthority("GROUP_manager3");
groups.getGroup("company1").addAuthority("GROUP_department2");
if(groups.getGroup("department3") == null) {
groups.createRootGroup("department3", "Legal department");
}
orgstruct.createTypedGroup("branch", "department", "department3");
if(groups.getGroup("department4") == null) {
groups.createRootGroup("department4", "Contract deprartment");
}
orgstruct.createTypedGroup("branch", "department", "department4");
if(groups.getGroup("employee8") == null) {
groups.createRootGroup("employee8", "Legal expert");
}
orgstruct.createTypedGroup("role", "employee", "employee8");
if(people.getPerson("karamzin") == null) {
people.createPerson("karamzin", "Nikolay", "Karamzin", "test@test.ru", "one two three", true);
}
groups.getGroup("employee8").addAuthority("karamzin");
groups.getGroup("department4").addAuthority("GROUP_employee8");
if(groups.getGroup("manager9") == null) {
groups.createRootGroup("manager9", "Contract department manager");
}
orgstruct.createTypedGroup("role", "manager", "manager9");
if(people.getPerson("pasternak") == null) {
people.createPerson("pasternak", "Boris", "Pasternak", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager9").addAuthority("pasternak");
groups.getGroup("department4").addAuthority("GROUP_manager9");
groups.getGroup("department3").addAuthority("GROUP_department4");
if(groups.getGroup("employee7") == null) {
groups.createRootGroup("employee7", "Lawyer");
}
orgstruct.createTypedGroup("role", "employee", "employee7");
if(people.getPerson("gogol") == null) {
people.createPerson("gogol", "Nikolay", "Gogol", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee7").addAuthority("gogol");
groups.getGroup("department3").addAuthority("GROUP_employee7");
if(groups.getGroup("manager8") == null) {
groups.createRootGroup("manager8", "Legal department manager");
}
orgstruct.createTypedGroup("role", "manager", "manager8");
if(people.getPerson("pasternak") == null) {
people.createPerson("pasternak", "Boris", "Pasternak", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager8").addAuthority("pasternak");
groups.getGroup("department3").addAuthority("GROUP_manager8");
groups.getGroup("company1").addAuthority("GROUP_department3");
if(groups.getGroup("department5") == null) {
groups.createRootGroup("department5", "Security service");
}
orgstruct.createTypedGroup("branch", "department", "department5");
if(groups.getGroup("department8") == null) {
groups.createRootGroup("department8", "Economic security department");
}
orgstruct.createTypedGroup("branch", "department", "department8");
if(groups.getGroup("manager6") == null) {
groups.createRootGroup("manager6", "Economic security department manager");
}
orgstruct.createTypedGroup("role", "manager", "manager6");
if(people.getPerson("lermontov") == null) {
people.createPerson("lermontov", "Mikhail", "LermontovЛермонтов", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager6").addAuthority("lermontov");
groups.getGroup("department8").addAuthority("GROUP_manager6");
groups.getGroup("department5").addAuthority("GROUP_department8");
if(groups.getGroup("manager5") == null) {
groups.createRootGroup("manager5", "Security service manager");
}
orgstruct.createTypedGroup("role", "manager", "manager5");
if(people.getPerson("dostoyevsky") == null) {
people.createPerson("dostoyevsky", "Fedor", "Dostoevskiy", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager5").addAuthority("dostoyevsky");
groups.getGroup("department5").addAuthority("GROUP_manager5");
groups.getGroup("company1").addAuthority("GROUP_department5");
if(groups.getGroup("department6") == null) {
groups.createRootGroup("department6", "HR department");
}
orgstruct.createTypedGroup("branch", "department", "department6");
if(groups.getGroup("employee5") == null) {
groups.createRootGroup("employee5", "HR manager");
}
orgstruct.createTypedGroup("role", "employee", "employee5");
if(people.getPerson("bunin") == null) {
people.createPerson("bunin", "Ivan", "Bunin", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee5").addAuthority("bunin");
groups.getGroup("department6").addAuthority("GROUP_employee5");
if(groups.getGroup("manager4") == null) {
groups.createRootGroup("manager4", "HR department director");
}
orgstruct.createTypedGroup("role", "manager", "manager4");
if(people.getPerson("bulgakov") == null) {
people.createPerson("bulgakov", "Mikhail", "Bulgakov", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager4").addAuthority("bulgakov");
groups.getGroup("department6").addAuthority("GROUP_manager4");
groups.getGroup("company1").addAuthority("GROUP_department6");
if(groups.getGroup("department7") == null) {
groups.createRootGroup("department7", "Financial economic department");
}
orgstruct.createTypedGroup("branch", "department", "department7");
if(groups.getGroup("employee6") == null) {
groups.createRootGroup("employee6", "Economist");
}
orgstruct.createTypedGroup("role", "employee", "employee6");
if(people.getPerson("nekrasov") == null) {
people.createPerson("nekrasov", "Nikolay", "Nekrasov", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee6").addAuthority("nekrasov");
groups.getGroup("department7").addAuthority("GROUP_employee6");
if(groups.getGroup("manager7") == null) {
groups.createRootGroup("manager7", "Financial economic department manager");
}
orgstruct.createTypedGroup("role", "manager", "manager7");
if(people.getPerson("fet") == null) {
people.createPerson("fet", "Afanasiy", "Fet", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager7").addAuthority("fet");
groups.getGroup("department7").addAuthority("GROUP_manager7");
groups.getGroup("company1").addAuthority("GROUP_department7");
if(groups.getGroup("department9") == null) {
groups.createRootGroup("department9", "IT department");
}
orgstruct.createTypedGroup("branch", "department", "department9");
if(groups.getGroup("employee1") == null) {
groups.createRootGroup("employee1", "Technical specialist");
}
orgstruct.createTypedGroup("role", "employee", "employee1");
if(people.getPerson("blok") == null) {
people.createPerson("blok", "Alexander", "Blok", "test@citeck.ru", "one two three", true);
}
groups.getGroup("employee1").addAuthority("blok");
groups.getGroup("department9").addAuthority("GROUP_employee1");
if(groups.getGroup("manager10") == null) {
groups.createRootGroup("manager10", "IT department manager");
}
orgstruct.createTypedGroup("role", "manager", "manager10");
if(people.getPerson("ahmatova") == null) {
people.createPerson("ahmatova", "Anna", "Akhmatova", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager10").addAuthority("ahmatova");
groups.getGroup("department9").addAuthority("GROUP_manager10");
groups.getGroup("company1").addAuthority("GROUP_department9");
if(groups.getGroup("manager1") == null) {
groups.createRootGroup("manager1", "CEO");
}
orgstruct.createTypedGroup("role", "director-general", "manager1");
if(people.getPerson("pushkin") == null) {
people.createPerson("pushkin", "Alexander", "Pushkin", "test@citeck.ru", "one two three", true);
}
groups.getGroup("manager1").addAuthority("pushkin");
groups.getGroup("company1").addAuthority("GROUP_manager1");
groups.getGroup("_orgstruct_home_").addAuthority("GROUP_company1");
