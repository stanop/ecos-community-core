package ru.citeck.ecos.dto;

import org.alfresco.service.namespace.QName;

import java.util.List;

/**
 * Object Qname and titles property Qname pair data transfer object
 */
public class HistoryEventTitlePairListDto {

    /**
     * Object Qname
     */
    private QName objectQName;

    /**
     * Titles qnames
     */
    private List<QName> titles;

    /**
     * Constructor
     * @param objectQName Object Qname
     * @param titles Titles qnames
     */
    public HistoryEventTitlePairListDto(QName objectQName, List<QName> titles) {
        this.objectQName = objectQName;
        this.titles = titles;
    }

    /** Getters and setters */

    public QName getObjectQName() {
        return objectQName;
    }

    public void setObjectQName(QName objectQName) {
        this.objectQName = objectQName;
    }

    public List<QName> getTitles() {
        return titles;
    }

    public void setTitles(List<QName> titles) {
        this.titles = titles;
    }
}
