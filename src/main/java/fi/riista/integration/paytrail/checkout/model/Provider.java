package fi.riista.integration.paytrail.checkout.model;

import java.util.List;

public class Provider {

    private String url;
    private String icon;
    private String svg;

    private PaymentMethodGroup group;

    private String name;
    private String id;
    private List<PaytrailFormField> parameters;

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
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

    public PaymentMethodGroup getGroup() {
        return group;
    }

    public void setGroup(final PaymentMethodGroup group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public List<PaytrailFormField> getParameters() {
        return parameters;
    }

    public void setParameters(final List<PaytrailFormField> parameters) {
        this.parameters = parameters;
    }
}
