package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpecies_;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameAge;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.CriteriaUtils;
import org.hibernate.annotations.OptimisticLock;
import org.joda.time.DateTime;

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
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static fi.riista.feature.common.entity.Required.NO;
import static fi.riista.feature.common.entity.Required.VOLUNTARY;

@Entity
@Access(AccessType.FIELD)
public class ObservationContextSensitiveFields extends BaseEntity<Long> {

    private Long id;

    @Min(1)
    @Column(nullable = false)
    private int metadataVersion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_species_id", nullable = false)
    private GameSpecies species;

    /**
     * Ilmaisee, onko havaintokonteksti sidottu hirvenmetsästykseen.
     */
    @Column(nullable = false)
    private boolean withinMooseHunting;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObservationType observationType;

    @NotNull
    @Column(nullable = false, insertable = true, updatable = false)
    private DateTime creationTime;

    // Trigger-updated in production DB
    @OptimisticLock(excluded = true)
    @NotNull
    @Column(nullable = false)
    private DateTime modificationTime;

    /**
     * Eläinyksilöiden kappalemäärä (havaintokirjaus)
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private DynamicObservationFieldPresence amount = DynamicObservationFieldPresence.NO;

    /**
     * Petoyhdyshenkilön maastossa varmentama (havaintokirjaus)
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private DynamicObservationFieldPresence verifiedByCarnivoreAuthority = DynamicObservationFieldPresence.NO;

    /**
     * Petohavainnontekijän nimi (havaintokirjaus)
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private DynamicObservationFieldPresence observerName = DynamicObservationFieldPresence.NO;

    /**
     * Petohavainnontekijän puhelinnumero (havaintokirjaus)
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private DynamicObservationFieldPresence observerPhoneNumber = DynamicObservationFieldPresence.NO;

    /**
     * Lisätieto suurpedoista Lukelle (havaintokirjaus)
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private DynamicObservationFieldPresence officialAdditionalInfo = DynamicObservationFieldPresence.NO;

    /**
     * Eläinyksilön ikäluokka
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required age = NO;

    /**
     * Ilmaisee, onko "1-2-vuotias" käytössä
     */
    @Column(nullable = false)
    private boolean extendedAgeRange;

