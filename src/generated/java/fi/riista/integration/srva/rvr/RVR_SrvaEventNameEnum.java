
package fi.riista.integration.srva.rvr;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for srvaEventNameEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="srvaEventNameEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="ACCIDENT"/&gt;
 *     &lt;enumeration value="DEPORTATION"/&gt;
 *     &lt;enumeration value="INJURED_ANIMAL"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "srvaEventNameEnum", namespace = "http://riista.fi/integration/srva/rvr")
@XmlEnum
public enum RVR_SrvaEventNameEnum {

    ACCIDENT,
    DEPORTATION,
    INJURED_ANIMAL;

    public String value() {
        return name();
    }

    public static RVR_SrvaEventNameEnum fromValue(String v) {
        return valueOf(v);
    }

}
