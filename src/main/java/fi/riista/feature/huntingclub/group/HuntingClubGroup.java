package fi.riista.feature.huntingclub.group;

import com.google.common.base.Preconditions;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.rejection.HarvestRejection;
import fi.riista.feature.huntingclub.hunting.rejection.ObservationRejection;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImport;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.util.LocalisedString;
import fi.riista.util.jpa.CriteriaUtils;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Entity
@Access(value = AccessType.FIELD)
@DiscriminatorValue("CLUBGROUP")
public class HuntingClubGroup extends Organisation {

    private static final LocalisedString GROUP_NAME_PREFIX_RESERVED_FOR_MOOSE_DATA_CARD_IMPORT =
            LocalisedString.of("hirvitietokortti", "älg data kort", "moose data card");

    public static final int MIN_YEAR = 2000;
    public static final int MAX_YEAR = 2100;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_species_id", nullable = false)
    private GameSpecies species;

    @Min(MIN_YEAR)
    @Max(MAX_YEAR)
    @Column(nullable = false)
    private int huntingYear;

    @OneToMany(mappedBy = "group")
    private Set<GroupHuntingDay> huntingDays = new HashSet<>();

    @OneToMany(mappedBy = "group")
    private Set<HarvestRejection> harvestRejections = new HashSet<>();

    @OneToMany(mappedBy = "group")
    private Set<ObservationRejection> observationRejections = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private HuntingClubArea huntingArea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_permit_id")
    private HarvestPermit harvestPermit;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date harvestPermitModificationTime;

    @Column(insertable = true, updatable = false)
    private Boolean fromMooseDataCard = false;

    @OneToMany(mappedBy = "group")
    private final Set<MooseDataCardImport> mooseDataCardImports = new HashSet<>();

    public HuntingClubGroup() {
        super(OrganisationType.CLUBGROUP);
        initOfficialCode();
    }

    public HuntingClubGroup(HuntingClub club, String nameFI, String nameSV, GameSpecies species, int huntingYear) {
        this();

        this.parentOrganisation = Objects.requireNonNull(club, "club is null");
        this.species = Objects.requireNonNull(species, "species is null");
        this.huntingYear = huntingYear;

        setNameFinnish(Objects.requireNonNull(nameFI, "nameFI is null"));
        setNameSwedish(Objects.requireNonNull(nameSV, "nameSV is null"));

        Preconditions.checkArgument(
                !hasNameReservedForMooseDataCardGroups(),
                "Names reserved for groups imported from moose data cards must not be explicitly given");
    }

    public static boolean isNameReservedForMooseDataCardGroups(@Nonnull final String groupName) {
        Objects.requireNonNull(groupName);

        final String trimmedGroupName = groupName.replace(" ", "").toLowerCase();

        return GROUP_NAME_PREFIX_RESERVED_FOR_MOOSE_DATA_CARD_IMPORT.asStream()
                .map(reservedName -> reservedName.replace(" ", ""))
                .anyMatch(trimmedGroupName::startsWith);
    }

    public static LocalisedString generateNameForMooseDataCardGroup(
            @Nonnull final Function<String, String> namePrefixFn) {

        return GROUP_NAME_PREFIX_RESERVED_FOR_MOOSE_DATA_CARD_IMPORT.transform(Objects.requireNonNull(namePrefixFn));
    }

    public boolean hasNameReservedForMooseDataCardGroups() {
        return getNameLocalisation().asStream().anyMatch(HuntingClubGroup::isNameReservedForMooseDataCardGroups);
    }

    public Optional<String> findNameReservedForMooseDataCardGroups() {
        return getNameLocalisation().asStream()
                .filter(HuntingClubGroup::isNameReservedForMooseDataCardGroups)
                .findFirst();
    }

    private void initOfficialCode() {
        setOfficialCode(UUID.randomUUID().toString());
    }

    public boolean isFromMooseDataCard() {
        return Boolean.TRUE.equals(fromMooseDataCard);
    }

    public void updateHarvestPermit(final HarvestPermit newPermit) {
        if (!Objects.equals(this.harvestPermit, newPermit)) {
            // use joda time to enable wall time tweaking in tests
            setHarvestPermitModificationTime(new DateTime().toDate());
        }
        setHarvestPermit(newPermit);
    }

    // Accessors -->

    public GameSpecies getSpecies() {
        return species;
    }

    public void setSpecies(final GameSpecies species) {
        this.species = species;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public HuntingClubArea getHuntingArea() {
        return huntingArea;
    }

    public void setHuntingArea(final HuntingClubArea huntingArea) {
        this.huntingArea = huntingArea;
    }

    public HarvestPermit getHarvestPermit() {
        return harvestPermit;
    }

    // package private because when updating permit we want tp update modification time too
    void setHarvestPermit(final HarvestPermit harvestPermit) {
        CriteriaUtils.updateInverseCollection(HarvestPermit_.permitGroups, this, this.harvestPermit, harvestPermit);
        this.harvestPermit = harvestPermit;
    }

    public Date getHarvestPermitModificationTime() {
        return harvestPermitModificationTime;
    }

    // package private because when updating modification time we want tp update permit too
    void setHarvestPermitModificationTime(final Date harvestPermitModificationTime) {
        this.harvestPermitModificationTime = harvestPermitModificationTime;
    }

    public Boolean getFromMooseDataCard() {
        return fromMooseDataCard;
    }

    public void setFromMooseDataCard(final Boolean fromMooseDataCard) {
        this.fromMooseDataCard = fromMooseDataCard;
    }

    // Collection getters below are exposed in package-private scope only for property introspection purposes.

    Set<GroupHuntingDay> getHuntingDays() {
        return huntingDays;
    }

    Set<HarvestRejection> getHarvestRejections() {
        return harvestRejections;
    }

    Set<ObservationRejection> getObservationRejections() {
        return observationRejections;
    }

    Set<MooseDataCardImport> getMooseDataCardImports() {
        return mooseDataCardImports;
    }

}
