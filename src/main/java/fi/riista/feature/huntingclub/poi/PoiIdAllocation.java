package fi.riista.feature.huntingclub.poi;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.huntingclub.HuntingClub;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static java.util.Objects.requireNonNull;

@Entity
@Access(value = AccessType.FIELD)
public class PoiIdAllocation extends LifecycleEntity<Long> {
    public static final String ID_COLUMN_NAME = "poi_id_allocation_id";

    private Long id;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HuntingClub club;


    @Column(nullable = false)
    private int idCounter;

    /* package */ PoiIdAllocation() {

    }

    public PoiIdAllocation(@Nonnull final HuntingClub club) {
        this.club = requireNonNull(club);
        this.idCounter = 1;

    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    public Long getId() {
        return this.id;
    }

    public HuntingClub getClub() {
        return club;
    }

    public void setClub(final HuntingClub club) {
        this.club = club;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public int getNextId() {
        return idCounter++;
    }

}
