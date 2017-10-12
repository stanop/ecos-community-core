package ru.citeck.ecos.utils;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipContainer {

    private static final Charset ENCODING_CP866 = Charset.forName("CP866");
    private static final String ENCODING_UTF8_NAME = "UTF-8";

    private ContentService contentService;
    private MimetypeService mimetypeService;
    private NodeService nodeService;

    private File zipFile;

    public ZipContainer(File zipFile, ServiceRegistry serviceRegistry) {
        this.zipFile = zipFile;
        contentService = serviceRegistry.getContentService();
        mimetypeService = serviceRegistry.getMimetypeService();
        nodeService = serviceRegistry.getNodeService();
    }

    public List<NodeRef> extract(NodeRef target) {
        return extract(target, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT);
    }

    public List<NodeRef> extract(NodeRef target, QName childAssoc) {
        return extract(target, childAssoc, ContentModel.TYPE_CONTENT);
    }

    public List<NodeRef> extract(NodeRef parent, QName assocType, QName type) {

        List<NodeRef> result = new ArrayList<>();

        forEachFile((entry, stream) -> {

            QName assocName = QName.createQNameWithValidLocalName(assocType.getNamespaceURI(), entry.getName());
            Map<QName, Serializable> properties = Collections.singletonMap(ContentModel.PROP_NAME, entry.getName());

            NodeRef docRef = nodeService.createNode(parent, assocType, assocName, type, properties).getChildRef();
            ContentWriter writer = contentService.getWriter(docRef, ContentModel.PROP_CONTENT, true);

            writer.setMimetype(mimetypeService.guessMimetype(entry.getName()));
            writer.setEncoding(ENCODING_UTF8_NAME);
            writer.putContent(stream);

            result.add(docRef);
        });

        return result;
    }

    public void forEachFile(ZipEntryWork work) {

        try (ZipFile zip = new ZipFile(zipFile, ENCODING_CP866)) {
            Enumeration<? extends ZipEntry> entries;
            for (entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                try (InputStream inStream = zip.getInputStream(entry)) {
                    work.doWork(entry, inStream);
                }
            }
        } catch (IOException exception) {
            throw new AlfrescoRuntimeException("IOException", exception);
        }
    }

    @FunctionalInterface
    public interface ZipEntryWork {
        void doWork(ZipEntry entry, InputStream inStream);
    }
}
