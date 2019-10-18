
package fi.riista.integration.common.export.harvests;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gameAntlersType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="gameAntlersType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="HANKO"/&gt;
 *     &lt;enumeration value="LAPIO"/&gt;
 *     &lt;enumeration value="SEKA"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gameAntlersType")
@XmlEnum
public enum CHAR_GameAntlersType {

    HANKO,
    LAPIO,
    SEKA;

    public String value() {
        return name();
    }

    public static CHAR_GameAntlersType fromValue(String v) {
        return valueOf(v);
    }

}
