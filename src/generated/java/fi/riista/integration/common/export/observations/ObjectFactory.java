
package fi.riista.integration.common.export.observations;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.riista.integration.common.export.observations package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.riista.integration.common.export.observations
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link COBS_Observations }
     * 
     */
    public COBS_Observations createCOBS_Observations() {
        return new COBS_Observations();
    }

    /**
     * Create an instance of {@link COBS_Observation }
     * 
     */
    public COBS_Observation createCOBS_Observation() {
        return new COBS_Observation();
    }

    /**
     * Create an instance of {@link COBS_ObservationSpecimen }
     * 
     */
    public COBS_ObservationSpecimen createCOBS_ObservationSpecimen() {
        return new COBS_ObservationSpecimen();
    }

    /**
     * Create an instance of {@link COBS_GeoLocation }
     * 
     */
    public COBS_GeoLocation createCOBS_GeoLocation() {
        return new COBS_GeoLocation();
    }

    /**
     * Create an instance of {@link COBS_FemaleAndCalfs }
     * 
     */
    public COBS_FemaleAndCalfs createCOBS_FemaleAndCalfs() {
        return new COBS_FemaleAndCalfs();
    }

}
