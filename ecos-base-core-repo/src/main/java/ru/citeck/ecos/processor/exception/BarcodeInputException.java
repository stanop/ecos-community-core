package ru.citeck.ecos.processor.exception;

public class BarcodeInputException extends RuntimeException {

    public BarcodeInputException() {
        super("Cannot extract input value for generate barcode");
    }
}
