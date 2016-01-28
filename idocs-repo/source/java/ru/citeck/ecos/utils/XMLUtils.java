/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.utils;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

public class XMLUtils {
    
    // static only class
    private XMLUtils() {}

    /**
     * Create unmarshaller for the specified root element class (with XML binding annotations) 
     *   and optional schema locations for the validation.
     * 
     * @param rootClass
     * @param schemaLocations
     * @return
     * @throws JAXBException if parsing fails
     * @throws SAXException if schema parsing fails
     * @throws IOException if schema location was not resolved to a valid file
     */
    public static Unmarshaller createUnmarshaller(Class<?> rootClass, String ...schemaLocations) 
            throws JAXBException, SAXException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(rootClass);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        
        if (schemaLocations.length > 0) {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance("http://www.w3.org/2001/XMLSchema");
            Source[] schemaSources = new Source[schemaLocations.length];
            for(int i = 0; i < schemaLocations.length; i++) {
                ClassPathResource schemaResource = new ClassPathResource(schemaLocations[i]);
                schemaSources[i] = new StreamSource(schemaResource.getInputStream());
            }
            Schema schema = schemaFactory.newSchema(schemaSources);
            jaxbUnmarshaller.setSchema(schema);
        }
        return jaxbUnmarshaller;
    }
    
}