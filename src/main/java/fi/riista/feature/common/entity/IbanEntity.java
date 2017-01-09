package fi.riista.feature.common.entity;

import fi.riista.validation.Iban;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Embeddable
@Access(AccessType.FIELD)
public class IbanEntity implements Serializable {

    @Column(length = 34)
    @Size(min = 16, max = 34)
    @Iban
    @Convert(converter = IbanConverter.class)
    private String iban;

    public IbanEntity() {
    }

    public IbanEntity(String iban) {
        this.iban = iban;
    }

    public String getValue() {
        return this.iban;
    }

    public void setValue(String value) {
        this.iban = value;
    }

}
