
package fi.riista.integration.luke_export.mooselikeharvests;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mooseHuntingAreaType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="mooseHuntingAreaType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="SUMMER_PASTURE"/&gt;
 *     &lt;enumeration value="WINTER_PASTURE"/&gt;
 *     &lt;enumeration value="BOTH"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "mooseHuntingAreaType", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
@XmlEnum
public enum LEM_MooseHuntingAreaType {

    SUMMER_PASTURE,
    WINTER_PASTURE,
    BOTH;

    public String value() {
        return name();
    }

    public static LEM_MooseHuntingAreaType fromValue(String v) {
        return valueOf(v);
    }

}
