
package fi.riista.integration.common.export.srva;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SRVAEventType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SRVAEventType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="TRAFFIC_ACCIDENT"/&gt;
 *     &lt;enumeration value="RAILWAY_ACCIDENT"/&gt;
 *     &lt;enumeration value="ANIMAL_NEAR_HOUSES_AREA"/&gt;
 *     &lt;enumeration value="ANIMAL_AT_FOOD_DESTINATION"/&gt;
 *     &lt;enumeration value="INJURED_ANIMAL"/&gt;
 *     &lt;enumeration value="ANIMAL_ON_ICE"/&gt;
 *     &lt;enumeration value="OTHER"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "SRVAEventType")
@XmlEnum
public enum CEV_SRVAEventType {

    TRAFFIC_ACCIDENT,
    RAILWAY_ACCIDENT,
    ANIMAL_NEAR_HOUSES_AREA,
    ANIMAL_AT_FOOD_DESTINATION,
    INJURED_ANIMAL,
    ANIMAL_ON_ICE,
    OTHER;

    public String value() {
        return name();
    }

    public static CEV_SRVAEventType fromValue(String v) {
        return valueOf(v);
    }

}
