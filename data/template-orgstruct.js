type = orgmeta.createSubType("branch", "company");
type.properties["cm:title"] = "Организация";
type.save();
type = orgmeta.createSubType("branch", "department");
type.properties["cm:title"] = "Департамент";
type.save();
type = orgmeta.createSubType("branch", "division");
type.properties["cm:title"] = "Отдел";
type.save();
type = orgmeta.createSubType("role", "director");
type.properties["cm:title"] = "Директор";
type.properties["org:roleIsManager"] = true;
type.save();
type = orgmeta.createSubType("role", "manager");
type.properties["cm:title"] = "Руководитель";
type.properties["org:roleIsManager"] = true;
type.save();
type = orgmeta.createSubType("role", "employee");
type.properties["cm:title"] = "Работник";
type.properties["org:roleIsManager"] = false;
type.save();
if(groups.getGroup("_orgstruct_home_") == null) {
groups.createRootGroup("_orgstruct_home_", "_orgstruct_home_");
}
if(groups.getGroup("company") == null) {
groups.createRootGroup("company", "Организация");
}
orgstruct.createTypedGroup("branch", "company", "company");
if(groups.getGroup("company_accountancy") == null) {
groups.createRootGroup("company_accountancy", "Бухгалтерия");
}
orgstruct.createTypedGroup("branch", "department", "company_accountancy");
if(groups.getGroup("company_chief_accountant") == null) {
groups.createRootGroup("company_chief_accountant", "Главный бухгалтер");
}
orgstruct.createTypedGroup("role", "manager", "company_chief_accountant");
groups.getGroup("company_accountancy").addAuthority("GROUP_company_chief_accountant");
groups.getGroup("company").addAuthority("GROUP_company_accountancy");
if(groups.getGroup("company_director") == null) {
groups.createRootGroup("company_director", "Директор");
}
orgstruct.createTypedGroup("role", "director", "company_director");
groups.getGroup("company").addAuthority("GROUP_company_director");
groups.getGroup("_orgstruct_home_").addAuthority("GROUP_company");