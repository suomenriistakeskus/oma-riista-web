
package fi.riista.integration.metsastajarekisteri.jht;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.riista.integration.metsastajarekisteri.jht package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.riista.integration.metsastajarekisteri.jht
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MR_JHT_Jht }
     * 
     */
    public MR_JHT_Jht createMR_JHT_Jht() {
        return new MR_JHT_Jht();
    }

    /**
     * Create an instance of {@link MR_JHT_Occupation }
     * 
     */
    public MR_JHT_Occupation createMR_JHT_Occupation() {
        return new MR_JHT_Occupation();
    }

}
