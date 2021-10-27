
package fi.riista.integration.common.export.otherwisedeceased;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.riista.integration.common.export.otherwisedeceased package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.riista.integration.common.export.otherwisedeceased
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ODA_DeceasedAnimals }
     * 
     */
    public ODA_DeceasedAnimals createODA_DeceasedAnimals() {
        return new ODA_DeceasedAnimals();
    }

    /**
     * Create an instance of {@link ODA_DeceasedAnimal }
     * 
     */
    public ODA_DeceasedAnimal createODA_DeceasedAnimal() {
        return new ODA_DeceasedAnimal();
    }

    /**
     * Create an instance of {@link ODA_GeoLocation }
     * 
     */
    public ODA_GeoLocation createODA_GeoLocation() {
        return new ODA_GeoLocation();
    }

}
