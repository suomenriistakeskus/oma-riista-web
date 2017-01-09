
package fi.riista.integration.luke_export.mooselikeharvests;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gameMarking.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="gameMarking"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="NOT_MARKED"/&gt;
 *     &lt;enumeration value="COLLAR_OR_RADIO_TRANSMITTER"/&gt;
 *     &lt;enumeration value="LEG_RING_OR_WING_TAG"/&gt;
 *     &lt;enumeration value="EARMARK"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gameMarking", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
@XmlEnum
public enum LEM_GameMarking {

    NOT_MARKED,
    COLLAR_OR_RADIO_TRANSMITTER,
    LEG_RING_OR_WING_TAG,
    EARMARK;

    public String value() {
        return name();
    }

    public static LEM_GameMarking fromValue(String v) {
        return valueOf(v);
    }

}
