package fi.riista.feature.common.entity;

import fi.riista.validation.FinnishCreditorReference;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Embeddable
@Access(AccessType.FIELD)
public class CreditorReference implements Serializable {

    public static CreditorReference fromNullable(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        CreditorReference result = new CreditorReference();
        result.creditorReference = value;
        return result;
    }

    @Column(length = 20)
    @Size(max = 20)
    @FinnishCreditorReference
    @Convert(converter = CreditorReferenceConverter.class)
    private String creditorReference;

    public String getValue() {
        return this.creditorReference;
    }

    public void setValue(String value) {
        this.creditorReference = value;
    }

}
