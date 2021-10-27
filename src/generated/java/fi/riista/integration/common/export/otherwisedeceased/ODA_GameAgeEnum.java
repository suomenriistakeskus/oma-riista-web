
package fi.riista.integration.common.export.otherwisedeceased;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gameAgeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="gameAgeEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="ADULT"/&gt;
 *     &lt;enumeration value="YOUNG"/&gt;
 *     &lt;enumeration value="UNKNOWN"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gameAgeEnum")
@XmlEnum
public enum ODA_GameAgeEnum {


    /**
     * Adult
     * 
     */
    ADULT,

    /**
     * Under 1 year old
     * 
     */
    YOUNG,

    /**
     * Age unknown
     * 
     */
    UNKNOWN;

    public String value() {
        return name();
    }

    public static ODA_GameAgeEnum fromValue(String v) {
        return valueOf(v);
    }

}
