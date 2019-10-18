
package fi.riista.integration.common.export.srva;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gameAge.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="gameAge"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="ADULT"/&gt;
 *     &lt;enumeration value="YOUNG"/&gt;
 *     &lt;enumeration value="UNKNOWN"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gameAge")
@XmlEnum
public enum CEV_GameAge {

    ADULT,
    YOUNG,
    UNKNOWN;

    public String value() {
        return name();
    }

    public static CEV_GameAge fromValue(String v) {
        return valueOf(v);
    }

}
