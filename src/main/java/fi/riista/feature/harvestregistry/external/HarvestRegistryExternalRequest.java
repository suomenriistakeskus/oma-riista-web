package fi.riista.feature.harvestregistry.external;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.validation.FinnishHunterNumber;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Access(value = AccessType.FIELD)
public class HarvestRegistryExternalRequest extends LifecycleEntity<Long> {

    private static final String ID_COLUMN_NAME = "harvest_registry_external_request_id";

    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private HarvestRegistryExternalRequestReason reason;

    @Size(max = 255)
    @Column
    private String remoteUser;

    @Size(max = 255)
    @Column
    private String remoteAddress;

    @NotNull
    @Column(nullable = false)
    private LocalDate beginDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;

    @NotNull
    @Column(nullable = false)
    private Boolean allSpecies;

    @OneToMany(mappedBy = "request")
    private Set<HarvestRegistryExternalRequestSpecies> species = new HashSet<>();

    @Pattern(regexp = "^\\d{3}$")
    @Column(length = 3)
    private String municipalityCode;

    @Pattern(regexp = "^\\d{3}$")
    @Column(length = 3)
    private String rkaCode;

    @Pattern(regexp = "^\\d{3}$")
    @Column(length = 3)
    private String rhyCode;

    @FinnishHunterNumber
    @Column
    private String shooterHunterNumber;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer page;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer pageSize;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HarvestRegistryExternalRequestReason getReason() {
        return reason;
    }

    public void setReason(final HarvestRegistryExternalRequestReason reason) {
        this.reason = reason;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(final String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(final String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public Set<HarvestRegistryExternalRequestSpecies> getSpecies() {
        return species;
    }

    public void setSpecies(final Set<HarvestRegistryExternalRequestSpecies> species) {
        this.species = species;
    }

    public Boolean getAllSpecies() {
        return allSpecies;
    }

    public void setAllSpecies(final Boolean allSpecies) {
        this.allSpecies = allSpecies;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(final String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public String getRkaCode() {
        return rkaCode;
    }

    public void setRkaCode(final String rkaCode) {
        this.rkaCode = rkaCode;
    }

    public String getRhyCode() {
        return rhyCode;
    }

    public void setRhyCode(final String rhyCode) {
        this.rhyCode = rhyCode;
    }

    public String getShooterHunterNumber() {
        return shooterHunterNumber;
    }

    public void setShooterHunterNumber(final String shooterHunterNumber) {
        this.shooterHunterNumber = shooterHunterNumber;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(final Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(final Integer pageSize) {
        this.pageSize = pageSize;
    }
}
