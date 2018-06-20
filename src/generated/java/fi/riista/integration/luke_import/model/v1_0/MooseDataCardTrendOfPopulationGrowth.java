
package fi.riista.integration.luke_import.model.v1_0;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for _MääräType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="_MääräType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value=""/&gt;
 *     &lt;enumeration value="vähentynyt"/&gt;
 *     &lt;enumeration value="ennallaan"/&gt;
 *     &lt;enumeration value="lisääntynyt"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "_M\u00e4\u00e4r\u00e4Type", namespace = "http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd")
@XmlEnum
public enum MooseDataCardTrendOfPopulationGrowth {

    @XmlEnumValue("")
    UNDEFINED(""),
    @XmlEnumValue("v\u00e4hentynyt")
    DECREASED("v\u00e4hentynyt"),
    @XmlEnumValue("ennallaan")
    UNCHANGED("ennallaan"),
    @XmlEnumValue("lis\u00e4\u00e4ntynyt")
    INCREASED("lis\u00e4\u00e4ntynyt");
    private final String value;

    MooseDataCardTrendOfPopulationGrowth(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MooseDataCardTrendOfPopulationGrowth fromValue(String v) {
        for (MooseDataCardTrendOfPopulationGrowth c: MooseDataCardTrendOfPopulationGrowth.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
