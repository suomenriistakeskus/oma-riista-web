package fi.riista.feature.common.entity;

import com.google.common.base.Preconditions;
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

    private static final int LENGTH = 9;

    @Column(length = LENGTH)
    @Size(min = LENGTH, max = LENGTH)
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

    public static FinnishBusinessIdEntity of(String value) {
        Preconditions.checkArgument(value.length() == LENGTH);
        FinnishBusinessIdEntity businessId = new FinnishBusinessIdEntity();
        businessId.setValue(value);
        return businessId;
    }
}
