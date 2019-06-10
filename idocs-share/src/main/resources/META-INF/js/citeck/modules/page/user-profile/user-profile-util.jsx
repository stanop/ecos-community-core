import { utils as CiteckUtils } from 'js/citeck/modules/utils/citeck';

export const PROFILE_TAB = "profile";
export const DOCUMENTS_TAB = "documents";

export function getCurrentTab() {
    return CiteckUtils.getURLParameterByName("tab") || PROFILE_TAB;
}

export function setCurrentTabUrl(tab) {
    CiteckUtils.setURLParameter("tab", tab);
}
