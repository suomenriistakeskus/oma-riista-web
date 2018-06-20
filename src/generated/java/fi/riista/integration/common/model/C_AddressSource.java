
package fi.riista.integration.common.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OsoiteLahde.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="OsoiteLahde"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="VTJ"/&gt;
 *     &lt;enumeration value="LupaHallinta"/&gt;
 *     &lt;enumeration value="Manual"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "OsoiteLahde")
@XmlEnum
public enum C_AddressSource {

    VTJ("VTJ"),
    @XmlEnumValue("LupaHallinta")
    LUPA_HALLINTA("LupaHallinta"),
    @XmlEnumValue("Manual")
    MANUAL("Manual");
    private final String value;

    C_AddressSource(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static C_AddressSource fromValue(String v) {
        for (C_AddressSource c: C_AddressSource.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
