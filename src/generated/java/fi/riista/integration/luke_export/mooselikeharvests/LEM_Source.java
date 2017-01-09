
package fi.riista.integration.luke_export.mooselikeharvests;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for source.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="source"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="GPS_DEVICE"/&gt;
 *     &lt;enumeration value="MANUAL"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "source", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
@XmlEnum
public enum LEM_Source {

    GPS_DEVICE,
    MANUAL;

    public String value() {
        return name();
    }

    public static LEM_Source fromValue(String v) {
        return valueOf(v);
    }

}
