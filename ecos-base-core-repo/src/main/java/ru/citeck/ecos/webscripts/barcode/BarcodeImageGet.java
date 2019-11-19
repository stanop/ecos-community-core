package ru.citeck.ecos.webscripts.barcode;

import com.google.zxing.BarcodeFormat;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.barcode.BarcodeAttributeRegistry;
import ru.citeck.ecos.processor.exception.BarcodeInputException;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.template.Base64TemplateImageConverter;
import ru.citeck.ecos.utils.NodeUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BarcodeImageGet extends DeclarativeWebScript {

    private static final String PARAM_PROPERTY = "property";
    private static final String PARAM_NODE_REF = "nodeRef";
    private static final String PARAM_BARCODE_WIDTH = "width";
    private static final String PARAM_BARCODE_HEIGHT = "height";

    private BarcodeAttributeRegistry barcodeAttributeRegistry;
    private Base64TemplateImageConverter converter;
    private NodeUtils nodeUtils;
    private NamespacePrefixResolver prefixResolver;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        String nodeRef = req.getParameter(PARAM_NODE_REF);
        String barcodeWidthStr = req.getParameter(PARAM_BARCODE_WIDTH);
        String barcodeHeightStr = req.getParameter(PARAM_BARCODE_HEIGHT);
        String property = req.getParameter(PARAM_PROPERTY);

        if (nodeRef == null) {
            status.setCode(Status.STATUS_BAD_REQUEST, PARAM_NODE_REF + " should be set");
            return null;
        }
        int barcodeHeight;
        if (barcodeHeightStr == null) {
            barcodeHeight = 50;
        } else {
            barcodeHeight = Integer.parseInt(barcodeHeightStr);
        }
        int barcodeWidth;
        if (barcodeWidthStr == null) {
            barcodeWidth = 100;
        } else {
            barcodeWidth = Integer.parseInt(barcodeWidthStr);
        }

        if (property == null || property.isEmpty()) {
            property = getProperty(nodeRef);
        }

        String barcodeInput;
        try {
            barcodeInput = nodeUtils.getProperty(nodeUtils.getNodeRef(nodeRef), QName.resolveToQName(prefixResolver, property));
        } catch (Exception e) {
            throw new BarcodeInputException();
        }

        String result = converter.fromBarcode(barcodeInput, barcodeWidth, barcodeHeight, BarcodeFormat.CODE_128);
        Map<String, Object> model = new HashMap<>();
        model.put("image", result);
        return model;
    }

    private String getProperty(String nodeRef) {
        RecordRef recordRef = RecordRef.create("", nodeRef);
        return barcodeAttributeRegistry.getAttribute(recordRef);
    }

    public void setBarcodeAttributeRegistry(BarcodeAttributeRegistry barcodeAttributeRegistry) {
        this.barcodeAttributeRegistry = barcodeAttributeRegistry;
    }

    @Autowired
    public void setConverter(Base64TemplateImageConverter converter) {
        this.converter = converter;
    }

    public void setNodeUtils(NodeUtils nodeUtils) {
        this.nodeUtils = nodeUtils;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }
}
