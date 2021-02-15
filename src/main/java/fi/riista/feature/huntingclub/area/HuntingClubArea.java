package fi.riista.feature.huntingclub.area;

import com.querydsl.core.annotations.QueryDelegate;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.util.LocalisedString;
import fi.riista.util.RandomStringUtil;
import javax.validation.constraints.NotBlank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Entity
@Access(value = AccessType.FIELD)
public class HuntingClubArea extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "hunting_club_area_id";

    public static final int MIN_YEAR = 2000;
    public static final int MAX_YEAR = 2100;

    public static int calculateMetsahallitusYear(final int huntingYear, final int latestMetsahallitusYear) {
        return huntingYear <= latestMetsahallitusYear ? huntingYear : latestMetsahallitusYear;
    }

    private Long id;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HuntingClub club;

    @Min(MIN_YEAR)
    @Max(MAX_YEAR)
    @Column(nullable = false)
    private int huntingYear;

    @Min(MIN_YEAR)
    @Max(MAX_YEAR)
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

    public Optional<Set<Long>> getZoneIdSet() {
        return Optional.ofNullable(getZone()).map(HasID::getId).map(Collections::singleton);
    }

    public boolean isGeometryEmpty() {
        return this.zone == null || this.zone.isGeometryEmpty();
    }

    public boolean isMhYearMismatchToHuntingYear() {
        return huntingYear != metsahallitusYear;
    }

    public HuntingClubArea() {
    }

    public HuntingClubArea(@Nonnull final HuntingClub club,
                           @Nonnull final String nameFinnish,
                           @Nonnull final String nameSwedish,
                           final int huntingYear,
                           final int metsahallitusYear,
                           @Nullable final String externalId) {

        this.club = Objects.requireNonNull(club, "club is null");
        this.active = true;
        this.nameFinnish = Objects.requireNonNull(nameFinnish, "nameFinnish is null");
        this.nameSwedish = Objects.requireNonNull(nameSwedish, "nameSwedish is null");
        this.huntingYear = huntingYear;
        this.metsahallitusYear = metsahallitusYear;
        this.externalId = externalId;
    }

    public void generateAndStoreExternalId(final SecureRandom random) {
        if (this.externalId != null) {
            throw new IllegalStateException("Cannot update existing externalId");
        }
        this.externalId = RandomStringUtil.generateExternalId(random);
    }

    // QueryDSL delegates -->

    @QueryDelegate(HuntingClubArea.class)
    public static fi.riista.util.QLocalisedString nameLocalisation(final QHuntingClubArea area) {
        return new fi.riista.util.QLocalisedString(area.nameFinnish, area.nameSwedish);
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
}
