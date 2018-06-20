package fi.riista.integration.paytrail.rest.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "type")
@XmlEnum
public enum ProductType {

    @XmlEnumValue("1")
    NORMAL("1"),

    @XmlEnumValue("2")
    POSTAL("2"),

    @XmlEnumValue("3")
    HANDLING("3"),

    @XmlEnumValue("")
    BLANK("");

    private final String value;

    ProductType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProductType fromValue(String v) {
        for (ProductType c : ProductType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
