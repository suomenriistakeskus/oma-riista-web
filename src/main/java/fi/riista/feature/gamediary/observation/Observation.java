package fi.riista.feature.gamediary.observation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.observation.metadata.CanIdentifyObservationContextSensitiveFields;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay_;
import fi.riista.feature.huntingclub.hunting.rejection.ObservationRejection;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.Person_;
import fi.riista.util.F;
import fi.riista.util.jpa.CriteriaUtils;
import javaslang.Tuple;
import org.hibernate.validator.constraints.Range;
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
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Entity
@Table(name = "game_observation")
@Access(AccessType.FIELD)
public class Observation extends GameDiaryEntry implements CanIdentifyObservationContextSensitiveFields {

    public static final int MIN_AMOUNT = 1;
    public static final int MAX_AMOUNT = 999;

    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObservationType observationType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person observer;

    @Range(min = MIN_AMOUNT, max = MAX_AMOUNT)
    @Column
    private Integer amount;

    @Column
    private Boolean withinMooseHunting;

    @Range(min = 0, max = 100)
    @Column
    private Integer mooselikeMaleAmount;

    @Range(min = 0, max = 100)
    @Column
    private Integer mooselikeFemaleAmount;

    /**
     * Amount of groups of one adult female moose with one calf within one game
     * observation (event).
     */
    @Range(min = 0, max = 50)
    @Column(name = "mooselike_female_1_calf_amount")
    private Integer mooselikeFemale1CalfAmount;

    /**
     * Amount of groups of one adult female moose with two calfs within one game
     * observation (event).
     */
    @Range(min = 0, max = 50)
    @Column(name = "mooselike_female_2_calfs_amount")
    private Integer mooselikeFemale2CalfsAmount;

    /**
     * Amount of groups of one adult female moose with three calfs within one
     * game observation (event).
     */
    @Range(min = 0, max = 50)
    @Column(name = "mooselike_female_3_calfs_amount")
    private Integer mooselikeFemale3CalfsAmount;

    /**
     * Amount of groups of one adult female moose with four calfs within one
     * game observation (event).
     */
    @Range(min = 0, max = 50)
    @Column(name = "mooselike_female_4_calfs_amount")
    private Integer mooselikeFemale4CalfsAmount;

    @Range(min = 0, max = 100)
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

