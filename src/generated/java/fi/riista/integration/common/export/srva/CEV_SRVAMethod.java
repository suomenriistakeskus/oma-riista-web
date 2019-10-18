
package fi.riista.integration.common.export.srva;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SRVAMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SRVAMethod"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="DOG"/&gt;
 *     &lt;enumeration value="PAIN_EQUIPMENT"/&gt;
 *     &lt;enumeration value="SOUND_EQUIPMENT"/&gt;
 *     &lt;enumeration value="TRACED_WITH_DOG"/&gt;
 *     &lt;enumeration value="TRACED_WITHOUT_DOG"/&gt;
 *     &lt;enumeration value="OTHER"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "SRVAMethod")
@XmlEnum
public enum CEV_SRVAMethod {

    DOG,
    PAIN_EQUIPMENT,
    SOUND_EQUIPMENT,
    TRACED_WITH_DOG,
    TRACED_WITHOUT_DOG,
    OTHER;

    public String value() {
        return name();
    }

    public static CEV_SRVAMethod fromValue(String v) {
        return valueOf(v);
    }

}
