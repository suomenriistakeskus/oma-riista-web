
package fi.riista.integration.metsastajarekisteri.shootingtest;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.riista.integration.metsastajarekisteri.shootingtest package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.riista.integration.metsastajarekisteri.shootingtest
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MR_ShootingTestRegistry }
     * 
     */
    public MR_ShootingTestRegistry createMR_ShootingTestRegistry() {
        return new MR_ShootingTestRegistry();
    }

    /**
     * Create an instance of {@link MR_PersonList }
     * 
     */
    public MR_PersonList createMR_PersonList() {
        return new MR_PersonList();
    }

    /**
     * Create an instance of {@link MR_ShootingTest }
     * 
     */
    public MR_ShootingTest createMR_ShootingTest() {
        return new MR_ShootingTest();
    }

    /**
     * Create an instance of {@link MR_ShootingTestList }
     * 
     */
    public MR_ShootingTestList createMR_ShootingTestList() {
        return new MR_ShootingTestList();
    }

    /**
     * Create an instance of {@link MR_Person }
     * 
     */
    public MR_Person createMR_Person() {
        return new MR_Person();
    }

}
