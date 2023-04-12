package fi.riista.feature.gamediary;

import com.google.common.collect.ImmutableSet;
import com.querydsl.core.annotations.QueryDelegate;
import fi.riista.feature.common.entity.HasOfficialCode;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields;
import fi.riista.util.LocalisedString;

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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Access(AccessType.FIELD)
public class GameSpecies extends LifecycleEntity<Long> implements HasOfficialCode {

    public static final String ID_COLUMN_NAME = "game_species_id";

    public static final int OFFICIAL_CODE_UNKNOWN = -1;

    public static final int OFFICIAL_CODE_CANADIAN_BEAVER = 48250;
    public static final int OFFICIAL_CODE_BEAR = 47348;
    public static final int OFFICIAL_CODE_EUROPEAN_BEAVER = 48251;
    public static final int OFFICIAL_CODE_LYNX = 46615;
    public static final int OFFICIAL_CODE_WOLF = 46549;
    public static final int OFFICIAL_CODE_WOLVERINE = 47212;

    public static final int OFFICIAL_CODE_MOOSE = 47503;

    // Kuusipeura
    public static final int OFFICIAL_CODE_FALLOW_DEER = 47484;

    // Metsäkauris
    public static final int OFFICIAL_CODE_ROE_DEER = 47507;

    // Valkohäntäpeura
    public static final int OFFICIAL_CODE_WHITE_TAILED_DEER = 47629;

    // Metsäpeura
    public static final int OFFICIAL_CODE_WILD_FOREST_REINDEER = 200556;

    // Saksanhirvi
    public static final int OFFICIAL_CODE_RED_DEER = 47476;

    // Japaninpeura
    public static final int OFFICIAL_CODE_SIKA_DEER = 47479;

    // Villisika
    public static final int OFFICIAL_CODE_WILD_BOAR = 47926;

    // Mufloni
    public static final int OFFICIAL_CODE_MUFFLON = 47774;

    // Metsähanhi
    public static final int OFFICIAL_CODE_BEAN_GOOSE = 26287;
    public static final int OFFICIAL_CODE_TAIGA_BEAN_GOOSE = 292022;
    public static final int OFFICIAL_CODE_TUNDRA_BEAN_GOOSE = 292023;

    // Hilleri
    public static final int OFFICIAL_CODE_EUROPEAN_POLECAT = 47240;

    // Halli
    public static final int OFFICIAL_CODE_GREY_SEAL = 47282;

    // Norppa
    public static final int OFFICIAL_CODE_RINGED_SEAL = 200555;

    // Peltopyy
    public static final int OFFICIAL_CODE_PARTRIDGE = 27048;

    // Minkki
    public static final int OFFICIAL_CODE_AMERICAN_MINK = 47243;

    // Pesukarhu
    public static final int OFFICIAL_CODE_RACCOON = 47329;

    // Piisami
    public static final int OFFICIAL_CODE_MUSKRAT = 48537;

    // Supikoira
    public static final int OFFICIAL_CODE_RACCOON_DOG = 46564;

    // Rämemajava
    public static final int OFFICIAL_CODE_NUTRIA = 50336;

    // Villikani
    public static final int OFFICIAL_CODE_RABBIT = 50114;

    // Metsäjänis
    public static final int OFFICIAL_CODE_MOUNTAIN_HARE = 50106;

    // Rusakko
    public static final int OFFICIAL_CODE_BROWN_HARE = 50386;

    //Orava
    public static final int OFFICIAL_CODE_RED_SQUIRREL = 48089;

    // Tarhattu naali
    public static final int OFFICIAL_CODE_BLUE_FOX = 46542;

    // Kettu
    public static final int OFFICIAL_CODE_RED_FOX = 46587;

    // Mäyrä
    public static final int OFFICIAL_CODE_BADGER = 47180;

    // Kärppä
    public static final int OFFICIAL_CODE_ERMINE = 47230;

    // Saukko
    public static final int OFFICIAL_CODE_OTTER = 47169;

    // Näätä
    public static final int OFFICIAL_CODE_PINE_MARTEN = 47223;

