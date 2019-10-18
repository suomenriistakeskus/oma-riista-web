package fi.riista.feature.gamediary.observation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.observation.metadata.ObservationContext;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay_;
import fi.riista.feature.huntingclub.hunting.rejection.ObservationRejection;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.Person_;
import fi.riista.util.F;
import fi.riista.util.jpa.CriteriaUtils;
import fi.riista.validation.PhoneNumber;
import org.joda.time.LocalDateTime;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

import static fi.riista.util.F.coalesceAsInt;

@Entity
@Table(name = "game_observation")
@Access(AccessType.FIELD)
public class Observation extends GameDiaryEntry {

    public static final int MIN_AMOUNT = 1;
    public static final int MAX_AMOUNT = 9999;

    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObservationType observationType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person observer;

    @Min(MIN_AMOUNT)
    @Max(MAX_AMOUNT)
    @Column
    private Integer amount;

    @Column
    private Boolean withinMooseHunting;

    /**
     * Etäisyys asumuksesta (<= 100 m)
     */
    @Min(0)
    @Max(100)
    @Column
    private Integer inYardDistanceToResidence;

    /**
     * Petoyhdyshenkilön maastossa varmentama
     */
    @Column
    private Boolean verifiedByCarnivoreAuthority;

    /**
     * Havainnontekijän nimi
     */
    @Size(max = 255)
    @Column
    private String observerName;

    /**
     * Havainnontekijän puhelinnumero
     */
    @PhoneNumber
    @Column
    private String observerPhoneNumber;

    /**
     * Lisätieto suurpedoista Lukelle
     */
    @Column(columnDefinition = "text")
    private String officialAdditionalInfo;

    @Min(0)
    @Max(100)
    @Column
    private Integer mooselikeMaleAmount;

    @Min(0)
    @Max(100)
    @Column
    private Integer mooselikeFemaleAmount;

    @Min(0)
    @Max(50)
    @Column
    private Integer mooselikeCalfAmount;

    /**
     * Amount of groups of one adult female moose with one calf within one game
     * observation (event).
     */
    @Min(0)
    @Max(50)
    @Column(name = "mooselike_female_1_calf_amount")
    private Integer mooselikeFemale1CalfAmount;

    /**
     * Amount of groups of one adult female moose with two calfs within one game
     * observation (event).
     */
    @Min(0)
    @Max(50)
    @Column(name = "mooselike_female_2_calfs_amount")
    private Integer mooselikeFemale2CalfsAmount;

    /**
     * Amount of groups of one adult female moose with three calfs within one
     * game observation (event).
     */
    @Min(0)
    @Max(50)
    @Column(name = "mooselike_female_3_calfs_amount")
    private Integer mooselikeFemale3CalfsAmount;

    /**
     * Amount of groups of one adult female moose with four calfs within one
     * game observation (event).
     */
    @Min(0)
    @Max(50)
    @Column(name = "mooselike_female_4_calfs_amount")
    private Integer mooselikeFemale4CalfsAmount;

    @Min(0)
    @Max(50)
    @Column
    private Integer mooselikeUnknownSpecimenAmount;

    @Column(nullable = false, insertable = true, updatable = false)
    private boolean fromMobile;

    @OneToMany(mappedBy = "observation")
    private Set<ObservationSpecimen> specimens = new HashSet<>();

    @OneToMany(mappedBy = "observation")
    private Set<GameDiaryImage> images = new HashSet<>();

    @OneToMany(mappedBy = "observation")
    private Set<ObservationRejection> groupRejections = new HashSet<>();

    public Observation() {
        super();
    }

    public Observation(final Person author,
                       final GeoLocation geoLocation,
                       final LocalDateTime pointOfTime,
                       final GameSpecies species,
                       final int amount) {

        super(geoLocation, pointOfTime, species, author);

        setObserver(author);
        this.amount = amount;
    }

    @Override
    public GameDiaryEntryType getType() {
        return GameDiaryEntryType.OBSERVATION;
    }

