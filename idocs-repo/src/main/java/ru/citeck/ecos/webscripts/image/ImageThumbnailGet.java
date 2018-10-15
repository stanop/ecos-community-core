package ru.citeck.ecos.webscripts.image;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.lang.StringUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.*;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class ImageThumbnailGet extends AbstractWebScript {

    private static final String PARAM_IMAGE_REF = "imageRef";
    private static final String PARAM_WIDTH = "width";
    private static final String PARAM_HEIGHT = "height";
    private static final String PARAM_CACHED = "cached";
    private static final String PARAM_ATTACH = "attach";
    private static final String PARAM_STRETCH = "stretch";

    private static final int TEMP_FILE_UPDATE_RATE = 10 * 60 * 1000;// 10 min

    private NodeService nodeService;
    private ContentService contentService;
    private ContentStreamer contentStreamer;
    private MimetypeService mimetypeService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String imageRefStr = req.getParameter(PARAM_IMAGE_REF);
        if (StringUtils.isBlank(imageRefStr) || !NodeRef.isNodeRef(imageRefStr)) {
            String msg = PARAM_IMAGE_REF + " has incorrect value: " + imageRefStr;
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }

        NodeRef imageRef = new NodeRef(imageRefStr);
        String widthStr = req.getParameter(PARAM_WIDTH);
        String heightStr = req.getParameter(PARAM_HEIGHT);

        int width = StringUtils.isNotBlank(widthStr) ? Integer.parseInt(widthStr) : -1;
        int height = StringUtils.isNotBlank(heightStr) ? Integer.parseInt(heightStr) : -1;
        boolean stretch = "true".equals(req.getParameter(PARAM_STRETCH));

        ContentReader reader = contentService.getReader(imageRef, ContentModel.PROP_CONTENT);
        File tempFile = getTempFile(imageRef, reader, width, height, stretch);

        long tempLastModified = tempFile.lastModified();
        if (!tempFile.exists() || tempLastModified < reader.getLastModified()) {
            updateImage(reader, tempFile, width, height, stretch);
        } else if (System.currentTimeMillis() - tempLastModified > TEMP_FILE_UPDATE_RATE) {
            tempFile.setLastModified(System.currentTimeMillis());
        }

        Map<QName, Serializable> imageProps = nodeService.getProperties(imageRef);
        String imageName = (String) imageProps.get(ContentModel.PROP_NAME);
        boolean attach = "true".equals(req.getParameter(PARAM_ATTACH));

        contentStreamer.streamContent(req, res,
                                      tempFile, reader.getLastModified(),
                                      attach, imageName, getModel(req));
        res.setStatus(Status.STATUS_OK);
    }

    private File getTempFile(NodeRef imageRef,
                             ContentReader reader,
                             int width, int height, boolean stretch) {

        String extension = mimetypeService.getExtension(reader.getMimetype());
        File tmpDir = TempFileProvider.getTempDir().getAbsoluteFile();
        return new File(tmpDir, String.format("%s_%s_%s_%s.%s", imageRef.getId(), width, height, stretch, extension));
    }

    private void updateImage(ContentReader reader,
                             File target,
                             int width,
                             int height,
                             boolean stretch) throws IOException {

        String originalMimetype = reader.getMimetype();
        String originalExtension = mimetypeService.getExtension(originalMimetype);

        BufferedImage originalImage;
        try (InputStream content = reader.getContentInputStream()) {
            originalImage = ImageIO.read(content);
        }

        BufferedImage thumbnailImg;
        if (width > 0 && height == -1) {
            thumbnailImg = Scalr.resize(originalImage, Scalr.Mode.FIT_TO_WIDTH, width);
        } else if (height > 0 && width == -1) {
            thumbnailImg = Scalr.resize(originalImage, Scalr.Mode.FIT_TO_HEIGHT, height);
        } else if (stretch) {
            thumbnailImg = Scalr.resize(originalImage, Scalr.Mode.FIT_EXACT, width, height);
        } else {
            thumbnailImg = Scalr.resize(originalImage, Scalr.Mode.AUTOMATIC, width, height);
        }

        ImageIO.write(thumbnailImg, originalExtension, target);
    }

    private Map<String, Object> getModel(WebScriptRequest req) {
        String cached = req.getParameter(PARAM_CACHED);
        Map<String, Object> model = new HashMap<>();
        model.put("allowBrowserToCache", "false".equals(cached) ? "false" : "true");
        return model;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
        contentService = serviceRegistry.getContentService();
        mimetypeService = serviceRegistry.getMimetypeService();
    }

    @Autowired
    @Qualifier("webscript.content.streamer")
    public void setContentStreamer(ContentStreamer contentStreamer) {
        this.contentStreamer = contentStreamer;
    }
}
