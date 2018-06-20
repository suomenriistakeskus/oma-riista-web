
package fi.riista.integration.luke_import.model.v1_0;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for _EsiintyminenType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="_EsiintyminenType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value=""/&gt;
 *     &lt;enumeration value="Ei"/&gt;
 *     &lt;enumeration value="Kyllä"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "_EsiintyminenType", namespace = "http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd")
@XmlEnum
public enum MooseDataCardGameSpeciesAppearance {

    @XmlEnumValue("")
    UNDEFINED(""),
    @XmlEnumValue("Ei")
    NO("Ei"),
    @XmlEnumValue("Kyll\u00e4")
    YES("Kyll\u00e4");
    private final String value;

    MooseDataCardGameSpeciesAppearance(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MooseDataCardGameSpeciesAppearance fromValue(String v) {
        for (MooseDataCardGameSpeciesAppearance c: MooseDataCardGameSpeciesAppearance.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
