
package fi.riista.integration.common.export.observations;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for observedGameAge.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="observedGameAge"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="ADULT"/&gt;
 *     &lt;enumeration value="LT1Y"/&gt;
 *     &lt;enumeration value="_1TO2Y"/&gt;
 *     &lt;enumeration value="UNKNOWN"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "observedGameAge")
@XmlEnum
public enum COBS_ObservedGameAge {

    ADULT,
    LT1Y,
    _1TO2Y,
    UNKNOWN;

    public String value() {
        return name();
    }

    public static COBS_ObservedGameAge fromValue(String v) {
        return valueOf(v);
    }

}
