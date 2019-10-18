
package fi.riista.integration.common.export.srva;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SRVAEventName.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SRVAEventName"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="ACCIDENT"/&gt;
 *     &lt;enumeration value="DEPORTATION"/&gt;
 *     &lt;enumeration value="INJURED_ANIMAL"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "SRVAEventName")
@XmlEnum
public enum CEV_SRVAEventName {

    ACCIDENT,
    DEPORTATION,
    INJURED_ANIMAL;

    public String value() {
        return name();
    }

    public static CEV_SRVAEventName fromValue(String v) {
        return valueOf(v);
    }

}
