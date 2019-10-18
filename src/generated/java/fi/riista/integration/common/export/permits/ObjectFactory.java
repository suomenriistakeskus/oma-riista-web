
package fi.riista.integration.common.export.permits;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.riista.integration.common.export.permits package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.riista.integration.common.export.permits
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CPER_Permits }
     * 
     */
    public CPER_Permits createCPER_Permits() {
        return new CPER_Permits();
    }

    /**
     * Create an instance of {@link CPER_Permit }
     * 
     */
    public CPER_Permit createCPER_Permit() {
        return new CPER_Permit();
    }

    /**
     * Create an instance of {@link CPER_PermitSpeciesAmount }
     * 
     */
    public CPER_PermitSpeciesAmount createCPER_PermitSpeciesAmount() {
        return new CPER_PermitSpeciesAmount();
    }

    /**
     * Create an instance of {@link CPER_PermitPartner }
     * 
     */
    public CPER_PermitPartner createCPER_PermitPartner() {
        return new CPER_PermitPartner();
    }

    /**
     * Create an instance of {@link CPER_GeoLocation }
     * 
     */
    public CPER_GeoLocation createCPER_GeoLocation() {
        return new CPER_GeoLocation();
    }

    /**
     * Create an instance of {@link CPER_ValidityTimeInterval }
     * 
     */
    public CPER_ValidityTimeInterval createCPER_ValidityTimeInterval() {
        return new CPER_ValidityTimeInterval();
    }

}
