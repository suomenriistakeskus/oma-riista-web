
package fi.riista.integration.koulutusportaali.other;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TehtavaTyyppi.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TehtavaTyyppi"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="PETOYHDYSHENKILO"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "TehtavaTyyppi")
@XmlEnum
public enum OTH_TehtavaTyyppi {

    PETOYHDYSHENKILO;

    public String value() {
        return name();
    }

    public static OTH_TehtavaTyyppi fromValue(String v) {
        return valueOf(v);
    }

}
