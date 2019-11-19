package ru.citeck.ecos.barcode.exception;

public class UnsupportedBarcodeTypeException extends RuntimeException{

    public UnsupportedBarcodeTypeException(String barcodeType) {
        super("Unsupported barcode type: " + barcodeType);
    }
}
