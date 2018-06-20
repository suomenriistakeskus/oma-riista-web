package fi.riista.integration.paytrail.rest.model;

import fi.riista.integration.support.TwoDecimalBigDecimalAdapter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "title",
        "code",
        "amount",
        "price",
        "vat",
        "discount",
        "type"
})
public class Product {

    @NotBlank
    @Length(max = 255)
    @ValidPaytrailRestString
    @XmlElement(name = "title", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String title;

    @Length(max = 16)
    @XmlElement(name = "code")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String code;

    @DecimalMin("0.01")
    @XmlElement(name = "amount", required = true)
    @XmlJavaTypeAdapter(TwoDecimalBigDecimalAdapter.class)
    private BigDecimal amount;

    @NotNull
    @DecimalMin("0.65")
    @DecimalMax("499999")
    @XmlElement(name = "price", required = true)
    @XmlJavaTypeAdapter(TwoDecimalBigDecimalAdapter.class)
    private BigDecimal price;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @XmlElement(name = "vat", required = true)
    @XmlJavaTypeAdapter(TwoDecimalBigDecimalAdapter.class)
    private BigDecimal vat;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @XmlElement(name = "discount")
    @XmlJavaTypeAdapter(TwoDecimalBigDecimalAdapter.class)
    private BigDecimal discount;

    @XmlElement(name = "type")
    private ProductType type;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getVat() {
        return vat;
    }

    public void setVat(final BigDecimal vat) {
        this.vat = vat;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(final BigDecimal discount) {
        this.discount = discount;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(final ProductType type) {
        this.type = type;
    }
}
