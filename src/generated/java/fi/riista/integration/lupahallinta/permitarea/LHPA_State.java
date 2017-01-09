
package fi.riista.integration.lupahallinta.permitarea;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for state.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="state"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="INCOMPLETE"/&gt;
 *     &lt;enumeration value="READY"/&gt;
 *     &lt;enumeration value="LOCKED"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "state", namespace = "http://riista.fi/integration/lupahallinta/export/permitarea")
@XmlEnum
public enum LHPA_State {


    /**
     * Permit area is not ready to be used in Lupahallinta.
     * 
     */
    INCOMPLETE,

    /**
     * Permit area is ready to be used in Lupahallinta.
     * 
     */
    READY,

    /**
     * Permit area is used in Lupahallinta.
     * 
     */
    LOCKED;

    public String value() {
        return name();
    }

    public static LHPA_State fromValue(String v) {
        return valueOf(v);
    }

}
