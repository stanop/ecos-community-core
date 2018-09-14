export function t(message) {
    if (!window.Alfresco) {
        return message;
    }
    return window.Alfresco.util.message(message);
}