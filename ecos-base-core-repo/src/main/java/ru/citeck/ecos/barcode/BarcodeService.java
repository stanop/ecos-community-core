package ru.citeck.ecos.barcode;

import com.google.zxing.BarcodeFormat;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.processor.exception.BarcodeInputException;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.template.Base64TemplateImageConverter;
import ru.citeck.ecos.utils.NodeUtils;

@Service
public class BarcodeService {

    private NodeUtils nodeUtils;
    private NamespacePrefixResolver prefixResolver;
    private Base64TemplateImageConverter base64ImageConverter;
    private BarcodeAttributeRegistry barcodeAttributeRegistry;

    @Autowired
    public BarcodeService(NodeUtils nodeUtils,
                          @Qualifier("namespaceService") NamespacePrefixResolver prefixResolver,
                          Base64TemplateImageConverter base64ImageConverter,
                          BarcodeAttributeRegistry barcodeAttributeRegistry) {
        this.nodeUtils = nodeUtils;
        this.prefixResolver = prefixResolver;
        this.base64ImageConverter = base64ImageConverter;
        this.barcodeAttributeRegistry = barcodeAttributeRegistry;
    }

    public String getBarcodeAsBase64(String nodeRef, String propertyName, int width, int height, String barcodetype) {
        String barcodePropertyValue;
        try {
            NodeRef targetNodeRef = nodeUtils.getNodeRef(nodeRef);
            QName propertyQName = QName.resolveToQName(prefixResolver, propertyName);
            barcodePropertyValue = nodeUtils.getProperty(targetNodeRef, propertyQName);
        } catch (Exception e) {
            throw new BarcodeInputException();
        }

        BarcodeFormat barcodeFormat = getBarcodeFormatByType(barcodetype);

        return base64ImageConverter.fromBarcode(barcodePropertyValue, width, height, barcodeFormat);
    }


    public String getProperty(String nodeRef) {
        RecordRef recordRef = RecordRef.create("", nodeRef);
        return barcodeAttributeRegistry.getAttribute(recordRef);
    }

    private BarcodeFormat getBarcodeFormatByType(String barcodeType) {
        switch (barcodeType) {
            case "code-128":
                return BarcodeFormat.CODE_128;
            case "code-39":
                return BarcodeFormat.CODE_39;
            case "code-93":
                return BarcodeFormat.CODE_93;
            default:
                throw new RuntimeException("Unsupported barcode type: " + barcodeType);
        }
    }
}
