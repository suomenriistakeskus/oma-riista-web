package fi.riista.feature.gamediary;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.common.entity.HasOfficialCode;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields;
import fi.riista.util.LocalisedString;
import org.hibernate.validator.constraints.NotBlank;

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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Access(AccessType.FIELD)
public class GameSpecies extends LifecycleEntity<Long> implements HasOfficialCode {

    public static final String ID_COLUMN_NAME = "game_species_id";

    public static final int OFFICIAL_CODE_CANADIAN_BEAVER = 48250;
    public static final int OFFICIAL_CODE_BEAR = 47348;
    public static final int OFFICIAL_CODE_EUROPEAN_BEAVER = 48251;
    public static final int OFFICIAL_CODE_LYNX = 46615;
    public static final int OFFICIAL_CODE_WOLF = 46549;
    public static final int OFFICIAL_CODE_WOLVERINE = 47212;

    public static final int OFFICIAL_CODE_MOOSE = 47503;

    /**
     * kuusipeura
     */
    public static final int OFFICIAL_CODE_FALLOW_DEER = 47484;

    /**
     * metsäkauris
     */
    public static final int OFFICIAL_CODE_ROE_DEER = 47507;

    /**
     * valkohäntäpeura
     */
    public static final int OFFICIAL_CODE_WHITE_TAILED_DEER = 47629;

    /**
     * metsäpeura
     */
    public static final int OFFICIAL_CODE_WILD_FOREST_REINDEER = 200556;

    public static final ImmutableSet<Integer> DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING = ImmutableSet
            .of(OFFICIAL_CODE_FALLOW_DEER, OFFICIAL_CODE_WHITE_TAILED_DEER, OFFICIAL_CODE_WILD_FOREST_REINDEER);

    public static final ImmutableSet<Integer> MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING = ImmutableSet
            .of(OFFICIAL_CODE_MOOSE,
                    OFFICIAL_CODE_FALLOW_DEER,
                    OFFICIAL_CODE_WHITE_TAILED_DEER,
                    OFFICIAL_CODE_WILD_FOREST_REINDEER);

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

    public GameSpecies() {
    }

    public GameSpecies(
            final int officialCode,
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

    public static boolean isMoose(final int speciesCode) {
        return speciesCode == OFFICIAL_CODE_MOOSE;
    }

    public static boolean isDeerRequiringPermitForHunting(final int speciesCode) {
        return DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING.contains(speciesCode);
    }

    public static boolean isMooseOrDeerRequiringPermitForHunting(final int speciesCode) {
        return MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING.contains(speciesCode);
    }

    public static boolean isBeaver(final int speciesCode) {
        return OFFICIAL_CODE_CANADIAN_BEAVER == speciesCode || OFFICIAL_CODE_EUROPEAN_BEAVER == speciesCode;
    }

    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFinnish, nameSwedish, nameEnglish);
    }

    public boolean isMoose() {
        return isMoose(officialCode);
    }

    public boolean isDeerRequiringPermitForHunting() {
        return isDeerRequiringPermitForHunting(officialCode);
    }

    public boolean isMooseOrDeerRequiringPermitForHunting() {
        return isMooseOrDeerRequiringPermitForHunting(officialCode);
    }

    public boolean isBeaver() {
        return isBeaver(officialCode);
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
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(int officialCode) {
        this.officialCode = officialCode;
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

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public GameCategory getCategory() {
        return category;
    }

    public void setCategory(GameCategory category) {
        this.category = category;
    }

    public boolean isMultipleSpecimenAllowedOnHarvest() {
        return multipleSpecimenAllowedOnHarvest;
    }

    public void setMultipleSpecimenAllowedOnHarvest(boolean multipleSpecimenAllowedOnHarvest) {
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

    public void setSrvaOrdinal(Integer srvaOrdinal) {
        this.srvaOrdinal = srvaOrdinal;
    }
}
