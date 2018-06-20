
package fi.riista.integration.srva.rvr;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sourceEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="sourceEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="GPS_DEVICE"/&gt;
 *     &lt;enumeration value="MANUAL"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "sourceEnum", namespace = "http://riista.fi/integration/srva/rvr")
@XmlEnum
public enum RVR_SourceEnum {

    GPS_DEVICE,
    MANUAL;

    public String value() {
        return name();
    }

    public static RVR_SourceEnum fromValue(String v) {
        return valueOf(v);
    }

}
