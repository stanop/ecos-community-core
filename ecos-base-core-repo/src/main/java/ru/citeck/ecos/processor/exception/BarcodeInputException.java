package ru.citeck.ecos.processor.exception;

public class BarcodeInputException extends RuntimeException {

    public BarcodeInputException(String message) {
        super(message);
    }

    public BarcodeInputException(Exception e) {
        super("Cannot extract input value for generate barcode", e);
    }
}
