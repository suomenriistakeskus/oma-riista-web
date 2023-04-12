package fi.riista.integration.paytrail.checkout.model;

public class PaymentMethodGroupData {

    private PaymentMethodGroup id;
    private String name;
    private String icon;
    private String svg;

    public PaymentMethodGroup getId() {
        return id;
    }

    public void setId(final PaymentMethodGroup id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public String getSvg() {
        return svg;
    }

    public void setSvg(final String svg) {
        this.svg = svg;
    }
}
