package fi.riista.feature.huntingclub.area;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.util.LocalisedString;
import fi.riista.util.RandomStringUtil;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.SecureRandom;

@Entity
@Access(value = AccessType.FIELD)
public class HuntingClubArea extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "hunting_club_area_id";

    private Long id;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HuntingClub club;

    @Range(min = 2000, max = 2100)
    @Column(nullable = false)
    private int huntingYear;

    @Range(min = 2000, max = 2100)
    @Column(nullable = false)
    private int metsahallitusYear;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameFinnish;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameSwedish;

    @Size(min = 8, max = 255)
    @Column
    private String externalId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(unique = true)
    private GISZone zone;

    @Nonnull
    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFinnish, nameSwedish);
    }

    public HuntingClubArea() {
    }

    public HuntingClubArea(final HuntingClub club,
                           final String nameFinnish,
                           final String nameSwedish,
                           final int huntingYear,
                           final int metsahallitusYear,
                           final String externalId) {
        this.club = club;
        this.active = true;
        this.nameFinnish = nameFinnish;
        this.nameSwedish = nameSwedish;
        this.huntingYear = huntingYear;
        this.metsahallitusYear = metsahallitusYear;
        this.externalId = externalId;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HuntingClub getClub() {
        return club;
    }

    public void setClub(HuntingClub club) {
        this.club = club;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public int getMetsahallitusYear() {
        return metsahallitusYear;
    }

    public void setMetsahallitusYear(final int metsahallitusYear) {
        this.metsahallitusYear = metsahallitusYear;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNameFinnish() {
        return nameFinnish;
    }

    public void setNameFinnish(String nameFinnish) {
        this.nameFinnish = nameFinnish;
    }

    public String getNameSwedish() {
        return nameSwedish;
    }

    public void setNameSwedish(String nameSwedish) {
        this.nameSwedish = nameSwedish;
    }

    public String getExternalId() {
        return externalId;
    }

    public GISZone getZone() {
        return this.zone;
    }

    public void setZone(GISZone zone) {
        this.zone = zone;
    }

    public void generateAndStoreExternalId(final SecureRandom random) {
        if (this.externalId != null) {
            throw new IllegalStateException("Cannot update existing externalId");
        }
        this.externalId = RandomStringUtil.generateExternalId(random);
    }
}
