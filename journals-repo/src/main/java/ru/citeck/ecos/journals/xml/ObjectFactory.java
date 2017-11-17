
package ru.citeck.ecos.journals.xml;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.citeck.ecos.journals.xml package.
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.citeck.ecos.journals.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Journals }
     * 
     */
    public Journals createJournals() {
        return new Journals();
    }

    /**
     * Create an instance of {@link Journal }
     * 
     */
    public Journal createJournal() {
        return new Journal();
    }

    /**
     * Create an instance of {@link Journals.Imports }
     * 
     */
    public Journals.Imports createJournalsImports() {
        return new Journals.Imports();
    }

    /**
     * Create an instance of {@link AttributeFilterRegion }
     * 
     */
    public AttributeFilterRegion createAttributeFilterRegion() {
        return new AttributeFilterRegion();
    }

    /**
     * Create an instance of {@link BatchEdit }
     * 
     */
    public BatchEdit createBatchEdit() {
        return new BatchEdit();
    }

    /**
     * Create an instance of {@link GroupAction }
     * 
     */
    public GroupAction createGroupAction() {
        return new GroupAction();
    }

    /**
     * Create an instance of {@link Header }
     * 
     */
    public Header createHeader() {
        return new Header();
    }

    /**
     * Create an instance of {@link AttributeFilter }
     * 
     */
    public AttributeFilter createAttributeFilter() {
        return new AttributeFilter();
    }

    /**
     * Create an instance of {@link Option }
     * 
     */
    public Option createOption() {
        return new Option();
    }

    /**
     * Create an instance of {@link Evaluator }
     * 
     */
    public Evaluator createEvaluator() {
        return new Evaluator();
    }

    /**
     * Create an instance of {@link Journal.GroupActions }
     * 
     */
    public Journal.GroupActions createJournalGroupActions() {
        return new Journal.GroupActions();
    }

    /**
     * Create an instance of {@link Journal.Headers }
     * 
     */
    public Journal.Headers createJournalHeaders() {
        return new Journal.Headers();
    }

    /**
     * Create an instance of {@link Journals.Imports.Import }
     * 
     */
    public Journals.Imports.Import createJournalsImportsImport() {
        return new Journals.Imports.Import();
    }

}