    // Kirjohylje
    public static final int OFFICIAL_CODE_HARBOUR_SEAL = 47305;

    // Haapana
    public static final int OFFICIAL_CODE_WIGEON = 26360;

    // Jouhisorsa
    public static final int OFFICIAL_CODE_PINTAIL = 26382;

    // Heinätavi
    public static final int OFFICIAL_CODE_GARGANEY = 26388;

    // Lapasorsa
    public static final int OFFICIAL_CODE_SHOVELER = 26394;

    // Punasotka
    public static final int OFFICIAL_CODE_POCHARD = 26407;

    // Tukkasotka
    public static final int OFFICIAL_CODE_TUFTED_DUCK = 26415;

    // Haahka
    public static final int OFFICIAL_CODE_COMMON_EIDER = 26419;

    // Alli
    public static final int OFFICIAL_CODE_LONG_TAILED_DUCK = 26427;

    // Tukkakoskelo
    public static final int OFFICIAL_CODE_RED_BREASTED_MERGANSER = 26440;

    // Isokoskelo
    public static final int OFFICIAL_CODE_GOOSANDER = 26442;

    // Nokikana
    public static final int OFFICIAL_CODE_COOT = 27381;

    // Merihanhi
    public static final int OFFICIAL_CODE_GREYLAG_GOOSE = 26291;

    // "villiintynyt kissa"
    public static final int OFFICIAL_CODE_DOMESTICATED_CAT = 53004;

    public static final int[] ALL_GAME_SPECIES_CODES = new int[] {
            26366, 27152, OFFICIAL_CODE_COOT, 27649, 27911, 37178, 37166, 37122, 27750, 27759, 200535, 33117, OFFICIAL_CODE_PARTRIDGE, 26921, 26922,
            26931, 26926, 26928, OFFICIAL_CODE_RINGED_SEAL, OFFICIAL_CODE_HARBOUR_SEAL, OFFICIAL_CODE_GREY_SEAL, 26298, OFFICIAL_CODE_GREYLAG_GOOSE, 26373, OFFICIAL_CODE_WIGEON, OFFICIAL_CODE_PINTAIL, OFFICIAL_CODE_GARGANEY, OFFICIAL_CODE_SHOVELER, OFFICIAL_CODE_POCHARD, OFFICIAL_CODE_TUFTED_DUCK,
            OFFICIAL_CODE_COMMON_EIDER, OFFICIAL_CODE_LONG_TAILED_DUCK, 26435, OFFICIAL_CODE_RED_BREASTED_MERGANSER, OFFICIAL_CODE_GOOSANDER, OFFICIAL_CODE_BEAN_GOOSE, OFFICIAL_CODE_MOUNTAIN_HARE, OFFICIAL_CODE_BROWN_HARE, OFFICIAL_CODE_NUTRIA, OFFICIAL_CODE_RED_DEER, OFFICIAL_CODE_SIKA_DEER, OFFICIAL_CODE_MUFFLON, OFFICIAL_CODE_DOMESTICATED_CAT, OFFICIAL_CODE_RED_SQUIRREL, OFFICIAL_CODE_EUROPEAN_BEAVER,
            OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_MUSKRAT, OFFICIAL_CODE_BLUE_FOX, OFFICIAL_CODE_MOOSE, OFFICIAL_CODE_WHITE_TAILED_DEER, OFFICIAL_CODE_ROE_DEER, OFFICIAL_CODE_WILD_FOREST_REINDEER, OFFICIAL_CODE_FALLOW_DEER, OFFICIAL_CODE_RACCOON, OFFICIAL_CODE_ERMINE, OFFICIAL_CODE_EUROPEAN_POLECAT, OFFICIAL_CODE_OTTER, OFFICIAL_CODE_PINE_MARTEN, OFFICIAL_CODE_AMERICAN_MINK, OFFICIAL_CODE_RABBIT,
            OFFICIAL_CODE_RED_FOX, OFFICIAL_CODE_RACCOON_DOG, OFFICIAL_CODE_BADGER, OFFICIAL_CODE_WILD_BOAR, OFFICIAL_CODE_LYNX, OFFICIAL_CODE_BEAR, OFFICIAL_CODE_WOLF, OFFICIAL_CODE_WOLVERINE
    };


