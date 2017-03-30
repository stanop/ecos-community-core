package ru.citeck.ecos.currency;

import org.alfresco.service.cmr.repository.NodeRef;

import java.math.BigDecimal;

/**
 * Currency entity
 *
 * @author alexander.nemerov
 *         date 03.11.2016.
 */
public class Currency {

    private String code;

    private BigDecimal rate;

    private Integer numberCode;

    private NodeRef nodeRef;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public Integer getNumberCode() {
        return numberCode;
    }

    public void setNumberCode(Integer numberCode) {
        this.numberCode = numberCode;
    }


    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency)) return false;

        Currency currency = (Currency) o;

        if (code != null ? !code.equals(currency.code) : currency.code != null) return false;
        return numberCode != null ? numberCode.equals(currency.numberCode) : currency.numberCode == null;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (numberCode != null ? numberCode.hashCode() : 0);
        return result;
    }
}
