package ru.citeck.ecos.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.processor.DataBundle;
import ru.citeck.ecos.processor.ProcessorHelper;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class PDFBarcodeService implements ApplicationContextAware {

    private final static Logger logger = Logger.getLogger(PDFBarcodeService.class);

    private ApplicationContext applicationContext;
    private ContentService contentService;
    private SysAdminParams sysAdminParams;
    private ProcessorHelper helper;

    private static final String BARCODE_NAME = "Barcode.code128";
    private static final Integer UNDER_MARGIN = 10;
    private static final Integer STAMP_MARGIN = 20;

    public DataBundle putBarcodeOnDocument(DataBundle transformedDocument, String barcode) {
        return putBarcodeOnDocument(transformedDocument, barcode, null);
    }

    public DataBundle putBarcodeOnDocument(DataBundle transformedDocument, String barCodeStr, String documentRef) {

        Barcode barCode = null;
        BarcodeQRCode barCodeQRCode = null;

        if (barCodeStr != null) {
            barCode = generateBarCode(barCodeStr);
        }

        if (documentRef != null) {
            barCodeQRCode = generateQRcode(documentRef);
        }

        if (barCode != null || barCodeQRCode != null) {

            ContentWriter writer = contentService.getTempWriter();

            try {
                PdfReader reader = new PdfReader(transformedDocument.getInputStream());
                PdfStamper stamper = new PdfStamper(reader, writer.getContentOutputStream());

                Image imageBarcode = null;
                Image imageQRcode = null;

                int countPages = reader.getNumberOfPages();

                if (countPages > 0 && barCode != null) {
                    imageBarcode = barCode.createImageWithBarcode(stamper.getUnderContent(1), null, null);
                }
                if (barCodeQRCode != null) {
                    imageQRcode = barCodeQRCode.getImage();
                }

                if (imageBarcode != null) {
                    for (int i = 1; i <= countPages; i++) {
                        AffineTransform transformPrefs = AffineTransform.getTranslateInstance(
                            stamper.getReader().getPageSize(i).getWidth() / 2 - imageBarcode.getWidth() / 2,
                            UNDER_MARGIN);
                        transformPrefs.concatenate(AffineTransform.getScaleInstance(
                            imageBarcode.getScaledWidth(), imageBarcode.getScaledHeight()));
                        stamper.getUnderContent(i).addImage(imageBarcode, transformPrefs);
                    }
                }

                if (imageQRcode != null) {
                    float marginFromCenter;
                    if (imageBarcode != null) {
                        marginFromCenter = imageBarcode.getWidth() / 2 + STAMP_MARGIN;
                    } else {
                        marginFromCenter = -imageQRcode.getWidth() / 2;
                    }

                    AffineTransform transformPrefsQRcode = AffineTransform.getTranslateInstance(
                        stamper.getReader().getPageSize(1).getWidth() / 2 + marginFromCenter,
                        UNDER_MARGIN);
                    transformPrefsQRcode.concatenate(AffineTransform.getScaleInstance(imageQRcode.getScaledWidth(), imageQRcode.getScaledHeight()));
                    stamper.getUnderContent(1).addImage(imageQRcode, transformPrefsQRcode);
                }

                stamper.close();
                reader.close();
            } catch (IOException | DocumentException e) {
                logger.error("Error while adding barcode or QR-code on document", e);
            }

            Map<String, Object> model = new HashMap<>();
            return helper.getDataBundle(writer.getReader(), model);
        }

        return transformedDocument;
    }


    private Barcode generateBarCode(String barcodeInput) {
        Barcode barcode = applicationContext.getBean(BARCODE_NAME, Barcode.class);
        barcode.setTextAlignment(Element.ALIGN_CENTER);
        barcode.setCode(barcodeInput);
        barcode.setSize(4);
        return barcode;
    }

    private BarcodeQRCode generateQRcode(String documentRef) {
        if (documentRef == null) {
            return null;
        }
        String link = generateLinkForDocument(documentRef);
        if (link == null) {
            return null;
        }

        logger.debug("Generate QR for address = " + link);
        return new BarcodeQRCode(link, 1, 1, null);
    }

    private String generateLinkForDocument(String ref) {
        if (ref == null) {
            return null;
        }
        return getUrl(sysAdminParams) + "/v2/dashboard?recordRef=" + ref;
    }

    private String getUrl(SysAdminParams sysAdminParams) {
        return buildUrl(
            sysAdminParams.getShareProtocol(),
            sysAdminParams.getShareHost(),
            sysAdminParams.getSharePort()
        );
    }

    private String buildUrl(String protocol, String host, int port) {
        StringBuilder url = new StringBuilder();
        url.append(protocol);
        url.append("://");
        url.append(host);
        if ("http".equals(protocol) && port == 80) {
            // Not needed
        } else if ("https".equals(protocol) && port == 443) {
            // Not needed
        } else {
            url.append(':');
            url.append(port);
        }
        return url.toString();
    }

    @Override
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired
    @Qualifier("DataBundleProcessorHelper")
    public void setHelper(ProcessorHelper helper) {
        this.helper = helper;
    }

    @Autowired
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Autowired
    public void setSysAdminParams(SysAdminParams sysAdminParams) {
        this.sysAdminParams = sysAdminParams;
    }
}
