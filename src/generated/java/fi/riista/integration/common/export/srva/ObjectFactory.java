
package fi.riista.integration.common.export.srva;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.riista.integration.common.export.srva package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.riista.integration.common.export.srva
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CEV_SrvaEvents }
     * 
     */
    public CEV_SrvaEvents createCEV_SrvaEvents() {
        return new CEV_SrvaEvents();
    }

    /**
     * Create an instance of {@link CEV_SRVAEvent }
     * 
     */
    public CEV_SRVAEvent createCEV_SRVAEvent() {
        return new CEV_SRVAEvent();
    }

    /**
     * Create an instance of {@link CEV_SRVASpecimen }
     * 
     */
    public CEV_SRVASpecimen createCEV_SRVASpecimen() {
        return new CEV_SRVASpecimen();
    }

    /**
     * Create an instance of {@link CEV_GeoLocation }
     * 
     */
    public CEV_GeoLocation createCEV_GeoLocation() {
        return new CEV_GeoLocation();
    }

}
