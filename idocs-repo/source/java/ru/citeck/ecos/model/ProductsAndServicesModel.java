package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

/**
 * Created by maxim on 21.03.2016.
 */
public final class ProductsAndServicesModel {
    public static final String PAS_MODEL_PREFIX = "pas";

    public static final String PAS_NAMESPACE = "http://www.citeck.ru/model/products-and-services/1.0";

    public static final QName PROP_ORDER = QName.createQName(PAS_NAMESPACE, "order");
    public static final QName PROP_PRICE_PER_UNIT = QName.createQName(PAS_NAMESPACE, "pricePerUnit");
    public static final QName PROP_QUANTITY = QName.createQName(PAS_NAMESPACE, "quantity");
    public static final QName PROP_TOTAL = QName.createQName(PAS_NAMESPACE, "total");

    public static final QName ASSOC_CONTAINS_PRODUCTS_AND_SERVICES = QName.createQName(PAS_NAMESPACE, "containsProductsAndServices");
}
