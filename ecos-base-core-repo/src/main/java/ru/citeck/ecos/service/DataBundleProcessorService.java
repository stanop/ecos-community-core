package ru.citeck.ecos.service;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import ru.citeck.ecos.processor.CompositeDataBundleProcessor;
import ru.citeck.ecos.processor.DataBundle;
import ru.citeck.ecos.processor.DataBundleProcessor;
import ru.citeck.ecos.processor.ProcessorConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.citeck.ecos.webscripts.processor.DataBundleProcessorWebscript.KEY_ARGS;

public class DataBundleProcessorService {
    private DataBundleProcessor processor;
    private List<DataBundleProcessor> processors;
    private MimetypeService mimetypeService;

    public void init() {
        if (processor instanceof CompositeDataBundleProcessor) {
            ((CompositeDataBundleProcessor) processor).setProcessors(processors);
        }
    }

    public ProcessionResult getProcessedDataStream(NodeRef document, String templateType, Format format) throws IOException {
        DataBundle inputBundle = getInputBundle(document, templateType, format);

        List<DataBundle> inputs = Arrays.asList(new DataBundle[]{inputBundle});

        // get actual input stream and model
        InputStream inputStream = null;
        ProcessionResult processionResult = null;
        try {
            // do the processing
            List<DataBundle> outputs = processor.process(inputs);

            Map<String, Object> outputModel = null;
            for (DataBundle output : outputs) {
                if (output == null) continue;
                InputStream str = output.getInputStream();
                if (str != null) {
                    inputStream = str;
                    outputModel = output.getModel();
                    break;
                }
            }

            if (inputStream != null) {

                // first set the headers
                String encoding = (String) outputModel.get(ProcessorConstants.KEY_ENCODING);
                String mimetype = (String) outputModel.get(ProcessorConstants.KEY_MIMETYPE);
                String filename = (String) outputModel.get(ProcessorConstants.KEY_FILENAME);

                if (mimetype != null) {
                    String extension = mimetypeService.getExtension(mimetype);
                    if (filename != null && extension != null && !filename.endsWith(extension)
                            && !extension.equals(MimetypeMap.EXTENSION_BINARY)) {
                        filename = filename + "." + extension;
                    }
                }
                processionResult = new ProcessionResult(inputStream, encoding,mimetype, filename);

            }

        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return processionResult;
    }

    private DataBundle getInputBundle(NodeRef document, String templateType, Format format) {
        Map<String, Object> model = new HashMap<String, Object>(2);
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("nodeRef", document);
        argsMap.put("templateType", templateType);
        argsMap.put("format", format.getValue());
        model.put(KEY_ARGS, null);
        model.put(ProcessorConstants.KEY_ENCODING, "UTF-8");
        model.put(ProcessorConstants.KEY_MIMETYPE, null);
        return new DataBundle(model);
    }

    public void setProcessor(DataBundleProcessor processor) {
        this.processor = processor;
    }

    public void setProcessors(List<DataBundleProcessor> processors) {
        this.processors = processors;
    }

    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    public  class ProcessionResult {
       private final InputStream inputStream;
       private final String encoding;
       private final String mimeType;
       private final String fileName;

       ProcessionResult(InputStream inputStream, String encoding, String mimeType, String fileName) {
           this.inputStream = inputStream;
           this.encoding = encoding;
           this.mimeType = mimeType;
           this.fileName = fileName;
       }

       public InputStream getInputStream() {
           return inputStream;
       }

       public String getEncoding() {
           return encoding;
       }

       public String getMimeType() {
           return mimeType;
       }

       public String getFileName() {
           return fileName;
       }

       @Override
       public boolean equals(Object o) {
           if (this == o) return true;
           if (!(o instanceof ProcessionResult)) return false;

           ProcessionResult that = (ProcessionResult) o;

           if (!inputStream.equals(that.inputStream)) return false;
           if (encoding != null ? !encoding.equals(that.encoding) : that.encoding != null) return false;
           if (mimeType != null ? !mimeType.equals(that.mimeType) : that.mimeType != null) return false;
           return fileName.equals(that.fileName);
       }

       @Override
       public int hashCode() {
           int result = inputStream.hashCode();
           result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
           result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
           result = 31 * result + fileName.hashCode();
           return result;
       }
   }

    enum Format {
        DOCX("docx"),
        PDF("pdf"),
        DOC("doc"),
        HTML("html");

        private String value;

        Format(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
