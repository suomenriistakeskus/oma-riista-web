package fi.riista.integration.paytrail.rest.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "errorCode",
        "errorMessage"
})
@XmlRootElement(name = "error")
public class ErrorMessage {

    @XmlElement(name = "errorCode")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String errorCode;

    @XmlElement(name = "errorMessage")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String errorMessage;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
