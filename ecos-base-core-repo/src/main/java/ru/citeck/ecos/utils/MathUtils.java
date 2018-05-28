package ru.citeck.ecos.utils;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * @author Roman Makarskiy
 */
public class MathUtils {

    /**
     * It returns a scaled {@code BigDecimal}, with rounding mode {@code BigDecimal.ROUND_HALF_UP}, from specified
     * property or {@code BigDecimal.ZERO} if property {@code null}
     *
     * @param nodeRef     - node reference
     * @param property    - property from which the {@code BigDecimal} will be created
     * @param scale       - scale of the {@code BigDecimal} value to be returned
     * @param nodeService - node service
     * @return {@code BigDecimal} value
     */
    public static BigDecimal createScaledBigDecimalFromPropOrZero(NodeRef nodeRef, QName property, int scale,
                                                                  NodeService nodeService) {
        return createScaledBigDecimalFromPropOrZero(nodeRef, property, scale, BigDecimal.ROUND_HALF_UP, nodeService);
    }

    /**
     * It returns a scaled {@code BigDecimal}, with specified rounding mode, from specified
     * property or {@code BigDecimal.ZERO} if property {@code null}
     *
     * @param nodeRef      - node reference
     * @param property     - property from which the {@code BigDecimal} will be created
     * @param scale        - scale of the {@code BigDecimal} value to be returned
     * @param roundingMode - the rounding mode to apply
     * @param nodeService  - node service
     * @return {@code BigDecimal} value
     */
    public static BigDecimal createScaledBigDecimalFromPropOrZero(NodeRef nodeRef, QName property, int scale,
                                                                  int roundingMode, NodeService nodeService) {
        BigDecimal value = createBigDecimalFromPropOrZero(nodeRef, property, nodeService);
        value = value.setScale(scale, roundingMode);
        return value;
    }

    /**
     * It returns a {@code BigDecimal} from specified property or {@code BigDecimal.ZERO} if property {@code null}
     *
     * @param nodeRef     - node reference
     * @param property    - property from which the {@code BigDecimal} will be created
     * @param nodeService - node service
     * @return {@code BigDecimal} value
     */
    public static BigDecimal createBigDecimalFromPropOrZero(NodeRef nodeRef, QName property, NodeService nodeService) {
        BigDecimal value = BigDecimal.ZERO;

        if (!nodeService.exists(nodeRef) || nodeService.getProperty(nodeRef, property) == null) {
            return value;
        }

        value = new BigDecimal((Double) nodeService.getProperty(nodeRef, property), MathContext.DECIMAL64);
        return value;
    }

    /**
     * It returns a parsed {@code Double} according a current JVM Locale
     *
     * @param data - data to parse
     * @return {@code Double} value
     * @throws ParseException if impossible to parse data
     */
    public static Double parseDouble(String data) throws ParseException {
        return parseDouble(data, Locale.getDefault());
    }

    /**
     * It returns a parsed {@code Double} with specified locale number format
     *
     * @param data   - data to parse
     * @param locale - the desired locale
     * @return {@code Double} value
     * @throws ParseException if impossible to parse data
     */
    public static Double parseDouble(String data, Locale locale) throws ParseException {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        ParsePosition parsePosition = new ParsePosition(0);
        Number number = numberFormat.parse(data, parsePosition);

        if (parsePosition.getIndex() != data.length()) {
            throw new ParseException("Invalid double input: " + data + " with locale: " + Locale.getDefault(),
                    parsePosition.getIndex());
        }

        return number.doubleValue();
    }
}
