
package fi.riista.integration.common.export.huntingsummaries;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.riista.integration.common.export.huntingsummaries package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.riista.integration.common.export.huntingsummaries
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CSUM_HuntingSummaries }
     * 
     */
    public CSUM_HuntingSummaries createCSUM_HuntingSummaries() {
        return new CSUM_HuntingSummaries();
    }

    /**
     * Create an instance of {@link CSUM_ClubHuntingSummary }
     * 
     */
    public CSUM_ClubHuntingSummary createCSUM_ClubHuntingSummary() {
        return new CSUM_ClubHuntingSummary();
    }

    /**
     * Create an instance of {@link CSUM_GeoLocation }
     * 
     */
    public CSUM_GeoLocation createCSUM_GeoLocation() {
        return new CSUM_GeoLocation();
    }

}
