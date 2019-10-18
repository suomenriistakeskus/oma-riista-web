
package fi.riista.integration.common.export.harvests;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gameFitnessClass.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="gameFitnessClass"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="ERINOMAINEN"/&gt;
 *     &lt;enumeration value="NORMAALI"/&gt;
 *     &lt;enumeration value="LAIHA"/&gt;
 *     &lt;enumeration value="NAANTYNYT"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gameFitnessClass")
@XmlEnum
public enum CHAR_GameFitnessClass {

    ERINOMAINEN,
    NORMAALI,
    LAIHA,
    NAANTYNYT;

    public String value() {
        return name();
    }

    public static CHAR_GameFitnessClass fromValue(String v) {
        return valueOf(v);
    }

}