    public static final ImmutableSet<Integer> MOOSELIKE = ImmutableSet
            .of(OFFICIAL_CODE_FALLOW_DEER,
                    OFFICIAL_CODE_MOOSE,
                    OFFICIAL_CODE_ROE_DEER,
                    OFFICIAL_CODE_WHITE_TAILED_DEER,
                    OFFICIAL_CODE_WILD_FOREST_REINDEER);

    public static final ImmutableSet<Integer> DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING = ImmutableSet
            .of(OFFICIAL_CODE_FALLOW_DEER, OFFICIAL_CODE_WHITE_TAILED_DEER, OFFICIAL_CODE_WILD_FOREST_REINDEER);

    public static final ImmutableSet<Integer> MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING = ImmutableSet
            .of(OFFICIAL_CODE_MOOSE,
                    OFFICIAL_CODE_FALLOW_DEER,
                    OFFICIAL_CODE_WHITE_TAILED_DEER,
                    OFFICIAL_CODE_WILD_FOREST_REINDEER);

    public static final ImmutableSet<Integer> PERMIT_REQUIRED_WITHOUT_SEASON = ImmutableSet.of(
            OFFICIAL_CODE_ROE_DEER, OFFICIAL_CODE_BEAR, OFFICIAL_CODE_WOLVERINE, OFFICIAL_CODE_LYNX, OFFICIAL_CODE_WOLF,
            OFFICIAL_CODE_GREY_SEAL, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_RINGED_SEAL, OFFICIAL_CODE_OTTER,
            OFFICIAL_CODE_WIGEON, OFFICIAL_CODE_WILD_BOAR, OFFICIAL_CODE_EUROPEAN_POLECAT, OFFICIAL_CODE_BEAN_GOOSE,
            OFFICIAL_CODE_PINTAIL, OFFICIAL_CODE_GARGANEY, OFFICIAL_CODE_SHOVELER, OFFICIAL_CODE_POCHARD,
            OFFICIAL_CODE_TUFTED_DUCK, OFFICIAL_CODE_COMMON_EIDER, OFFICIAL_CODE_LONG_TAILED_DUCK,
            OFFICIAL_CODE_RED_BREASTED_MERGANSER, OFFICIAL_CODE_GOOSANDER, OFFICIAL_CODE_COOT,
            OFFICIAL_CODE_GREYLAG_GOOSE);

    public static final ImmutableSet<Integer> LARGE_CARNIVORES =
            ImmutableSet.of(OFFICIAL_CODE_BEAR, OFFICIAL_CODE_LYNX, OFFICIAL_CODE_WOLF, OFFICIAL_CODE_WOLVERINE);

    // All fowl and unprotected species except "domesticated cat" (53004)
    public static final ImmutableSet<Integer> BIRD_PERMIT_SPECIES = ImmutableSet.of(
            OFFICIAL_CODE_BEAN_GOOSE, OFFICIAL_CODE_GREYLAG_GOOSE, 26298, OFFICIAL_CODE_WIGEON, 26366, 26373, OFFICIAL_CODE_PINTAIL, OFFICIAL_CODE_GARGANEY, OFFICIAL_CODE_SHOVELER, OFFICIAL_CODE_POCHARD, OFFICIAL_CODE_TUFTED_DUCK, OFFICIAL_CODE_COMMON_EIDER, OFFICIAL_CODE_LONG_TAILED_DUCK, 26435, OFFICIAL_CODE_RED_BREASTED_MERGANSER,
            OFFICIAL_CODE_GOOSANDER, 26921, 26922, 26926, 26928, 26931, OFFICIAL_CODE_PARTRIDGE, 27152, OFFICIAL_CODE_COOT, 27649, 27750, 27759, 27911, 33117, 37122,
            37142, 37166, 37178, 200535);


