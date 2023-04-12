package fi.riista.integration.paytrail.checkout.model;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class Item {

    // Unit price in cents (EUR)
    @NotNull
    private Integer unitPrice;
    @Min(1)
    private Integer units;

    @Min(0)
    @Max(100)
    @NotNull
    private Integer vatPercentage;

    @NotBlank
    @Length(max = 255)
    @Pattern(regexp = "[\\pL-0-9- \"\\',()\\[\\]{}*\\/+\\-_,.:&!?@#$£=*;~]*")
    private String productCode;

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(final Integer unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getUnits() {
        return units;
    }

    public void setUnits(final Integer units) {
        this.units = units;
    }

    public Integer getVatPercentage() {
        return vatPercentage;
    }

    public void setVatPercentage(final Integer vatPercentage) {
        this.vatPercentage = vatPercentage;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(final String productCode) {
        this.productCode = productCode;
    }
}
