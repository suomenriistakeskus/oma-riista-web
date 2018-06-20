
package fi.riista.integration.metsastajarekisteri.shootingtest;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ShootingTestType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ShootingTestType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="MOOSE"/&gt;
 *     &lt;enumeration value="BEAR"/&gt;
 *     &lt;enumeration value="ROE_DEER"/&gt;
 *     &lt;enumeration value="BOW"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ShootingTestType")
@XmlEnum
public enum MR_ShootingTestType {

    MOOSE,
    BEAR,
    ROE_DEER,
    BOW;

    public String value() {
        return name();
    }

    public static MR_ShootingTestType fromValue(String v) {
        return valueOf(v);
    }

}