    @Override
    public Person getActor() {
        return getObserver();
    }

    @Override
    public void setActor(Person person) {
        setObserver(person);
    }

    public boolean observedWithinMooseHunting() {
        return Boolean.TRUE.equals(withinMooseHunting);
    }

    public ObservationContext getObservationContext() {
        return new ObservationContext(
                ObservationSpecVersion.MOST_RECENT,
                getSpecies().getOfficialCode(),
                observedWithinMooseHunting(),
                observationType);
    }

    @Override
    protected void updateAuthorInverseCollection(final Person newAuthor) {
        CriteriaUtils.updateInverseCollection(Person_.authoredObservations, this, this.author, newAuthor);
    }

    @Override
    protected void updateHuntingDayOfGroupInverseCollection(final GroupHuntingDay newHuntingDay) {
        CriteriaUtils
                .updateInverseCollection(GroupHuntingDay_.observations, this, this.huntingDayOfGroup, newHuntingDay);
    }

    public boolean isAnyLargeCarnivoreFieldPresent() {
        // XXX: Will need to include inYardDistanceToResidence when large carnivore fields are
        // enabled in production.
        return F.anyNonNull(verifiedByCarnivoreAuthority, observerName, observerPhoneNumber, officialAdditionalInfo);
    }

    @AssertTrue
    public boolean isMooselikeAmountPresenceConsistent() {
        return !isAnyMooselikeAmountPresent() || hasMinimumSetOfNonnullAmountsCommonToAllMooselikeSpecies();
    }

    public boolean isAnyMooselikeAmountPresent() {
        return F.anyNonNull(
                mooselikeMaleAmount, mooselikeFemaleAmount, mooselikeCalfAmount, mooselikeFemale1CalfAmount,
                mooselikeFemale2CalfsAmount, mooselikeFemale3CalfsAmount, mooselikeFemale4CalfsAmount,
                mooselikeUnknownSpecimenAmount);
    }

    public boolean hasMinimumSetOfNonnullAmountsCommonToAllMooselikeSpecies() {
        // The list below includes all mandatory amount fields for moose. Other moose-like species
        // include these as well plus female-with4-calfs-amount.
        return F.allNotNull(
                mooselikeMaleAmount, mooselikeFemaleAmount, mooselikeFemale1CalfAmount, mooselikeFemale2CalfsAmount,
                mooselikeFemale3CalfsAmount, mooselikeUnknownSpecimenAmount);
    }