    public static final ImmutableSet<Integer> MAMMAL_PERMIT_SPECIES = ImmutableSet.of(
            OFFICIAL_CODE_WOLVERINE,
            OFFICIAL_CODE_WOLF,
            OFFICIAL_CODE_BEAR,
            OFFICIAL_CODE_OTTER,
            OFFICIAL_CODE_LYNX,
            OFFICIAL_CODE_EUROPEAN_BEAVER,
            OFFICIAL_CODE_RINGED_SEAL,
            OFFICIAL_CODE_HARBOUR_SEAL,
            OFFICIAL_CODE_GREY_SEAL,
            OFFICIAL_CODE_EUROPEAN_POLECAT,
            OFFICIAL_CODE_PINE_MARTEN,
            OFFICIAL_CODE_MOUNTAIN_HARE,


            OFFICIAL_CODE_RABBIT,
            OFFICIAL_CODE_BROWN_HARE,
            OFFICIAL_CODE_RED_SQUIRREL,
            OFFICIAL_CODE_CANADIAN_BEAVER,
            OFFICIAL_CODE_BLUE_FOX,
            OFFICIAL_CODE_RED_FOX,
            OFFICIAL_CODE_BADGER,
            OFFICIAL_CODE_ERMINE,
            OFFICIAL_CODE_WILD_BOAR,
            OFFICIAL_CODE_FALLOW_DEER,
            OFFICIAL_CODE_RED_DEER,
            OFFICIAL_CODE_SIKA_DEER,
            OFFICIAL_CODE_ROE_DEER,
            OFFICIAL_CODE_MOOSE,
            OFFICIAL_CODE_WHITE_TAILED_DEER,
            OFFICIAL_CODE_MUFFLON,

            OFFICIAL_CODE_WILD_FOREST_REINDEER);

    public static final ImmutableSet<Integer> NEST_REMOVAL_PERMIT_SPECIES = ImmutableSet.of(
            OFFICIAL_CODE_WOLVERINE,
            OFFICIAL_CODE_WOLF,
            OFFICIAL_CODE_BEAR,
            OFFICIAL_CODE_OTTER,
            OFFICIAL_CODE_LYNX,
            OFFICIAL_CODE_EUROPEAN_BEAVER,
            OFFICIAL_CODE_GREY_SEAL,
            OFFICIAL_CODE_HARBOUR_SEAL,
            OFFICIAL_CODE_RINGED_SEAL,
            OFFICIAL_CODE_EUROPEAN_POLECAT,
            OFFICIAL_CODE_PINE_MARTEN,
            OFFICIAL_CODE_MOUNTAIN_HARE,

            // fowl and unprotected except domestic cat
            OFFICIAL_CODE_BEAN_GOOSE, OFFICIAL_CODE_GREYLAG_GOOSE, 26298, OFFICIAL_CODE_WIGEON, 26366, 26373, OFFICIAL_CODE_PINTAIL, OFFICIAL_CODE_GARGANEY, OFFICIAL_CODE_SHOVELER, OFFICIAL_CODE_POCHARD, OFFICIAL_CODE_TUFTED_DUCK, OFFICIAL_CODE_COMMON_EIDER, OFFICIAL_CODE_LONG_TAILED_DUCK, 26435, OFFICIAL_CODE_RED_BREASTED_MERGANSER,
            OFFICIAL_CODE_GOOSANDER, 26921, 26922, 26926, 26928, 26931, OFFICIAL_CODE_PARTRIDGE, 27152, OFFICIAL_CODE_COOT, 27649, 27750, 27759, 27911, 33117, 37122,
            37142, 37166, 37178, 200535,

            OFFICIAL_CODE_RABBIT,
            OFFICIAL_CODE_BROWN_HARE,
            OFFICIAL_CODE_RED_SQUIRREL,
            OFFICIAL_CODE_CANADIAN_BEAVER,
            OFFICIAL_CODE_BLUE_FOX,
            OFFICIAL_CODE_RED_FOX,
            OFFICIAL_CODE_BADGER,
            OFFICIAL_CODE_ERMINE,
            OFFICIAL_CODE_WILD_BOAR,
            OFFICIAL_CODE_FALLOW_DEER,
            OFFICIAL_CODE_RED_DEER,
            OFFICIAL_CODE_SIKA_DEER,
            OFFICIAL_CODE_ROE_DEER,
            OFFICIAL_CODE_MOOSE,
            OFFICIAL_CODE_WHITE_TAILED_DEER,
            OFFICIAL_CODE_WILD_FOREST_REINDEER,
            OFFICIAL_CODE_MUFFLON

    );

