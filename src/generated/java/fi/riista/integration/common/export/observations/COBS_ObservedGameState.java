
package fi.riista.integration.common.export.observations;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for observedGameState.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="observedGameState"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="HEALTHY"/&gt;
 *     &lt;enumeration value="ILL"/&gt;
 *     &lt;enumeration value="WOUNDED"/&gt;
 *     &lt;enumeration value="CARCASS"/&gt;
 *     &lt;enumeration value="DEAD"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "observedGameState")
@XmlEnum
public enum COBS_ObservedGameState {

    HEALTHY,
    ILL,
    WOUNDED,
    CARCASS,
    DEAD;

    public String value() {
        return name();
    }

    public static COBS_ObservedGameState fromValue(String v) {
        return valueOf(v);
    }

}
