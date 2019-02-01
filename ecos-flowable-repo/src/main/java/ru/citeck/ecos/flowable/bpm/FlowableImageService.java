package ru.citeck.ecos.flowable.bpm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.ui.modeler.service.ModelImageService;
import org.flowable.ui.modeler.util.ImageGenerator;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class FlowableImageService extends ModelImageService {

    private static final Log logger = LogFactory.getLog(FlowableImageService.class);

    public byte[] generateImage(BpmnModel bpmnModel) {
        return generateImage(bpmnModel, Integer.MAX_VALUE);
    }

    public byte[] generateImage(BpmnModel bpmnModel, int maxWidth) {
        try {

            double scaleFactor = 1.0;
            if (maxWidth < Integer.MAX_VALUE) {
                GraphicInfo diagramInfo = calculateDiagramSize(bpmnModel);
                if (diagramInfo.getWidth() > maxWidth) {
                    scaleFactor = diagramInfo.getWidth() / maxWidth;
                    scaleDiagram(bpmnModel, scaleFactor);
                }
            }

            BufferedImage modelImage = ImageGenerator.createImage(bpmnModel, scaleFactor);
            if (modelImage != null) {
                return ImageGenerator.createByteArrayForImage(modelImage, "png");
            }
        } catch (Exception e) {
            logger.error("Error creating thumbnail image " + bpmnModel, e);
        }
        return null;
    }
}
