package fi.riista.feature.harvestpermit.area;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.util.LocalisedString;
import fi.riista.util.RandomStringUtil;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitArea extends BaseEntity<Long> {
    public enum StatusCode {
        // Permit area is not ready to be used in Lupahallinta
        INCOMPLETE,

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

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameFinnish;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameSwedish;

    @Range(min = 2000, max = 2100)
    @Column(nullable = false)
    private int huntingYear;

    @NotNull
    @Size(min = 8, max = 255)
    @Column(nullable = false)
    private String externalId;

    // Owner
    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
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

    public void generateAndStoreExternalId(final SecureRandom random) {
        if (this.externalId != null) {
            throw new IllegalStateException("Cannot update existing externalId");
        }
        this.externalId = RandomStringUtil.generateExternalId(random);
    }

    public Optional<HarvestPermitAreaPartner> findPartner(final HuntingClubArea huntingClubArea) {
        return getPartners().stream().filter(p -> p.getSourceArea().equals(huntingClubArea)).findAny();
    }

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
        Preconditions.checkState(allowed.contains(this.status),
                "status should be %s was %s",
                allowed, this.status);
    }

    public void setStatusIncomplete() {
        assertStatus(EnumSet.of(StatusCode.READY));
        this.status = StatusCode.INCOMPLETE;
    }

    public void setStatusReady() {
        assertStatus(EnumSet.of(StatusCode.INCOMPLETE));
        this.status = StatusCode.READY;
    }

    public void setStatusLocked() {
        assertStatus(EnumSet.of(StatusCode.READY));
        this.status = StatusCode.LOCKED;
    }

    public StatusCode getStatus() {
        return status;
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

    public HuntingClub getClub() {
        return club;
    }

    public void setClub(final HuntingClub club) {
        this.club = club;
    }

    public GISZone getZone() {
        return zone;
    }

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
