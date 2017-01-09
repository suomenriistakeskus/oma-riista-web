package fi.riista.feature.common.entity;

import fi.riista.validation.Bic;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Embeddable
@Access(AccessType.FIELD)
public class BicEntity implements Serializable {

    @Column(length = 11)
    @Size.List({@Size(min = 8, max = 8), @Size(min = 11, max = 11)})
    @Bic
    @Convert(converter = BicConverter.class)
    private String bic;

    public BicEntity() {
    }

    public BicEntity(String bic) {
        this.bic = bic;
    }

    public String getValue() {
        return this.bic;
    }

    public void setValue(String value) {
        this.bic = value;
    }

}
