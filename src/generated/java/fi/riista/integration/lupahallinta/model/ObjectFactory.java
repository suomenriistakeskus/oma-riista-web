
package fi.riista.integration.lupahallinta.model;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.riista.integration.lupahallinta.model package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.riista.integration.lupahallinta.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LH_Person }
     * 
     */
    public LH_Person createLH_Person() {
        return new LH_Person();
    }

    /**
     * Create an instance of {@link LH_Export }
     * 
     */
    public LH_Export createLH_Export() {
        return new LH_Export();
    }

    /**
     * Create an instance of {@link LH_GeoLocation }
     * 
     */
    public LH_GeoLocation createLH_GeoLocation() {
        return new LH_GeoLocation();
    }

    /**
     * Create an instance of {@link LH_Organisation }
     * 
     */
    public LH_Organisation createLH_Organisation() {
        return new LH_Organisation();
    }

    /**
     * Create an instance of {@link LH_Position }
     * 
     */
    public LH_Position createLH_Position() {
        return new LH_Position();
    }

    /**
     * Create an instance of {@link LH_Person.LH_Positions }
     * 
     */
    public LH_Person.LH_Positions createLH_PersonLH_Positions() {
        return new LH_Person.LH_Positions();
    }

    /**
     * Create an instance of {@link LH_Export.LH_Organisations }
     * 
     */
    public LH_Export.LH_Organisations createLH_ExportLH_Organisations() {
        return new LH_Export.LH_Organisations();
    }

    /**
     * Create an instance of {@link LH_Export.LH_Persons }
     * 
     */
    public LH_Export.LH_Persons createLH_ExportLH_Persons() {
        return new LH_Export.LH_Persons();
    }

}