    /**
     * Eläinyksilön sukupuoli
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required gender = NO;

    /**
     * Eläinyksilön tila: loukkaantunut
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required wounded = NO;

    /**
     * Eläinyksilön tila: kuollut
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required dead = NO;

    /**
     * Eläinyksilön tila: haaskalla
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required onCarcass = NO;

    /**
     * Eläinyksilön merkintä: panta / radiolähetin
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "collar_or_radio", nullable = false)
    private Required collarOrRadioTransmitter = NO;

    /**
     * Eläinyksilön merkintä: jalkarengas / siipimerkki
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "legring_or_wingmark", nullable = false)
    private Required legRingOrWingMark = NO;

    /**
     * Eläinyksilön merkintä: korvamerkki
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "earmark", nullable = false)
    private Required earMark = NO;

    /**
     * Eläinyksilön etutassun leveys
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private DynamicObservationFieldPresence widthOfPaw = DynamicObservationFieldPresence.NO;

    /**
     * Eläinyksilön etutassun pituus
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private DynamicObservationFieldPresence lengthOfPaw = DynamicObservationFieldPresence.NO;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required mooselikeMaleAmount = NO;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required mooselikeFemaleAmount = NO;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required mooselikeCalfAmount = NO;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "mooselike_female_1_calf_amount", nullable = false)
    private Required mooselikeFemale1CalfAmount = NO;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "mooselike_female_2_calfs_amount", nullable = false)
    private Required mooselikeFemale2CalfsAmount = NO;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "mooselike_female_3_calfs_amount", nullable = false)
    private Required mooselikeFemale3CalfsAmount = NO;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "mooselike_female_4_calfs_amount", nullable = false)
    private Required mooselikeFemale4CalfsAmount = NO;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required mooselikeUnknownSpecimenAmount = NO;

    // Public default constructor needed for tests.
    public ObservationContextSensitiveFields() {
    }

    public ObservationContextSensitiveFields(final GameSpecies species,
                                             final boolean withinMooseHunting,
                                             final ObservationType observationType,
                                             final int metadataVersion) {

        this(species, withinMooseHunting, observationType, metadataVersion, DateUtil.now());
    }

    public ObservationContextSensitiveFields(final GameSpecies species,
                                             final boolean withinMooseHunting,
                                             final ObservationType observationType,
                                             final int metadataVersion,
                                             final DateTime creationTime) {

        setSpecies(species);
        this.withinMooseHunting = withinMooseHunting;
        this.observationType = observationType;
        this.metadataVersion = metadataVersion;
        this.creationTime = creationTime;
        this.modificationTime = creationTime;
    }

    @AssertTrue
    public boolean isExtendedAgeRangeValidForAgeRequirement() {
        return age != NO || !extendedAgeRange;
    }

    public EnumSet<ObservedGameAge> getAllowedGameAges() {
        if (age.nullValueRequired()) {
            return EnumSet.noneOf(ObservedGameAge.class);
        }

        return extendedAgeRange
                ? EnumSet.allOf(ObservedGameAge.class)
                : EnumSet.complementOf(EnumSet.of(ObservedGameAge._1TO2Y));
    }

    public EnumSet<ObservedGameState> getAllowedGameStates() {
        final List<ObservedGameState> states = new ArrayList<>(5);

        if (wounded.isNonNullValueLegal()) {
            states.add(ObservedGameState.WOUNDED);
        }
        if (dead.isNonNullValueLegal()) {
            states.add(ObservedGameState.DEAD);
        }
        if (onCarcass.isNonNullValueLegal()) {
            states.add(ObservedGameState.CARCASS);
        }

        if (!states.isEmpty()) {
            Stream.of(ObservedGameState.HEALTHY, ObservedGameState.ILL).forEach(states::add);
            return EnumSet.copyOf(states);
        }

        return EnumSet.noneOf(ObservedGameState.class);
    }

    public EnumSet<GameMarking> getAllowedGameMarkings() {
        final List<GameMarking> markings = new ArrayList<>(4);

        if (collarOrRadioTransmitter.isNonNullValueLegal()) {
            markings.add(GameMarking.COLLAR_OR_RADIO_TRANSMITTER);
        }
        if (legRingOrWingMark.isNonNullValueLegal()) {
            markings.add(GameMarking.LEG_RING_OR_WING_TAG);
        }
        if (earMark.isNonNullValueLegal()) {
            markings.add(GameMarking.EARMARK);
        }

        if (!markings.isEmpty()) {
            markings.add(GameMarking.NOT_MARKED);
            return EnumSet.copyOf(markings);
        }

        return EnumSet.noneOf(GameMarking.class);
    }

    public Map<ObservedGameState, Required> getValidGameStateRequirements() {
        final Map<ObservedGameState, Required> states = new HashMap<>();

        if (wounded.isNonNullValueLegal()) {
            states.put(ObservedGameState.WOUNDED, wounded);
        }

        if (onCarcass.isNonNullValueLegal()) {
            states.put(ObservedGameState.CARCASS, onCarcass);
        }

        if (dead.isNonNullValueLegal()) {
            states.put(ObservedGameState.DEAD, dead);
        }

        // HEALTHY and ILL are valid only if at least one of WOUNDED, CARCASS or DEAD is voluntary/required.
        if (!states.isEmpty()) {
            Stream.of(ObservedGameState.HEALTHY, ObservedGameState.ILL).forEach(v -> states.put(v, VOLUNTARY));
        }

        return states;
    }

    public Map<GameMarking, Required> getValidGameMarkingRequirements() {
        final Map<GameMarking, Required> markings = new HashMap<>();

        if (collarOrRadioTransmitter.isNonNullValueLegal()) {
            markings.put(GameMarking.COLLAR_OR_RADIO_TRANSMITTER, collarOrRadioTransmitter);
        }

        if (legRingOrWingMark.isNonNullValueLegal()) {
            markings.put(GameMarking.LEG_RING_OR_WING_TAG, legRingOrWingMark);
        }

        if (earMark.isNonNullValueLegal()) {
            markings.put(GameMarking.EARMARK, earMark);
        }

        // NOT_MARKED is valid only if at least one of the previous marking methods is voluntary/required.
        if (!markings.isEmpty()) {
            markings.put(GameMarking.NOT_MARKED, VOLUNTARY);
        }

        return markings;
    }

    public void copyRequirementsTo(final ObservationContextSensitiveFields that) {
        that.amount = this.amount;

        that.age = this.age;
        that.extendedAgeRange = this.extendedAgeRange;
        that.gender = this.gender;

        that.onCarcass = this.onCarcass;
        that.wounded = this.wounded;
        that.dead = this.dead;

        that.earMark = this.earMark;
        that.collarOrRadioTransmitter = this.collarOrRadioTransmitter;
        that.legRingOrWingMark = this.legRingOrWingMark;

        that.mooselikeMaleAmount = this.mooselikeMaleAmount;
        that.mooselikeFemaleAmount = this.mooselikeFemaleAmount;
        that.mooselikeCalfAmount = this.mooselikeCalfAmount;
        that.mooselikeFemale1CalfAmount = this.mooselikeFemale1CalfAmount;
        that.mooselikeFemale2CalfsAmount = this.mooselikeFemale2CalfsAmount;
        that.mooselikeFemale3CalfsAmount = this.mooselikeFemale3CalfsAmount;
        that.mooselikeFemale4CalfsAmount = this.mooselikeFemale4CalfsAmount;
        that.mooselikeUnknownSpecimenAmount = this.mooselikeUnknownSpecimenAmount;

        that.verifiedByCarnivoreAuthority = this.verifiedByCarnivoreAuthority;
        that.observerName = this.observerName;
        that.observerPhoneNumber = this.observerPhoneNumber;
        that.officialAdditionalInfo = this.officialAdditionalInfo;
        that.widthOfPaw = this.widthOfPaw;
        that.lengthOfPaw = this.lengthOfPaw;
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public int getMetadataVersion() {
        return metadataVersion;
    }

    public void setMetadataVersion(final int metadataVersion) {
        this.metadataVersion = metadataVersion;
    }

    public GameSpecies getSpecies() {
        return species;
    }

    public void setSpecies(final GameSpecies species) {
        CriteriaUtils.updateInverseCollection(
                GameSpecies_.observationContextSensitiveFields, this, this.species, species);
        this.species = species;
    }

    public boolean isWithinMooseHunting() {
        return withinMooseHunting;
    }

    public void setWithinMooseHunting(final boolean withinMooseHunting) {
        this.withinMooseHunting = withinMooseHunting;
    }

    public ObservationType getObservationType() {
        return observationType;
    }

    public void setObservationType(final ObservationType observationType) {
        this.observationType = observationType;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public DateTime getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(final DateTime modificationTime) {
        this.modificationTime = modificationTime;
    }

    public DynamicObservationFieldPresence getAmount() {
        return amount;
    }

    public void setAmount(final DynamicObservationFieldPresence amount) {
        this.amount = amount;
    }

    public DynamicObservationFieldPresence getVerifiedByCarnivoreAuthority() {
        return verifiedByCarnivoreAuthority;
    }

    public void setVerifiedByCarnivoreAuthority(final DynamicObservationFieldPresence verifiedByCarnivoreAuthority) {
        this.verifiedByCarnivoreAuthority = verifiedByCarnivoreAuthority;
    }

    public DynamicObservationFieldPresence getObserverName() {
        return observerName;
    }

    public void setObserverName(final DynamicObservationFieldPresence observerName) {
        this.observerName = observerName;
    }

    public DynamicObservationFieldPresence getObserverPhoneNumber() {
        return observerPhoneNumber;
    }

    public void setObserverPhoneNumber(final DynamicObservationFieldPresence observerPhoneNumber) {
        this.observerPhoneNumber = observerPhoneNumber;
    }

    public DynamicObservationFieldPresence getOfficialAdditionalInfo() {
        return officialAdditionalInfo;
    }

    public void setOfficialAdditionalInfo(final DynamicObservationFieldPresence officialAdditionalInfo) {
        this.officialAdditionalInfo = officialAdditionalInfo;
    }

    public Required getAge() {
        return age;
    }

    public void setAge(final Required age) {
        this.age = age;
    }

    public boolean isExtendedAgeRange() {
        return extendedAgeRange;
    }

    public void setExtendedAgeRange(final boolean extendedAgeRange) {
        this.extendedAgeRange = extendedAgeRange;
    }

    public Required getGender() {
        return gender;
    }

    public void setGender(final Required gender) {
        this.gender = gender;
    }

    public Required getWounded() {
        return wounded;
    }

    public void setWounded(final Required wounded) {
        this.wounded = wounded;
    }

    public Required getDead() {
        return dead;
    }

    public void setDead(final Required dead) {
        this.dead = dead;
    }

    public Required getOnCarcass() {
        return onCarcass;
    }

    public void setOnCarcass(final Required onCarcass) {
        this.onCarcass = onCarcass;
    }

    public Required getCollarOrRadioTransmitter() {
        return collarOrRadioTransmitter;
    }

    public void setCollarOrRadioTransmitter(final Required collarOrRadioTransmitter) {
        this.collarOrRadioTransmitter = collarOrRadioTransmitter;
    }

    public Required getLegRingOrWingMark() {
        return legRingOrWingMark;
    }

    public void setLegRingOrWingMark(final Required legRingOrWingMark) {
        this.legRingOrWingMark = legRingOrWingMark;
    }

    public Required getEarMark() {
        return earMark;
    }

    public void setEarMark(final Required earMark) {
        this.earMark = earMark;
    }

    public DynamicObservationFieldPresence getWidthOfPaw() {
        return widthOfPaw;
    }

    public void setWidthOfPaw(final DynamicObservationFieldPresence widthOfPaw) {
        this.widthOfPaw = widthOfPaw;
    }

    public DynamicObservationFieldPresence getLengthOfPaw() {
        return lengthOfPaw;
    }

    public void setLengthOfPaw(final DynamicObservationFieldPresence lengthOfPaw) {
        this.lengthOfPaw = lengthOfPaw;
    }

    public Required getMooselikeMaleAmount() {
        return mooselikeMaleAmount;
    }

    public void setMooselikeMaleAmount(final Required mooselikeMaleAmount) {
        this.mooselikeMaleAmount = mooselikeMaleAmount;
    }

    public Required getMooselikeFemaleAmount() {
        return mooselikeFemaleAmount;
    }

    public void setMooselikeFemaleAmount(final Required mooselikeFemaleAmount) {
        this.mooselikeFemaleAmount = mooselikeFemaleAmount;
    }

    public Required getMooselikeCalfAmount() {
        return mooselikeCalfAmount;
    }

    public void setMooselikeCalfAmount(final Required mooselikeCalfAmount) {
        this.mooselikeCalfAmount = mooselikeCalfAmount;
    }

    public Required getMooselikeFemale1CalfAmount() {
        return mooselikeFemale1CalfAmount;
    }

    public void setMooselikeFemale1CalfAmount(final Required mooselikeFemale1CalfAmount) {
        this.mooselikeFemale1CalfAmount = mooselikeFemale1CalfAmount;
    }

    public Required getMooselikeFemale2CalfsAmount() {
        return mooselikeFemale2CalfsAmount;
    }

    public void setMooselikeFemale2CalfsAmount(final Required mooselikeFemale2CalfsAmount) {
        this.mooselikeFemale2CalfsAmount = mooselikeFemale2CalfsAmount;
    }

    public Required getMooselikeFemale3CalfsAmount() {
        return mooselikeFemale3CalfsAmount;
    }

    public void setMooselikeFemale3CalfsAmount(final Required mooselikeFemale3CalfsAmount) {
        this.mooselikeFemale3CalfsAmount = mooselikeFemale3CalfsAmount;
    }

    public Required getMooselikeFemale4CalfsAmount() {
        return mooselikeFemale4CalfsAmount;
    }

    public void setMooselikeFemale4CalfsAmount(final Required mooselikeFemale4CalfsAmount) {
        this.mooselikeFemale4CalfsAmount = mooselikeFemale4CalfsAmount;
    }

    public Required getMooselikeUnknownSpecimenAmount() {
        return mooselikeUnknownSpecimenAmount;
    }

    public void setMooselikeUnknownSpecimenAmount(final Required mooselikeUnknownSpecimenAmount) {
        this.mooselikeUnknownSpecimenAmount = mooselikeUnknownSpecimenAmount;
    }

}