    public Observation(
            final Person author,
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

    @Override
    public boolean observedWithinMooseHunting() {
        return Optional.ofNullable(withinMooseHunting).orElse(false);
    }

    public boolean isAmountEqualTo(final Integer amount) {
        return Objects.equals(this.amount, amount);
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

    @AssertTrue
    public boolean isMooselikeAmountPresenceConsistent() {
        return !isAnyMooselikeAmountPresent() || hasMinimumSetOfNonnullAmountsCommonToAllMooselikeSpecies();
    }

    public boolean isAnyMooselikeAmountPresent() {
        return F.anyNonNull(
                mooselikeMaleAmount, mooselikeFemaleAmount, mooselikeFemale1CalfAmount,
                mooselikeFemale2CalfsAmount, mooselikeFemale3CalfsAmount, mooselikeFemale4CalfsAmount,
                mooselikeUnknownSpecimenAmount);
    }

    public Observation withMooselikeAmounts(
            final Integer mooselikeMaleAmount,
            final Integer mooselikeFemaleAmount,
            final Integer mooselikeFemale1CalfAmount,
            final Integer mooselikeFemale2CalfsAmount,
            final Integer mooselikeFemale3CalfsAmount,
            final Integer mooselikeFemale4CalfsAmount,
            final Integer mooselikeUnknownSpecimenAmount) {

        setMooselikeMaleAmount(mooselikeMaleAmount);
        setMooselikeFemaleAmount(mooselikeFemaleAmount);
        setMooselikeFemale1CalfAmount(mooselikeFemale1CalfAmount);
        setMooselikeFemale2CalfsAmount(mooselikeFemale2CalfsAmount);
        setMooselikeFemale3CalfsAmount(mooselikeFemale3CalfsAmount);
        setMooselikeFemale4CalfsAmount(mooselikeFemale4CalfsAmount);
        setMooselikeUnknownSpecimenAmount(mooselikeUnknownSpecimenAmount);
        return this;
    }

    private void updateAmountToSumOfAllMooselikeAmountsIfPresent() {
        if (hasMinimumSetOfNonnullAmountsCommonToAllMooselikeSpecies()) {
            setAmount(getTotalAmountOfMooselikeAmountFields());
        }
    }

    private boolean hasMinimumSetOfNonnullAmountsCommonToAllMooselikeSpecies() {
        // The list below includes all mandatory amount fields for moose. Other moose-like species
        // include these as well plus female-with4-calfs-amount.
        return F.allNotNull(
                mooselikeMaleAmount, mooselikeFemaleAmount, mooselikeFemale1CalfAmount,
                mooselikeFemale2CalfsAmount, mooselikeFemale3CalfsAmount, mooselikeUnknownSpecimenAmount);
    }

    private int getTotalAmountOfMooselikeAmountFields() {
        return Stream.of(
                Tuple.of(1, mooselikeMaleAmount),
                Tuple.of(1, mooselikeFemaleAmount),
                Tuple.of(2, mooselikeFemale1CalfAmount),
                Tuple.of(3, mooselikeFemale2CalfsAmount),
                Tuple.of(4, mooselikeFemale3CalfsAmount),
                Tuple.of(5, mooselikeFemale4CalfsAmount),
                Tuple.of(1, mooselikeUnknownSpecimenAmount))
                .filter(pair -> pair._2() != null)
                .mapToInt(pair -> pair._1() * pair._2())
                .sum();
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

    @Override
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

    public void setWithinMooseHunting(final Boolean withinMooseHunting) {
        this.withinMooseHunting = withinMooseHunting;
    }

    public Integer getMooselikeMaleAmount() {
        return mooselikeMaleAmount;
    }

    public void setMooselikeMaleAmount(final Integer mooselikeMaleAmount) {
        this.mooselikeMaleAmount = mooselikeMaleAmount;
        updateAmountToSumOfAllMooselikeAmountsIfPresent();
    }

    public Integer getMooselikeFemaleAmount() {
        return mooselikeFemaleAmount;
    }

    public void setMooselikeFemaleAmount(final Integer mooselikeFemaleAmount) {
        this.mooselikeFemaleAmount = mooselikeFemaleAmount;
        updateAmountToSumOfAllMooselikeAmountsIfPresent();
    }

    public Integer getMooselikeFemale1CalfAmount() {
        return mooselikeFemale1CalfAmount;
    }

    public void setMooselikeFemale1CalfAmount(final Integer mooselikeFemale1CalfAmount) {
        this.mooselikeFemale1CalfAmount = mooselikeFemale1CalfAmount;
        updateAmountToSumOfAllMooselikeAmountsIfPresent();
    }

    public Integer getMooselikeFemale2CalfsAmount() {
        return mooselikeFemale2CalfsAmount;
    }

    public void setMooselikeFemale2CalfsAmount(final Integer mooselikeFemale2CalfsAmount) {
        this.mooselikeFemale2CalfsAmount = mooselikeFemale2CalfsAmount;
        updateAmountToSumOfAllMooselikeAmountsIfPresent();
    }

    public Integer getMooselikeFemale3CalfsAmount() {
        return mooselikeFemale3CalfsAmount;
    }

    public void setMooselikeFemale3CalfsAmount(final Integer mooselikeFemale3CalfsAmount) {
        this.mooselikeFemale3CalfsAmount = mooselikeFemale3CalfsAmount;
        updateAmountToSumOfAllMooselikeAmountsIfPresent();
    }

    public Integer getMooselikeFemale4CalfsAmount() {
        return mooselikeFemale4CalfsAmount;
    }

    public void setMooselikeFemale4CalfsAmount(final Integer mooselikeFemale4CalfsAmount) {
        this.mooselikeFemale4CalfsAmount = mooselikeFemale4CalfsAmount;
        updateAmountToSumOfAllMooselikeAmountsIfPresent();
    }

    public Integer getMooselikeUnknownSpecimenAmount() {
        return mooselikeUnknownSpecimenAmount;
    }

    public void setMooselikeUnknownSpecimenAmount(final Integer mooselikeUnknownSpecimenAmount) {
        this.mooselikeUnknownSpecimenAmount = mooselikeUnknownSpecimenAmount;
        updateAmountToSumOfAllMooselikeAmountsIfPresent();
    }

    public boolean isFromMobile() {
        return fromMobile;
    }

    public void setFromMobile(final boolean fromMobile) {
        this.fromMobile = fromMobile;
    }

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
