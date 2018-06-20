
package fi.riista.integration.srva.rvr;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.riista.integration.srva.rvr package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.riista.integration.srva.rvr
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RVR_SrvaEvent }
     * 
     */
    public RVR_SrvaEvent createRVR_SrvaEvent() {
        return new RVR_SrvaEvent();
    }

    /**
     * Create an instance of {@link RVR_SrvaEvents }
     * 
     */
    public RVR_SrvaEvents createRVR_SrvaEvents() {
        return new RVR_SrvaEvents();
    }

    /**
     * Create an instance of {@link RVR_GeoLocation }
     * 
     */
    public RVR_GeoLocation createRVR_GeoLocation() {
        return new RVR_GeoLocation();
    }

    /**
     * Create an instance of {@link RVR_SrvaSpecimen }
     * 
     */
    public RVR_SrvaSpecimen createRVR_SrvaSpecimen() {
        return new RVR_SrvaSpecimen();
    }

    /**
     * Create an instance of {@link RVR_SrvaEvent.RVR_Specimens }
     * 
     */
    public RVR_SrvaEvent.RVR_Specimens createRVR_SrvaEventRVR_Specimens() {
        return new RVR_SrvaEvent.RVR_Specimens();
    }

    /**
     * Create an instance of {@link RVR_SrvaEvent.RVR_Methods }
     * 
     */
    public RVR_SrvaEvent.RVR_Methods createRVR_SrvaEventRVR_Methods() {
        return new RVR_SrvaEvent.RVR_Methods();
    }

}