    public static final ImmutableSet<Integer> LAW_SECTION_TEN_PERMIT_SPECIES = ImmutableSet.of(
            OFFICIAL_CODE_EUROPEAN_BEAVER,
            OFFICIAL_CODE_PARTRIDGE
    );

    public static final ImmutableSet<Integer> HABITATS_DIRECTIVE_ANNEX_IV_SPECIES = ImmutableSet.of(
            OFFICIAL_CODE_LYNX,
            OFFICIAL_CODE_BEAR,
            OFFICIAL_CODE_OTTER,
            OFFICIAL_CODE_WOLF
    );

    public static final ImmutableSet<Integer> DOG_EVENT_DISTURBANCE_SPECIES = ImmutableSet.of(
            OFFICIAL_CODE_LYNX,
            OFFICIAL_CODE_BEAR,
            OFFICIAL_CODE_OTTER,
            OFFICIAL_CODE_WOLF
    );

    private Long id;

    @Column(nullable = false)
    private int officialCode;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameFinnish;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameSwedish;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameEnglish;

    @Size(max = 100)
    @Column(length = 100)
    private String scientificName;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private GameCategory category;

    @Column(nullable = false)
    private boolean multipleSpecimenAllowedOnHarvest;

    @OneToMany(mappedBy = "species")
    private Set<ObservationBaseFields> observationBaseFields = new HashSet<>();

    @OneToMany(mappedBy = "species")
    private Set<ObservationContextSensitiveFields> observationContextSensitiveFields = new HashSet<>();

    @Column
    private Integer srvaOrdinal;

    // Static helpers for checking one specific species.

    public static boolean isBeanGoose(final int speciesCode) {
        return speciesCode == OFFICIAL_CODE_BEAN_GOOSE;
    }

    public static boolean isBear(final int speciesCode) {
        return speciesCode == OFFICIAL_CODE_BEAR;
    }

    public static boolean isFallowDeer(final int speciesCode) {
        return speciesCode == OFFICIAL_CODE_FALLOW_DEER;
    }

    public static boolean isGreySeal(final int speciesCode) {
        return speciesCode == OFFICIAL_CODE_GREY_SEAL;
    }

    public static boolean isMoose(final int speciesCode) {
        return speciesCode == OFFICIAL_CODE_MOOSE;
    }

    public static boolean isRoeDeer(final int speciesCode) {
        return speciesCode == OFFICIAL_CODE_ROE_DEER;
    }

    public static boolean isWhiteTailedDeer(final int speciesCode) {
        return speciesCode == OFFICIAL_CODE_WHITE_TAILED_DEER;
    }

    public static boolean isWildBoar(final int speciesCode) {
        return speciesCode == OFFICIAL_CODE_WILD_BOAR;
    }

    public static boolean isWildForestReindeer(final int speciesCode) {
        return speciesCode == OFFICIAL_CODE_WILD_FOREST_REINDEER;
    }

    public static boolean isWolf(final int speciesCode) {
        return speciesCode == OFFICIAL_CODE_WOLF;
    }

    // Static helpers for checking species group.

    public static boolean isBeaver(final int speciesCode) {
        return OFFICIAL_CODE_CANADIAN_BEAVER == speciesCode || OFFICIAL_CODE_EUROPEAN_BEAVER == speciesCode;
    }

    public static boolean isBirdPermitSpecies(final int speciesCode) {
        return BIRD_PERMIT_SPECIES.contains(speciesCode);
    }

    public static boolean isDeerRequiringPermitForHunting(final int speciesCode) {
        return DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING.contains(speciesCode);
    }

    public static boolean isDogEventDisturbanceSpecies(final int speciesCode) {
        return DOG_EVENT_DISTURBANCE_SPECIES.contains(speciesCode);
    }

