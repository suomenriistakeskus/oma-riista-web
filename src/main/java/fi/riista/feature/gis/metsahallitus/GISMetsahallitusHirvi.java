package fi.riista.feature.gis.metsahallitus;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.Persistable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;


@Entity
@Access(AccessType.FIELD)
@Table(name = "mh_hirvi")
public class GISMetsahallitusHirvi implements Persistable<Integer> {

    private Integer id;

    @Range(min = 2000, max = 2100)
    @Column(nullable = false)
    private int vuosi;

    @Column(nullable = false)
    private int koodi;

    @NotBlank
    @Size(max = 255)
    @Column(length = 255, nullable = false)
    private String nimi;

    @Column(nullable = false)
    private long pintaAla;

    // For persistence provider
    GISMetsahallitusHirvi() {
    }

    public GISMetsahallitusHirvi(int vuosi, int koodi, String nimi, long pintaAla) {
        this.vuosi = vuosi;
        this.koodi = koodi;
        this.nimi = nimi;
        this.pintaAla = pintaAla;
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
        if (this == that) return true;
        if (!(that instanceof Persistable)) return false;

        final Persistable<?> thatPersistable = (Persistable<?>) that;

        return null != this.getId() && this.getId().equals(thatPersistable.getId());
    }

    @Override
    public int hashCode() {
        return null == getId() ? 0 : 17 + getId().hashCode() * 31;
    }

}
