package ru.citeck.ecos.processor;

import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerWorker;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;

import java.util.Map;

public class RuntimeExecutableTransformContent extends AbstractDataBundleLine {

    private ContentService contentService;
    private String outputMimetype;
    private String outputEncoding;
    private TransformationOptions options;
    private RuntimeExecutableContentTransformerWorker worker;

    @Override
    public void init() {
        this.contentService = serviceRegistry.getContentService();
        if(options == null) options = new TransformationOptions();
    }

    @Override
    public DataBundle process(DataBundle input) {
        Map<String,Object> model = input.needModel();

        ContentReader reader = helper.getContentReader(input);

        ContentWriter writer = contentService.getTempWriter();
        writer.setEncoding(evaluateExpression(outputEncoding, model).toString());
        writer.setMimetype(evaluateExpression(outputMimetype, model).toString());

        try {
            worker.transform(reader, writer, options);
        }
        catch (Exception e) {
            throw new IllegalStateException("Can not transform " + reader.getMimetype() + " to " + outputMimetype, e);
        }

        ContentReader resultReader = writer.getReader();
        return helper.getDataBundle(resultReader, model);
    }

    public void setOutputMimetype(String outputMimetype) {
        this.outputMimetype = outputMimetype;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    public void setOptions(TransformationOptions options) {
        this.options = options;
    }

    public void setWorker(RuntimeExecutableContentTransformerWorker worker) {
        this.worker = worker;
    }
}
