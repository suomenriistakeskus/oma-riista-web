
package fi.riista.integration.luke_export.deerharvests;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dataSource.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="dataSource"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="WEB"/&gt;
 *     &lt;enumeration value="MOOSE_DATA_CARD"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "dataSource", namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
@XmlEnum
public enum LED_DataSource {

    WEB,
    MOOSE_DATA_CARD;

    public String value() {
        return name();
    }

    public static LED_DataSource fromValue(String v) {
        return valueOf(v);
    }

}
