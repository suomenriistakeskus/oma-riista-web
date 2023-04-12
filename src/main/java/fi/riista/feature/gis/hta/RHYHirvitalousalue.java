package fi.riista.feature.gis.hta;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.springframework.data.domain.Persistable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Access(AccessType.FIELD)
@Table(name = "rhy_hta")
public class RHYHirvitalousalue implements Persistable<RHYHirvitalousalueId> {

    public static final String ID_COLUMN_NAME = "hta_id";

    @EmbeddedId
    private RHYHirvitalousalueId id;

    @Column(name = "land_area_size", nullable = true)
    private Double landAreaSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    @MapsId("organisationId")
    private Riistanhoitoyhdistys rhy;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("htaId")
    private GISHirvitalousalue hta;

    protected RHYHirvitalousalue() {
    }

    public RHYHirvitalousalue(final Riistanhoitoyhdistys rhy, final GISHirvitalousalue hta, final Double landAreaSize) {
        this.rhy = rhy;
        this.hta = hta;
        this.landAreaSize = landAreaSize;
        this.id = new RHYHirvitalousalueId(rhy.getId(), hta.getId());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RHYHirvitalousalue that = (RHYHirvitalousalue) o;
        return Objects.equals(rhy, that.rhy) &&
                Objects.equals(hta, that.hta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rhy, hta);
    }

    public Double getLandAreaSize() {
        return landAreaSize;
    }

    public void setLandAreaSize(final Double landAreaSize) {
        this.landAreaSize = landAreaSize;
    }

    @Override
    public RHYHirvitalousalueId getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }
}
