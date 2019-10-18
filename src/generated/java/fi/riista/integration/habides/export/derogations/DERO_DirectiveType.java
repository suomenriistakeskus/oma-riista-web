
package fi.riista.integration.habides.export.derogations;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for directiveType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="directiveType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="http://rod.eionet.europa.eu/obligations/268"/&gt;
 *     &lt;enumeration value="http://rod.eionet.europa.eu/obligations/276"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "directiveType")
@XmlEnum
public enum DERO_DirectiveType {


    /**
     * Habitats Directive
     * 
     */
    @XmlEnumValue("http://rod.eionet.europa.eu/obligations/268")
    HTTP_ROD_EIONET_EUROPA_EU_OBLIGATIONS_268("http://rod.eionet.europa.eu/obligations/268"),

    /**
     * Birds Directive
     * 
     */
    @XmlEnumValue("http://rod.eionet.europa.eu/obligations/276")
    HTTP_ROD_EIONET_EUROPA_EU_OBLIGATIONS_276("http://rod.eionet.europa.eu/obligations/276");
    private final String value;

    DERO_DirectiveType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DERO_DirectiveType fromValue(String v) {
        for (DERO_DirectiveType c: DERO_DirectiveType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
