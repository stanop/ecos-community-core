package ru.citeck.ecos.content.config;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.content.config.parser.ConfigParser;

import java.io.InputStream;

public class ConfigData<T> {

    private NodeRef nodeRef;
    private long lastModified;
    private T data;

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public T getData() {
        return data;
    }

    public long getLastModified() {
        return lastModified;
    }

    ConfigData<T> updateData(ConfigParser<T> parser, ContentService contentService) {

        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        long contentLastModified = reader.getLastModified();

        if (lastModified < contentLastModified) {

            synchronized (this) {

                if (lastModified < contentLastModified) {

                    try (InputStream in = reader.getContentInputStream()) {

                        data = parser.parse(in);
                        lastModified = contentLastModified;

                    } catch (Exception e) {
                        throw new AlfrescoRuntimeException("Can't parse content from node " + nodeRef, e);
                    }
                }
            }
        } else if (contentLastModified == 0) {
            //content doesn't exists
            data = null;
            lastModified = 0;
        }

        return this;
    }

    ConfigData(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
        this.lastModified = 0;
        this.data = null;
    }
}