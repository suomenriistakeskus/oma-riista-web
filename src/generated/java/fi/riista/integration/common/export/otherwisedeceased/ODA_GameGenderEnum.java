
package fi.riista.integration.common.export.otherwisedeceased;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gameGenderEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="gameGenderEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="FEMALE"/&gt;
 *     &lt;enumeration value="MALE"/&gt;
 *     &lt;enumeration value="UNKNOWN"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gameGenderEnum")
@XmlEnum
public enum ODA_GameGenderEnum {


    /**
     * Female
     * 
     */
    FEMALE,

    /**
     * Male
     * 
     */
    MALE,

    /**
     * Gender unknown
     * 
     */
    UNKNOWN;

    public String value() {
        return name();
    }

    public static ODA_GameGenderEnum fromValue(String v) {
        return valueOf(v);
    }

}
