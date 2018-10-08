package ru.citeck.ecos.pojo;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;

public class Document {

    private NodeRef type;
    private NodeRef kind;
    private String mimeType;
    private ContentReader contentReader;

    public Document(NodeRef type, NodeRef kind, String mimeType, ContentReader contentReader) {
        this.type = type;
        this.kind = kind;
        this.mimeType = mimeType;
        this.contentReader = contentReader;
    }

    public NodeRef getType() {
        return type;
    }

    public void setType(NodeRef type) {
        this.type = type;
    }

    public NodeRef getKind() {
        return kind;
    }

    public void setKind(NodeRef kind) {
        this.kind = kind;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public ContentReader getContentReader() {
        return contentReader;
    }

    public void setContentReader(ContentReader contentReader) {
        this.contentReader = contentReader;
    }

}
