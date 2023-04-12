
package fi.riista.integration.srva.rvr;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for srvaEventTypeDetailsEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="srvaEventTypeDetailsEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="CARED_HOUSE_AREA"/&gt;
 *     &lt;enumeration value="FARM_ANIMAL_BUILDING"/&gt;
 *     &lt;enumeration value="URBAN_AREA"/&gt;
 *     &lt;enumeration value="CARCASS_AT_FOREST"/&gt;
 *     &lt;enumeration value="CARCASS_NEAR_HOUSES_AREA"/&gt;
 *     &lt;enumeration value="GARBAGE_CAN"/&gt;
 *     &lt;enumeration value="BEEHIVE"/&gt;
 *     &lt;enumeration value="OTHER"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "srvaEventTypeDetailsEnum", namespace = "http://riista.fi/integration/srva/rvr")
@XmlEnum
public enum RVR_SrvaEventTypeDetailsEnum {

    CARED_HOUSE_AREA,
    FARM_ANIMAL_BUILDING,
    URBAN_AREA,
    CARCASS_AT_FOREST,
    CARCASS_NEAR_HOUSES_AREA,
    GARBAGE_CAN,
    BEEHIVE,
    OTHER;

    public String value() {
        return name();
    }

    public static RVR_SrvaEventTypeDetailsEnum fromValue(String v) {
        return valueOf(v);
    }

}
