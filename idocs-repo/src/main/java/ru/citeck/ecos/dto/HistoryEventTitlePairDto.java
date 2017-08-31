package ru.citeck.ecos.dto;

import org.alfresco.service.namespace.QName;

/**
 * Object Qname and title property Qname pair data transfer object
 */
public class HistoryEventTitlePairDto {

    /**
     * Object Qname
     */
    private QName objectQName;

    /**
     * Title property Qname
     */
    private QName titlePropertyQName;

    /**
     * Constructor
     * @param objectQName Full object qname string
     * @param titlePropertyQName Full title qname string
     */
    public HistoryEventTitlePairDto(String objectQName, String titlePropertyQName) {
        this.objectQName = QName.createQName(objectQName);
        this.titlePropertyQName = QName.createQName(titlePropertyQName);
    }


    /**
     * Getters and setters
     */

    public QName getObjectQName() {
        return objectQName;
    }

    public QName getTitlePropertyQName() {
        return titlePropertyQName;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final HistoryEventTitlePairDto other = (HistoryEventTitlePairDto) object;
        return (other.getObjectQName().equals(objectQName)) && (other.getTitlePropertyQName().equals(titlePropertyQName));
    }
}
