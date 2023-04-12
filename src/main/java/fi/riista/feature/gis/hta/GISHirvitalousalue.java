package fi.riista.feature.gis.hta;

import com.querydsl.core.annotations.QueryDelegate;
import fi.riista.feature.common.entity.HasID;
import fi.riista.util.LocalisedString;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Persistable;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Access(AccessType.FIELD)
@Table(name = "hta")
public class GISHirvitalousalue implements Persistable<Integer>, HasID<Integer> {

    public static final String ID_COLUMN_NAME = "hta_id";

    private Integer id;

    @NotNull
    @Size(max = 255)
    @Column(name = "numero", nullable = false)
    private String number;

    @NotNull
    @Size(max = 255)
    @Column(name = "nimi", nullable = false)
    private String nameFinnish;

    @NotNull
    @Size(max = 255)
    @Column(name = "nimiSe", nullable = false)
    private String nameSwedish;

    @NotNull
    @Size(max = 255)
    @Column(name = "nimiLy", nullable = false)
    private String nameAbbrv;

    @NotNull
    @Type(type = "jts_geometry")
    @Column(nullable = false, columnDefinition = "Geometry")
    private Geometry geom;

    @OneToMany(
            mappedBy = "hta",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RHYHirvitalousalue> rhyHTAs = new ArrayList<>();

    @Nonnull
    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFinnish, nameSwedish);
    }

    // For persistence provider
    protected GISHirvitalousalue() {
    }

    public GISHirvitalousalue(final String number, final String nameFinnish, final String nameAbbrv, final String nameSwedish, final Geometry geom) {
        this.number = number;
        this.nameFinnish = nameFinnish;
        this.nameAbbrv = nameAbbrv;
        this.nameSwedish = nameSwedish;
        this.geom = geom;
    }

    @Override
    public boolean isNew() {
        return id != null;
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof Persistable)) {
            return false;
        }

        final Persistable<?> thatPersistable = (Persistable<?>) that;

        return null != this.getId() && this.getId().equals(thatPersistable.getId());
    }

    @Override
    public int hashCode() {
        return null == getId() ? 0 : 17 + getId().hashCode() * 31;
    }

    // QueryDSL delegates -->

    @QueryDelegate(GISHirvitalousalue.class)
    public static fi.riista.util.QLocalisedString nameLocalisation(final QGISHirvitalousalue hta) {
        return new fi.riista.util.QLocalisedString(hta.nameFinnish, hta.nameSwedish);
    }

    // Accessors -->

    @Override
    @Id
    @Access(value = AccessType.PROPERTY)
    @Column(name = "gid", nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getNameFinnish() {
        return nameFinnish;
    }

    public void setNameFinnish(final String nameFinnish) {
        this.nameFinnish = nameFinnish;
    }

    public String getNameSwedish() {
        return nameSwedish;
    }

    public void setNameSwedish(final String nameSwedish) {
        this.nameSwedish = nameSwedish;
    }

    public String getNameAbbrv() {
        return nameAbbrv;
    }

    public void setNameAbbrv(final String nameAbbrv) {
        this.nameAbbrv = nameAbbrv;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(final Geometry geom) {
        this.geom = geom;
    }


    public List<RHYHirvitalousalue> getRhyHTAs() {
        return rhyHTAs;
    }

    public void setRhyHTAs(final List<RHYHirvitalousalue> rhyHTAs) {
        this.rhyHTAs = rhyHTAs;
    }
}
