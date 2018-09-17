export function t(message) {
    if (!message) {
        return '';
    }

    if (!window.Alfresco) {
        return message;
    }

    return window.Alfresco.util.message(message);
}