    public int getSumOfMooselikeAmounts() {
        return coalesceAsInt(mooselikeMaleAmount, 0)
                + coalesceAsInt(mooselikeFemaleAmount, 0)
                + coalesceAsInt(mooselikeCalfAmount, 0)
                + 2 * coalesceAsInt(mooselikeFemale1CalfAmount, 0)
                + 3 * coalesceAsInt(mooselikeFemale2CalfsAmount, 0)
                + 4 * coalesceAsInt(mooselikeFemale3CalfsAmount, 0)
                + 5 * coalesceAsInt(mooselikeFemale4CalfsAmount, 0)
                + coalesceAsInt(mooselikeUnknownSpecimenAmount, 0);
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_observation_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Person getObserver() {
        return observer;
    }

    public ObservationType getObservationType() {
        return observationType;
    }

    public void setObservationType(final ObservationType observationType) {
        this.observationType = observationType;
    }

    public void setObserver(final Person observer) {
        CriteriaUtils.updateInverseCollection(Person_.actualObservations, this, this.observer, observer);
        this.observer = observer;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(final Integer amount) {
        this.amount = amount;
    }

    public Boolean getWithinMooseHunting() {
        return withinMooseHunting;
    }

    public Integer getInYardDistanceToResidence() {
        return inYardDistanceToResidence;
    }

    public void setInYardDistanceToResidence(final Integer inYardDistanceToResidence) {
        this.inYardDistanceToResidence = inYardDistanceToResidence;
    }

    public void setWithinMooseHunting(final Boolean withinMooseHunting) {
        this.withinMooseHunting = withinMooseHunting;
    }

    public Boolean getVerifiedByCarnivoreAuthority() {
        return verifiedByCarnivoreAuthority;
    }

    public void setVerifiedByCarnivoreAuthority(final Boolean verifiedByCarnivoreAuthority) {
        this.verifiedByCarnivoreAuthority = verifiedByCarnivoreAuthority;
    }

    public String getObserverName() {
        return observerName;
    }

    public void setObserverName(final String observerName) {
        this.observerName = observerName;
    }

    public String getObserverPhoneNumber() {
        return observerPhoneNumber;
    }

    public void setObserverPhoneNumber(final String observerPhoneNumber) {
        this.observerPhoneNumber = observerPhoneNumber;
    }

    public String getOfficialAdditionalInfo() {
        return officialAdditionalInfo;
    }

    public void setOfficialAdditionalInfo(final String officialAdditionalInfo) {
        this.officialAdditionalInfo = officialAdditionalInfo;
    }

    public Integer getMooselikeMaleAmount() {
        return mooselikeMaleAmount;
    }

    public void setMooselikeMaleAmount(final Integer mooselikeMaleAmount) {
        this.mooselikeMaleAmount = mooselikeMaleAmount;
    }

    public Integer getMooselikeFemaleAmount() {
        return mooselikeFemaleAmount;
    }

    public void setMooselikeFemaleAmount(final Integer mooselikeFemaleAmount) {
        this.mooselikeFemaleAmount = mooselikeFemaleAmount;
    }

    public Integer getMooselikeCalfAmount() {
        return mooselikeCalfAmount;
    }

    public void setMooselikeCalfAmount(final Integer mooselikeCalfAmount) {
        this.mooselikeCalfAmount = mooselikeCalfAmount;
    }

    public Integer getMooselikeFemale1CalfAmount() {
        return mooselikeFemale1CalfAmount;
    }

    public void setMooselikeFemale1CalfAmount(final Integer mooselikeFemale1CalfAmount) {
        this.mooselikeFemale1CalfAmount = mooselikeFemale1CalfAmount;
    }

    public Integer getMooselikeFemale2CalfsAmount() {
        return mooselikeFemale2CalfsAmount;
    }

    public void setMooselikeFemale2CalfsAmount(final Integer mooselikeFemale2CalfsAmount) {
        this.mooselikeFemale2CalfsAmount = mooselikeFemale2CalfsAmount;
    }

    public Integer getMooselikeFemale3CalfsAmount() {
        return mooselikeFemale3CalfsAmount;
    }

    public void setMooselikeFemale3CalfsAmount(final Integer mooselikeFemale3CalfsAmount) {
        this.mooselikeFemale3CalfsAmount = mooselikeFemale3CalfsAmount;
    }

    public Integer getMooselikeFemale4CalfsAmount() {
        return mooselikeFemale4CalfsAmount;
    }

    public void setMooselikeFemale4CalfsAmount(final Integer mooselikeFemale4CalfsAmount) {
        this.mooselikeFemale4CalfsAmount = mooselikeFemale4CalfsAmount;
    }

    public Integer getMooselikeUnknownSpecimenAmount() {
        return mooselikeUnknownSpecimenAmount;
    }

    public void setMooselikeUnknownSpecimenAmount(final Integer mooselikeUnknownSpecimenAmount) {
        this.mooselikeUnknownSpecimenAmount = mooselikeUnknownSpecimenAmount;
    }

    public boolean isFromMobile() {
        return fromMobile;
    }

    public void setFromMobile(final boolean fromMobile) {
        this.fromMobile = fromMobile;
    }

    // Following collection getters exposed in package-private scope only for property introspection.

    Set<ObservationSpecimen> getSpecimens() {
        return specimens;
    }

    Set<GameDiaryImage> getImages() {
        return images;
    }

    Set<ObservationRejection> getGroupRejections() {
        return groupRejections;
    }
}
