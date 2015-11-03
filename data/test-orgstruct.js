orgmeta.createSubType("branch", "organization");
orgmeta.createSubType("branch", "section");
orgmeta.createSubType("branch", "branch");
orgmeta.createSubType("branch", "division");
orgmeta.createSubType("branch", "department");
orgmeta.createSubType("branch", "subdivision");
managers = [];
managers.push(orgmeta.createSubType("role", "director general"));
managers.push(orgmeta.createSubType("role", "leader_section"));
managers.push(orgmeta.createSubType("role", "leader_branch"));
orgmeta.createSubType("role", "collaborator_section");
managers.push(orgmeta.createSubType("role", "manager"));
orgmeta.createSubType("role", "employee");

for(var i in managers) {
    managers[i].properties['org:roleIsManager'] = true;
    managers[i].save();
}

if(groups.getGroup("_orgstruct_home_") == null) {
 groups.createRootGroup("_orgstruct_home_", "_orgstruct_home_");
}
if(groups.getGroup("organization_test") == null) {
 groups.createRootGroup("organization_test", "Организация");
}
orgstruct.createTypedGroup("branch", "organization", "organization_test");
if(groups.getGroup("branch_test1") == null) {
 groups.createRootGroup("branch_test1", "Филиал Москва");
}
orgstruct.createTypedGroup("branch", "branch", "branch_test1");
if(groups.getGroup("leader_branch_test1") == null) {
 groups.createRootGroup("leader_branch_test1", "Руководитель филиала Москва");
}
orgstruct.createTypedGroup("role", "leader_branch", "leader_branch_test1");
if(people.getPerson("admin") == null) {
 people.createPerson("admin", "Administrator", "", "admin@alfresco.com", "one two three", true);
}
groups.getGroup("leader_branch_test1").addAuthority("admin");
if(people.getPerson("watson") == null) {
 people.createPerson("watson", "Джон", "Ватсон", "test@citeck.ru", "one two three", true);
}
groups.getGroup("leader_branch_test1").addAuthority("watson");
groups.getGroup("branch_test1").addAuthority("GROUP_leader_branch_test1");
if(groups.getGroup("section1_branch_test1") == null) {
 groups.createRootGroup("section1_branch_test1", "Экономический отдел");
}
orgstruct.createTypedGroup("branch", "section", "section1_branch_test1");
if(groups.getGroup("collaborator_section1_branch_test1") == null) {
 groups.createRootGroup("collaborator_section1_branch_test1", "Сотрудники экономического отдела");
}
orgstruct.createTypedGroup("role", "collaborator_section", "collaborator_section1_branch_test1");
if(people.getPerson("holmes") == null) {
 people.createPerson("holmes", "Шерлок", "Холмс", "test@citeck.ru", "one two three", true);
}
groups.getGroup("collaborator_section1_branch_test1").addAuthority("holmes");
groups.getGroup("section1_branch_test1").addAuthority("GROUP_collaborator_section1_branch_test1");
if(groups.getGroup("leader_section1_branch_test1") == null) {
 groups.createRootGroup("leader_section1_branch_test1", "Руководитель экономического отдела (Москва)");
}
orgstruct.createTypedGroup("role", "leader_section", "leader_section1_branch_test1");
if(people.getPerson("poirot") == null) {
 people.createPerson("poirot", "Эркюль", "Пуаро", "test@citeck.ru", "one two three", true);
}
groups.getGroup("leader_section1_branch_test1").addAuthority("poirot");
groups.getGroup("section1_branch_test1").addAuthority("GROUP_leader_section1_branch_test1");
if(groups.getGroup("section1_1_branch_test1") == null) {
 groups.createRootGroup("section1_1_branch_test1", "Бухгалтерия");
}
orgstruct.createTypedGroup("branch", "section", "section1_1_branch_test1");
if(groups.getGroup("collaborator_section1_1_branch_test1") == null) {
 groups.createRootGroup("collaborator_section1_1_branch_test1", "Сотрудники бухгалтерии");
}
orgstruct.createTypedGroup("role", "collaborator_section", "collaborator_section1_1_branch_test1");
if(people.getPerson("lemon") == null) {
 people.createPerson("lemon", "Фелисити", "Лемон", "test@citeck.ru", "one two three", true);
}
groups.getGroup("collaborator_section1_1_branch_test1").addAuthority("lemon");
groups.getGroup("section1_1_branch_test1").addAuthority("GROUP_collaborator_section1_1_branch_test1");
if(groups.getGroup("leader_section1_1_branch_test1") == null) {
 groups.createRootGroup("leader_section1_1_branch_test1", "Главный бухгалтер");
}
orgstruct.createTypedGroup("role", "leader_section", "leader_section1_1_branch_test1");
if(people.getPerson("marple") == null) {
 people.createPerson("marple", "Джейн", "Марпл", "test@citeck.ru", "one two three", true);
}
groups.getGroup("leader_section1_1_branch_test1").addAuthority("marple");
groups.getGroup("section1_1_branch_test1").addAuthority("GROUP_leader_section1_1_branch_test1");
groups.getGroup("section1_branch_test1").addAuthority("GROUP_section1_1_branch_test1");
groups.getGroup("branch_test1").addAuthority("GROUP_section1_branch_test1");
if(groups.getGroup("section2_branch_test1") == null) {
 groups.createRootGroup("section2_branch_test1", "Служба делопроизводства (Москва)");
}
orgstruct.createTypedGroup("branch", "section", "section2_branch_test1");
if(groups.getGroup("collaborator_section2_branch_test1") == null) {
 groups.createRootGroup("collaborator_section2_branch_test1", "Сотрудники службы делопроизводства (Москва)");
}
orgstruct.createTypedGroup("role", "collaborator_section", "collaborator_section2_branch_test1");
if(people.getPerson("admin") == null) {
 people.createPerson("admin", "Administrator", "", "admin@alfresco.com", "one two three", true);
}
groups.getGroup("collaborator_section2_branch_test1").addAuthority("admin");
if(people.getPerson("wolfe") == null) {
 people.createPerson("wolfe", "Ниро", "Вульф", "test@citeck.ru", "one two three", true);
}
groups.getGroup("collaborator_section2_branch_test1").addAuthority("wolfe");
groups.getGroup("section2_branch_test1").addAuthority("GROUP_collaborator_section2_branch_test1");
groups.getGroup("branch_test1").addAuthority("GROUP_section2_branch_test1");
if(groups.getGroup("section3_branch_test1") == null) {
 groups.createRootGroup("section3_branch_test1", "УФОиО");
}
orgstruct.createTypedGroup("branch", "section", "section3_branch_test1");
if(groups.getGroup("collaborator_section3_branch_test1") == null) {
 groups.createRootGroup("collaborator_section3_branch_test1", "Сотрудники УФОиО");
}
orgstruct.createTypedGroup("role", "collaborator_section", "collaborator_section3_branch_test1");
if(people.getPerson("oliver") == null) {
 people.createPerson("oliver", "Ариадна", "Оливер", "test@citeck.ru", "one two three", true);
}
groups.getGroup("collaborator_section3_branch_test1").addAuthority("oliver");
groups.getGroup("section3_branch_test1").addAuthority("GROUP_collaborator_section3_branch_test1");
groups.getGroup("branch_test1").addAuthority("GROUP_section3_branch_test1");
if(groups.getGroup("section4_branch_test1") == null) {
 groups.createRootGroup("section4_branch_test1", "Служба внутреннего контроля");
}
orgstruct.createTypedGroup("branch", "branch", "section4_branch_test1");
if(groups.getGroup("collaborator_section4_branch_test1") == null) {
 groups.createRootGroup("collaborator_section4_branch_test1", "Сотрудники СВК");
}
orgstruct.createTypedGroup("role", "collaborator_section", "collaborator_section4_branch_test1");
if(people.getPerson("morstan") == null) {
 people.createPerson("morstan", "Мэри", "Морстен", "test@citeck.ru", "one two three", true);
}
groups.getGroup("collaborator_section4_branch_test1").addAuthority("morstan");
groups.getGroup("section4_branch_test1").addAuthority("GROUP_collaborator_section4_branch_test1");
groups.getGroup("branch_test1").addAuthority("GROUP_section4_branch_test1");
if(people.getPerson("admin") == null) {
 people.createPerson("admin", "Administrator", "", "admin@alfresco.com", "one two three", true);
}
groups.getGroup("branch_test1").addAuthority("admin");
groups.getGroup("organization_test").addAuthority("GROUP_branch_test1");
if(groups.getGroup("branch_test2") == null) {
 groups.createRootGroup("branch_test2", "Филиал Тверь");
}
orgstruct.createTypedGroup("branch", "branch", "branch_test2");
if(groups.getGroup("leader_branch_test2") == null) {
 groups.createRootGroup("leader_branch_test2", "Руководитель филиала Тверь");
}
orgstruct.createTypedGroup("role", "leader_branch", "leader_branch_test2");
if(people.getPerson("hudson") == null) {
 people.createPerson("hudson", "Миссис", "Хадсон", "test@citeck.ru", "one two three", true);
}
groups.getGroup("leader_branch_test2").addAuthority("hudson");
groups.getGroup("branch_test2").addAuthority("GROUP_leader_branch_test2");
if(groups.getGroup("section2_branch_test2") == null) {
 groups.createRootGroup("section2_branch_test2", "Юридический отел");
}
orgstruct.createTypedGroup("branch", "section", "section2_branch_test2");
if(groups.getGroup("collaborator_section2_branch_test2") == null) {
 groups.createRootGroup("collaborator_section2_branch_test2", "Сотрудники юридического отдела");
}
orgstruct.createTypedGroup("role", "collaborator_section", "collaborator_section2_branch_test2");
if(people.getPerson("hastings") == null) {
 people.createPerson("hastings", "Артур", "Гастингс", "test@citeck.ru", "one two three", true);
}
groups.getGroup("collaborator_section2_branch_test2").addAuthority("hastings");
groups.getGroup("section2_branch_test2").addAuthority("GROUP_collaborator_section2_branch_test2");
if(groups.getGroup("leader_section2_branch_test2") == null) {
 groups.createRootGroup("leader_section2_branch_test2", "Руководитель юридического отдела (Тверь)");
}
orgstruct.createTypedGroup("role", "leader_section", "leader_section2_branch_test2");
if(people.getPerson("moriarty") == null) {
 people.createPerson("moriarty", "Джеймс", "Мориарти", "test@citeck.ru", "one two three", true);
}
groups.getGroup("leader_section2_branch_test2").addAuthority("moriarty");
groups.getGroup("section2_branch_test2").addAuthority("GROUP_leader_section2_branch_test2");
groups.getGroup("branch_test2").addAuthority("GROUP_section2_branch_test2");
groups.getGroup("organization_test").addAuthority("GROUP_branch_test2");
if(groups.getGroup("director_test") == null) {
 groups.createRootGroup("director_test", "Ген. директор");
}
orgstruct.createTypedGroup("role", "director general", "director_test");
if(people.getPerson("pyne") == null) {
 people.createPerson("pyne", "Паркер", "Пайн", "test@citeck.ru", "one two three", true);
}
groups.getGroup("director_test").addAuthority("pyne");
groups.getGroup("organization_test").addAuthority("GROUP_director_test");
groups.getGroup("_orgstruct_home_").addAuthority("GROUP_organization_test");
