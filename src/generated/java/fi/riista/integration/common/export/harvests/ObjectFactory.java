
package fi.riista.integration.common.export.harvests;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.riista.integration.common.export.harvests package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.riista.integration.common.export.harvests
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CHAR_Harvests }
     * 
     */
    public CHAR_Harvests createCHAR_Harvests() {
        return new CHAR_Harvests();
    }

    /**
     * Create an instance of {@link CHAR_Harvest }
     * 
     */
    public CHAR_Harvest createCHAR_Harvest() {
        return new CHAR_Harvest();
    }

    /**
     * Create an instance of {@link CHAR_Specimen }
     * 
     */
    public CHAR_Specimen createCHAR_Specimen() {
        return new CHAR_Specimen();
    }

    /**
     * Create an instance of {@link CHAR_GeoLocation }
     * 
     */
    public CHAR_GeoLocation createCHAR_GeoLocation() {
        return new CHAR_GeoLocation();
    }

}
