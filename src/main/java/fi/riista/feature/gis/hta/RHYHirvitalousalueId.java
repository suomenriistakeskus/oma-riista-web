package fi.riista.feature.gis.hta;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class RHYHirvitalousalueId implements Serializable {


    @Column(name = "organisation_id")
    private Long organisationId;

    @Column(name = "hta_id")
    private Integer htaId;

    protected RHYHirvitalousalueId() {
    }

    public RHYHirvitalousalueId(
            final Long organisationId,
            final Integer htaId) {
        this.organisationId = organisationId;
        this.htaId = htaId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RHYHirvitalousalueId that = (RHYHirvitalousalueId) o;
        return Objects.equals(organisationId, that.organisationId) &&
                Objects.equals(htaId, that.htaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organisationId, htaId);
    }

}