    public static boolean isHabitatsAnnexIVSpecies(final int speciesCode) {
        return HABITATS_DIRECTIVE_ANNEX_IV_SPECIES.contains(speciesCode);
    }

    public static boolean isLargeCarnivore(final int speciesCode) {
        return LARGE_CARNIVORES.contains(speciesCode);
    }

    public static boolean isLawSectionTenPermitSpecies(final int speciesCode) {
        return LAW_SECTION_TEN_PERMIT_SPECIES.contains(speciesCode);
    }

    public static boolean isMammalSpecies(final int speciesCode) {
        return MAMMAL_PERMIT_SPECIES.contains(speciesCode);
    }

    public static boolean isMooselike(final int speciesCode) {
        return MOOSELIKE.contains(speciesCode);
    }

    public static boolean isMooseOrDeerRequiringPermitForHunting(final int speciesCode) {
        return MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING.contains(speciesCode);
    }

    public static boolean isNestRemovalPermitSpecies(final int speciesCode) {
        return NEST_REMOVAL_PERMIT_SPECIES.contains(speciesCode);
    }

    public static boolean isPermitRequiredWithoutSeason(final int speciesCode) {
        return PERMIT_REQUIRED_WITHOUT_SEASON.contains(speciesCode);
    }

    public GameSpecies() {
    }

    public GameSpecies(final int officialCode,
                       final GameCategory category,
                       final String nameFinnish,
                       final String nameSwedish,
                       final String nameEnglish) {

        this.officialCode = officialCode;
        this.category = category;
        this.nameFinnish = nameFinnish;
        this.nameSwedish = nameSwedish;
        this.nameEnglish = nameEnglish;
    }

    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFinnish, nameSwedish, nameEnglish);
    }

    public boolean isBear() {
        return isBear(officialCode);
    }

    public boolean isBeaver() {
        return isBeaver(officialCode);
    }

    public boolean isDeerRequiringPermitForHunting() {
        return isDeerRequiringPermitForHunting(officialCode);
    }

    public boolean isLargeCarnivore() {
        return isLargeCarnivore(officialCode);
    }

    public boolean isMoose() {
        return isMoose(officialCode);
    }

    public boolean isWhiteTailedDeer() {
        return isWhiteTailedDeer(officialCode);
    }

    public boolean isMooseOrDeerRequiringPermitForHunting() {
        return isMooseOrDeerRequiringPermitForHunting(officialCode);
    }

    public boolean isWolf() {
        return isWolf(officialCode);
    }

    // QueryDSL delegates -->

    @QueryDelegate(GameSpecies.class)
    public static fi.riista.util.QLocalisedString nameLocalisation(final QGameSpecies species) {
        return new fi.riista.util.QLocalisedString(species.nameFinnish, species.nameSwedish, species.nameEnglish);
    }

    // Accessors -->

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

    @Override
    public int getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(final int officialCode) {
        this.officialCode = officialCode;
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

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(final String nameEnglish) {
        this.nameEnglish = nameEnglish;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(final String scientificName) {
        this.scientificName = scientificName;
    }

    public GameCategory getCategory() {
        return category;
    }

    public void setCategory(final GameCategory category) {
        this.category = category;
    }

    public boolean isMultipleSpecimenAllowedOnHarvest() {
        return multipleSpecimenAllowedOnHarvest;
    }

    public void setMultipleSpecimenAllowedOnHarvest(final boolean multipleSpecimenAllowedOnHarvest) {
        this.multipleSpecimenAllowedOnHarvest = multipleSpecimenAllowedOnHarvest;
    }

    Set<ObservationBaseFields> getObservationBaseFields() {
        return observationBaseFields;
    }

    Set<ObservationContextSensitiveFields> getObservationContextSensitiveFields() {
        return observationContextSensitiveFields;
    }

    public Integer getSrvaOrdinal() {
        return srvaOrdinal;
    }

    public void setSrvaOrdinal(final Integer srvaOrdinal) {
        this.srvaOrdinal = srvaOrdinal;
    }
}
