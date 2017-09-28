package ru.citeck.ecos.cmmn;

/**
 * @author deathNC
 */
public class CmmnExportImportException extends RuntimeException {

    public CmmnExportImportException() {
    }

    public CmmnExportImportException(String message) {
        super(message);
    }

    public CmmnExportImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public CmmnExportImportException(Throwable cause) {
        super(cause);
    }

    public CmmnExportImportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
