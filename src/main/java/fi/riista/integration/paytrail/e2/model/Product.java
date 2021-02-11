package fi.riista.integration.paytrail.e2.model;

import fi.riista.util.BigDecimalMoney;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class Product {
    @NotBlank
    @Length(max = 255)
    @Pattern(regexp = "[\\pL-0-9- \"\\',()\\[\\]{}*\\/+\\-_,.:&!?@#$£=*;~]*")
    private String title;

    @Length(max = 16)
    @Pattern(regexp = "\\d{0,16}")
    private String id;

    @Min(1)
    private Integer quantity;

    @NotNull
    private BigDecimalMoney unitPrice;

    @Min(0)
    @Max(100)
    @NotNull
    private Integer vatPercent;

    @Min(0)
    @Max(100)
    private Integer discountPercent;

    @Min(1)
    @Max(3)
    private Integer type;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimalMoney getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(final BigDecimalMoney unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getVatPercent() {
        return vatPercent;
    }

    public void setVatPercent(final Integer vatPercent) {
        this.vatPercent = vatPercent;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(final Integer discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Integer getType() {
        return type;
    }

    public void setType(final Integer type) {
        this.type = type;
    }
}
