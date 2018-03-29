package ru.citeck.ecos.content;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Class that represents config data
 * @param <T> type of parsed content data
 *
 * @author Pavel Simonov
 */
public class ContentData<T> {

    private NodeRef nodeRef;
    private T data;

    private long lastModified;
    private RepoContentDAO<T> registry;

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    /**
     * Get parsed content data
     */
    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }

    /**
     * Get content last modified time in millis
     * @see System#currentTimeMillis()
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Change data. Content will be updated after change will be performed
     */
    public void changeData(Consumer<T> consumer) {

        if (data != null) {

            synchronized (this) {

                consumer.accept(data);

                QName field = registry.getContentFieldName();
                ContentWriter writer = registry.getContentService().getWriter(nodeRef, field, true);

                registry.getContentDAO().write(data, writer);

                updateData();
            }
        }
    }

    boolean updateData() {

        if (nodeRef == null || !registry.getNodeService().exists(nodeRef)) {
            return false;
        }

        ContentReader reader = registry.getContentService().getReader(nodeRef, registry.getContentFieldName());

        if (reader == null) {
            return false;
        }

        long contentLastModified = reader.getLastModified();

        if (lastModified < contentLastModified) {

            synchronized (this) {

                if (lastModified < contentLastModified) {

                    try (InputStream in = reader.getContentInputStream()) {

                        data = registry.getContentDAO().read(in);
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

        return true;
    }

    ContentData(NodeRef nodeRef, RepoContentDAO<T> owner) {
        this.nodeRef = nodeRef;
        this.lastModified = 0;
        this.data = null;
        this.registry = owner;
    }
}