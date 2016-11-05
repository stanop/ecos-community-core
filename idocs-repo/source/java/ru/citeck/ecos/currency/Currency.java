package ru.citeck.ecos.currency;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Currency entity
 *
 * @author alexander.nemerov
 *         date 03.11.2016.
 */
public class Currency {

    private String code;

    private BigDecimal rate;

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

}
