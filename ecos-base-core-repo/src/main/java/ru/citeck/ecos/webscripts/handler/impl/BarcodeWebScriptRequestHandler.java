package ru.citeck.ecos.webscripts.handler.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.barcode.BarcodeAttributeRegistry;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.utils.StringUtils;
import ru.citeck.ecos.webscripts.handler.WebScriptRequestHandler;

import java.util.HashMap;
import java.util.Map;

import static ru.citeck.ecos.webscripts.barcode.BarcodeImageGet.*;

@Service
public class BarcodeWebScriptRequestHandler implements WebScriptRequestHandler {

    private static final int DEFAULT_BARCODE_HEIGHT = 50;
    private static final int DEFAULT_BARCODE_WIDTH = 100;
    private static final String DEFAULT_BARCODE_TYPE = "code-128";

    private BarcodeAttributeRegistry barcodeAttributeRegistry;

    @Autowired
    public BarcodeWebScriptRequestHandler(BarcodeAttributeRegistry barcodeAttributeRegistry) {
        this.barcodeAttributeRegistry = barcodeAttributeRegistry;
    }

    @Override
    public Map<String, Object> handleRequest(WebScriptRequest req) {

        String nodeRef = req.getParameter(NODE_REF_PARAM);
        String barcodeWidthStr = req.getParameter(BARCODE_WIDTH_PARAM);
        String barcodeHeightStr = req.getParameter(BARCODE_HEIGHT_PARAM);
        String barcodeTypeStr = req.getParameter(BARCODE_TYPE_PARAM);
        String property = req.getParameter(PROPERTY_PARAM);

        ParameterCheck.mandatoryString(NODE_REF_PARAM, nodeRef);

        int barcodeHeight;
        if (barcodeHeightStr == null) {
            barcodeHeight = DEFAULT_BARCODE_HEIGHT;
        } else {
            barcodeHeight = Integer.parseInt(barcodeHeightStr);
        }

        int barcodeWidth;
        if (barcodeWidthStr == null) {
            barcodeWidth = DEFAULT_BARCODE_WIDTH;
        } else {
            barcodeWidth = Integer.parseInt(barcodeWidthStr);
        }

        String barcodeType;
        if (barcodeTypeStr == null) {
            barcodeType = DEFAULT_BARCODE_TYPE;
        } else {
            barcodeType = barcodeTypeStr;
        }

        if (StringUtils.isBlank(property)) {
            RecordRef recordRef = RecordRef.create("", nodeRef);
            property = barcodeAttributeRegistry.getAttribute(recordRef);
        }

        Map<String, Object> input = new HashMap<>();
        input.put(NODE_REF_PARAM, nodeRef);
        input.put(PROPERTY_PARAM, property);
        input.put(BARCODE_HEIGHT_PARAM, barcodeHeight);
        input.put(BARCODE_WIDTH_PARAM, barcodeWidth);
        input.put(BARCODE_TYPE_PARAM, barcodeType);
        return input;
    }

}
