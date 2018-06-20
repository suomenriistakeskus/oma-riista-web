package fi.riista.feature.permit.area;

import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.zone.AreaEntity;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHta;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartner;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhy;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import fi.riista.util.RandomStringUtil;
import org.hibernate.validator.constraints.Range;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitArea extends AreaEntity<Long> {

    public enum StatusCode {
        // Permit area is not ready to be used in Lupahallinta
        INCOMPLETE,

        // Permit area processing is pending
        PENDING,

        // Permit area geometry processing has began
        PROCESSING,

        // Permit area geometry processing failed
        PROCESSING_FAILED,

        // Permit area is ready to be used in Lupahallinta
        READY,

        // Permit area is used in Lupahallinta
        LOCKED;
    }

    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCode status = StatusCode.INCOMPLETE;

    @NotNull
    @Column(nullable = false)
    private DateTime statusTime = DateUtil.now();

    @Size(max = 255)
    @Column
    private String nameFinnish;

    @Size(max = 255)
    @Column
    private String nameSwedish;

    @Range(min = 2000, max = 2100)
    @Column(nullable = false)
    private int huntingYear;

    @NotNull
    @Size(min = 8, max = 255)
    @Column(nullable = false)
    private String externalId;

    // Sisältääkö alue vähintään 1000 ha valtionmaata tietyissä pohjois-Suomen kunnissa, jotta ML 8§ voidaan soveltaa?
    @Column(nullable = false)
    private boolean freeHunting;

    // Owner
    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private HuntingClub club;

    // Computed union
    @NotNull
    @JoinColumn(nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private GISZone zone;

    @OneToMany(mappedBy = "harvestPermitArea")
    private Set<HarvestPermitAreaPartner> partners = new HashSet<>();

    @OneToMany(mappedBy = "harvestPermitArea")
    private Set<HarvestPermitAreaRhy> rhy = new HashSet<>();

    @OneToMany(mappedBy = "harvestPermitArea")
    private Set<HarvestPermitAreaHta> hta = new HashSet<>();

    public void generateAndStoreExternalId(final SecureRandom random) {
        if (this.externalId != null) {
            throw new IllegalStateException("Cannot update existing externalId");
        }
        this.externalId = RandomStringUtil.generateExternalId(random);
    }

    public Optional<HarvestPermitAreaPartner> findPartner(final HuntingClubArea huntingClubArea) {
        return getPartners().stream().filter(p -> p.getSourceArea().equals(huntingClubArea)).findAny();
    }

    public Optional<GISHirvitalousalue> findLargestHta() {
        return getHta().stream()
                .max(Comparator.comparingDouble(HarvestPermitAreaHta::getAreaSize))
                .map(HarvestPermitAreaHta::getHta);
    }

    @Override
    @Nonnull
    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFinnish, nameSwedish);
    }

    @Transient
    public void assertStatus(final StatusCode allowed) {
        assertStatus(EnumSet.of(allowed));
    }

    @Transient
    public void assertStatus(final EnumSet<StatusCode> allowed) {
        if (!allowed.contains(this.status)) {
            throw new IllegalHarvestPermitAreaStateTransitionException(
                    String.format("status should be %s was %s", allowed, this.status));
        }
    }

    @Transient
    public Optional<HarvestPermitAreaEvent> setStatusIncomplete() {
        assertStatus(EnumSet.of(StatusCode.READY, StatusCode.PENDING, StatusCode.PROCESSING_FAILED));
        return updateStatus(StatusCode.INCOMPLETE);
    }

    @Transient
    public Optional<HarvestPermitAreaEvent> setStatusPending() {
        assertStatus(EnumSet.of(StatusCode.INCOMPLETE, StatusCode.PROCESSING, StatusCode.PROCESSING_FAILED));
        return updateStatus(StatusCode.PENDING);
    }


    @Transient
    public Optional<HarvestPermitAreaEvent> setStatusProcessing() {
        assertStatus(EnumSet.of(StatusCode.PENDING, StatusCode.PROCESSING, StatusCode.PROCESSING_FAILED));
        return updateStatus(StatusCode.PROCESSING);
    }

    @Transient
    public Optional<HarvestPermitAreaEvent> setStatusProcessingFailed() {
        assertStatus(EnumSet.of(StatusCode.PENDING, StatusCode.PROCESSING));
        return updateStatus(StatusCode.PROCESSING_FAILED);
    }

    @Transient
    public Optional<HarvestPermitAreaEvent> setStatusReady() {
        assertStatus(EnumSet.of(StatusCode.PROCESSING));
        return updateStatus(StatusCode.READY);
    }

    @Transient
    public Optional<HarvestPermitAreaEvent> setStatusLocked() {
        assertStatus(EnumSet.of(StatusCode.READY));
        return updateStatus(StatusCode.LOCKED);
    }

    @Transient
    public Optional<HarvestPermitAreaEvent> setStatusUnlocked() {
        assertStatus(EnumSet.of(StatusCode.LOCKED));
        return updateStatus(StatusCode.INCOMPLETE);
    }

    @Transient
    private Optional<HarvestPermitAreaEvent> updateStatus(StatusCode code) {
        if (this.status == code) {
            return Optional.empty();
        }

        this.status = code;
        this.statusTime = DateUtil.now();

        return Optional.of(new HarvestPermitAreaEvent(this, code));
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "harvest_permit_area_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public StatusCode getStatus() {
        return status;
    }

    public DateTime getStatusTime() {
        return statusTime;
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

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

    public boolean isFreeHunting() {
        return freeHunting;
    }

    public void setFreeHunting(final boolean freeHunting) {
        this.freeHunting = freeHunting;
    }

    public HuntingClub getClub() {
        return club;
    }

    public void setClub(final HuntingClub club) {
        this.club = club;
    }

    @Override
    public GISZone getZone() {
        return zone;
    }

    @Override
    public void setZone(final GISZone zone) {
        this.zone = zone;
    }

    public Set<HarvestPermitAreaPartner> getPartners() {
        return partners;
    }

    public Set<HarvestPermitAreaRhy> getRhy() {
        return rhy;
    }

    public Set<HarvestPermitAreaHta> getHta() {
        return hta;
    }
}
