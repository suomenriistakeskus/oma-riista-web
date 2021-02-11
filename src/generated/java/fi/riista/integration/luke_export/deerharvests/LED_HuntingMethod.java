
package fi.riista.integration.luke_export.deerharvests;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HuntingMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="HuntingMethod"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="STAND_HUNTING"/&gt;
 *     &lt;enumeration value="DOG_HUNTING"/&gt;
 *     &lt;enumeration value="MUU"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "HuntingMethod", namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
@XmlEnum
public enum LED_HuntingMethod {

    STAND_HUNTING,
    DOG_HUNTING,
    MUU;

    public String value() {
        return name();
    }

    public static LED_HuntingMethod fromValue(String v) {
        return valueOf(v);
    }

}
