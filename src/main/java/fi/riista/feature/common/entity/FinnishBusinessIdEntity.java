package fi.riista.feature.common.entity;

import fi.riista.validation.FinnishBusinessId;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Embeddable
@Access(AccessType.FIELD)
public class FinnishBusinessIdEntity implements Serializable {

    @Column(length = 9)
    @Size(min = 9, max = 9)
    @FinnishBusinessId
    @Convert(converter = FinnishBusinessIdConverter.class)
    private String businessId;

    public FinnishBusinessIdEntity() {
    }

    public FinnishBusinessIdEntity(String businessId) {
        this.businessId = businessId;
    }

    public String getValue() {
        return this.businessId;
    }

    public void setValue(String value) {
        this.businessId = value;
    }

}