
package fi.riista.integration.luke_export.mooselikeharvests;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for trendOfPopulationGrowth.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="trendOfPopulationGrowth"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="INCREASED"/&gt;
 *     &lt;enumeration value="UNCHANGED"/&gt;
 *     &lt;enumeration value="DECREASED"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "trendOfPopulationGrowth", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
@XmlEnum
public enum LEM_TrendOfPopulationGrowth {

    INCREASED,
    UNCHANGED,
    DECREASED;

    public String value() {
        return name();
    }

    public static LEM_TrendOfPopulationGrowth fromValue(String v) {
        return valueOf(v);
    }

}
