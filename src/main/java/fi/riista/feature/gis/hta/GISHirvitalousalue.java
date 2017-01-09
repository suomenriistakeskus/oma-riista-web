package fi.riista.feature.gis.hta;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.common.entity.HasID;
import fi.riista.util.LocalisedString;
import org.hibernate.annotations.Type;
import org.springframework.data.domain.Persistable;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
@Table(name = "hta")
public class GISHirvitalousalue implements Persistable<Integer>, HasID<Integer> {

    private Integer id;

    @NotNull
    @Column(name = "numero", nullable = false)
    private String number;

    @NotNull
    @Column(name = "nimi", nullable = false)
    private String nameFinnish;

    @NotNull
    @Column(name = "nimiLy", nullable = false)
    private String nameAbbrv;

    @NotNull
    @Column(name = "nimiSe", nullable = false)
    private String nameSwedish;

    @NotNull
    @Column(nullable = false)
    @Type(type = "org.hibernate.spatial.GeometryType")
    private Geometry geom;

    @Nonnull
    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFinnish, nameSwedish);
    }

    // For persistence provider
    GISHirvitalousalue() {
    }

    public GISHirvitalousalue(String number, String nameFinnish, String nameAbbrv, String nameSwedish, Geometry geom) {
        this.number = number;
        this.nameFinnish = nameFinnish;
        this.nameAbbrv = nameAbbrv;
        this.nameSwedish = nameSwedish;
        this.geom = geom;
    }

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

    @Override
    public boolean isNew() {
        return id != null;
    }

    @Override
    public boolean equals(Object that) {
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNameFinnish() {
        return nameFinnish;
    }

    public void setNameFinnish(String nameFinnish) {
        this.nameFinnish = nameFinnish;
    }

    public String getNameAbbrv() {
        return nameAbbrv;
    }

    public void setNameAbbrv(String nameAbbrv) {
        this.nameAbbrv = nameAbbrv;
    }

    public String getNameSwedish() {
        return nameSwedish;
    }

    public void setNameSwedish(String nameSwedish) {
        this.nameSwedish = nameSwedish;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(final Geometry geom) {
        this.geom = geom;
    }
}
