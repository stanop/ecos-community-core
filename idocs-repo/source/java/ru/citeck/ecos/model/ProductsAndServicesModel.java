package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

/**
 * Created by maxim on 21.03.2016.
 */
public final class ProductsAndServicesModel {
    public static final String PAS_MODEL_PREFIX = "pas";

    public static final String PAS_NAMESPACE = "http://www.citeck.ru/model/products-and-services/1.0";

    public static final QName ASSOC_PROD_AND_SERV = QName.createQName("products-and-services");

    public static final QName PROP_ORDER = QName.createQName(PAS_NAMESPACE, "order");
    public static final QName PROP_PRICE_PER_UNIT = QName.createQName(PAS_NAMESPACE, "pricePerUnit");
    public static final QName PROP_TYPE = QName.createQName(PAS_NAMESPACE, "type");
    public static final QName PROP_QUANTITY = QName.createQName(PAS_NAMESPACE, "quantity");
    public static final QName PROP_TOTAL = QName.createQName(PAS_NAMESPACE, "total");

    public static final QName ASSOC_CONTAINS_PRODUCTS_AND_SERVICES = QName.createQName(PAS_NAMESPACE, "containsProductsAndServices");
    public static final QName ASSOC_CONTAINS_ORIG_PRODUCTS_AND_SERVICES = QName.createQName(PAS_NAMESPACE, "containsOriginalProductsAndServices");
    public static final QName TYPE_ENTITY_COPIED = QName.createQName(PAS_NAMESPACE, "pasEntityCopied");
    public static final QName ASSOC_ENTITY_UNIT = QName.createQName(PAS_NAMESPACE, "entityUnit");

    public static final QName ASPECT_HASUNIT = QName.createQName(PAS_NAMESPACE, "hasUnit");
}
