package ru.citeck.ecos.cmmn.service.util;

import ru.citeck.ecos.cmmn.condition.ConditionsList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

/**
 * @author Maxim Strizhov
 */
class CaseConditionImporter {
    ConditionsList parseXML(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ConditionsList.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        StringReader stringReader = new StringReader(xml);
        return (ConditionsList) jaxbUnmarshaller.unmarshal(stringReader);
    }
}
