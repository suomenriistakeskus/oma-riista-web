
package fi.riista.integration.common.model;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.riista.integration.common.model package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.riista.integration.common.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link C_Address }
     * 
     */
    public C_Address createC_Address() {
        return new C_Address();
    }

    /**
     * Create an instance of {@link C_TypeCodeAndName }
     * 
     */
    public C_TypeCodeAndName createC_TypeCodeAndName() {
        return new C_TypeCodeAndName();
    }

}
