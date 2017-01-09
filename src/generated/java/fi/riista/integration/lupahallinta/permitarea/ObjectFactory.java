
package fi.riista.integration.lupahallinta.permitarea;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.riista.integration.lupahallinta.permitarea package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.riista.integration.lupahallinta.permitarea
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LHPA_PermitArea }
     * 
     */
    public LHPA_PermitArea createLHPA_PermitArea() {
        return new LHPA_PermitArea();
    }

    /**
     * Create an instance of {@link LHPA_NameWithOfficialCode }
     * 
     */
    public LHPA_NameWithOfficialCode createLHPA_NameWithOfficialCode() {
        return new LHPA_NameWithOfficialCode();
    }

    /**
     * Create an instance of {@link LHPA_Partner }
     * 
     */
    public LHPA_Partner createLHPA_Partner() {
        return new LHPA_Partner();
    }

}
