
package fi.riista.integration.luke_import.model.v1_0;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for _Metsästysalueemme_onType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="_Metsästysalueemme_onType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value=""/&gt;
 *     &lt;enumeration value="Kesälaidunaluetta"/&gt;
 *     &lt;enumeration value="Talvilaidunaluetta"/&gt;
 *     &lt;enumeration value="Molempia"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "_Mets\u00e4stysalueemme_onType", namespace = "http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd")
@XmlEnum
public enum MooseDataCardHarvestAreaType {

    @XmlEnumValue("")
    UNDEFINED(""),
    @XmlEnumValue("Kes\u00e4laidunaluetta")
    SUMMER_PASTURE("Kes\u00e4laidunaluetta"),
    @XmlEnumValue("Talvilaidunaluetta")
    WINTER_PASTURE("Talvilaidunaluetta"),
    @XmlEnumValue("Molempia")
    BOTH("Molempia");
    private final String value;

    MooseDataCardHarvestAreaType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MooseDataCardHarvestAreaType fromValue(String v) {
        for (MooseDataCardHarvestAreaType c: MooseDataCardHarvestAreaType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
