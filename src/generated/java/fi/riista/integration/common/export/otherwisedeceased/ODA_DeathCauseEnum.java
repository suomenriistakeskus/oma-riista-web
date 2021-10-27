
package fi.riista.integration.common.export.otherwisedeceased;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deathCauseEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="deathCauseEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="HIGHWAY_ACCIDENT"/&gt;
 *     &lt;enumeration value="RAILWAY_ACCIDENT"/&gt;
 *     &lt;enumeration value="SICKNESS_OR_STARVATION"/&gt;
 *     &lt;enumeration value="KILLED_BY_POLICES_ORDER"/&gt;
 *     &lt;enumeration value="NECESSITY"/&gt;
 *     &lt;enumeration value="ILLEGAL_KILLING"/&gt;
 *     &lt;enumeration value="OTHER"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "deathCauseEnum")
@XmlEnum
public enum ODA_DeathCauseEnum {


    /**
     * Highway accident
     * 
     */
    HIGHWAY_ACCIDENT,

    /**
     * Railway accident
     * 
     */
    RAILWAY_ACCIDENT,

    /**
     * Sickness or starvation
     * 
     */
    SICKNESS_OR_STARVATION,

    /**
     * Killed by police's order
     * 
     */
    KILLED_BY_POLICES_ORDER,

    /**
     * Necessity
     * 
     */
    NECESSITY,

    /**
     * Illegal killing (hunting crime, available conviction)
     * 
     */
    ILLEGAL_KILLING,

    /**
     * Other cause. Optional details in field otherCause.
     * 
     */
    OTHER;

    public String value() {
        return name();
    }

    public static ODA_DeathCauseEnum fromValue(String v) {
        return valueOf(v);
    }

}
