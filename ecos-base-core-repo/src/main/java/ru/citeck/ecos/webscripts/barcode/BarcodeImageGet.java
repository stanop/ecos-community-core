package ru.citeck.ecos.webscripts.barcode;

import com.google.zxing.BarcodeFormat;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.http.HttpStatus;
import ru.citeck.ecos.barcode.BarcodeService;
import ru.citeck.ecos.utils.NodeUtils;
import ru.citeck.ecos.webscripts.handler.WebScriptRequestHandler;

import java.util.HashMap;
import java.util.Map;

public class BarcodeImageGet extends DeclarativeWebScript {

    public static final String BARCODE_WIDTH_PARAM = "width";
    public static final String BARCODE_HEIGHT_PARAM = "height";
    public static final String BARCODE_TYPE_PARAM = "barcodeType";
    public static final String PROPERTY_PARAM = "property";
    public static final String NODE_REF_PARAM = "nodeRef";
    private static final String DATA_JSON_PROPERTY = "data";

    private WebScriptRequestHandler requestHandler;
    private BarcodeService barcodeService;
    private NodeUtils nodeUtils;
    private NamespacePrefixResolver prefixResolver;

    public BarcodeImageGet(WebScriptRequestHandler requestHandler,
                           BarcodeService barcodeService,
                           NodeUtils nodeUtils,
                           NamespacePrefixResolver prefixResolver) {
        this.requestHandler = requestHandler;
        this.barcodeService = barcodeService;
        this.nodeUtils = nodeUtils;
        this.prefixResolver = prefixResolver;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> input = requestHandler.handleRequest(req);

        NodeRef nodeRef = nodeUtils.getNodeRef((String) input.get(NODE_REF_PARAM));
        QName propertyQName = QName.resolveToQName(prefixResolver, (String) input.get(PROPERTY_PARAM));
        BarcodeFormat barCodeFormat = barcodeService.getBarcodeFormatByType((String) input.get(BARCODE_TYPE_PARAM));

        String base64 = barcodeService.getBarcodeAsBase64FromProp(
                nodeRef,
                propertyQName,
                (int) input.get(BARCODE_WIDTH_PARAM),
                (int) input.get(BARCODE_HEIGHT_PARAM),
                barCodeFormat);

        status.setCode(HttpStatus.OK.value());

        Map<String, Object> model = new HashMap<>();
        model.put(DATA_JSON_PROPERTY, base64);
        return model;
    }

}
