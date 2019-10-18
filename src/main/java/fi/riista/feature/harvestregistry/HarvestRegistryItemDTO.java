package fi.riista.feature.harvestregistry;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class HarvestRegistryItemDTO implements HasID<Long> {

    private final long id;

    private final String shooterName;

    private final String shooterHunterNumber;

    private final LocalDateTime pointOfTime;

    private final LocalisedString species;

    private final Integer speciesCode;

    private final Integer amount;

    private final GameGender gender;

    private final GameAge age;

    private final Double weight;

    private final GeoLocation geoLocation;

    private final LocalisedString municipality;

    private final LocalisedString harvestArea;

    private final LocalisedString rka;

    private final LocalisedString rhy;

    private final HarvestRegistryShooterAddressDTO shooterAddress;

    public static HarvestRegistryItemDTO from(@Nonnull final HarvestRegistryItem entity,
                                              @Nonnull final Supplier<GameSpecies> speciesSupplier) {
        requireNonNull(entity);
        requireNonNull(speciesSupplier);

        final GameSpecies gameSpecies = speciesSupplier.get();

        return Builder.builder()
                .withId(entity.getId())
                .withShooterName(entity.getShooterName())
                .withShooterAddress(HarvestRegistryShooterAddressDTO.createFrom(entity))
                .withShooterHunterNumber(entity.getShooterHunterNumber())
                .withPointOfTime(DateUtil.toLocalDateTimeNullSafe(entity.getPointOfTime()))
                .withSpecies(gameSpecies.getNameLocalisation())
                .withSpeciesCode(gameSpecies.getOfficialCode())
                .withAmount(entity.getAmount())
                .withGender(entity.getGender())
                .withAge(entity.getAge())
                .withWeight(entity.getWeight())
                .withGeoLocation(entity.getGeoLocation())
                .withHarvestArea(entity.getHarvestAreaLocalisation())
                .withMunicipality(entity.getMunicipalityLocalisation())
                .withRka(entity.getRkaLocalisation())
                .withRhy(entity.getRhyLocalisation())
                .build();
    }


    @Override
    public Long getId() {
        return id;
    }

    public String getShooterName() {
        return shooterName;
    }

    public String getShooterHunterNumber() {
        return shooterHunterNumber;
    }

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public LocalisedString getSpecies() {
        return species;
    }

    public Integer getSpeciesCode() {
        return speciesCode;
    }

    public Integer getAmount() {
        return amount;
    }

    public GameGender getGender() {
        return gender;
    }

    public GameAge getAge() {
        return age;
    }

    public Double getWeight() {
        return weight;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public LocalisedString getMunicipality() {
        return municipality;
    }

    public LocalisedString getHarvestArea() {
        return harvestArea;
    }

    public LocalisedString getRka() {
        return rka;
    }

    public LocalisedString getRhy() {
        return rhy;
    }

    public HarvestRegistryShooterAddressDTO getShooterAddress() {
        return shooterAddress;
    }

    private HarvestRegistryItemDTO(final long id, final String shooterName, final String shooterHunterNumber,
                                   final LocalDateTime pointOfTime, final LocalisedString species,
                                   final Integer speciesCode,
                                   final Integer amount, final GameGender gender, final GameAge age,
                                   final Double weight, final GeoLocation geoLocation,
                                   final LocalisedString municipality, final LocalisedString harvestArea,
                                   final LocalisedString rka, final LocalisedString rhy,
                                   final HarvestRegistryShooterAddressDTO shooterAddress) {
        this.id = id;
        this.shooterName = shooterName;
        this.shooterHunterNumber = shooterHunterNumber;
        this.pointOfTime = pointOfTime;
        this.species = species;
        this.speciesCode = speciesCode;
        this.amount = amount;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.geoLocation = geoLocation;
        this.municipality = municipality;
        this.harvestArea = harvestArea;
        this.rka = rka;
        this.rhy = rhy;
        this.shooterAddress = shooterAddress;
    }


    public static final class Builder {
        private Long id;
        private String shooterName;
        private String shooterHunterNumber;
        private LocalDateTime pointOfTime;
        private LocalisedString species;
        private Integer speciesCode;
        private Integer amount;
        private GameGender gender;
        private GameAge age;
        private Double weight;
        private GeoLocation geoLocation;
        private LocalisedString municipality;
        private LocalisedString harvestArea;
        private LocalisedString rka;
        private LocalisedString rhy;
        private HarvestRegistryShooterAddressDTO shooterAddress;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withShooterName(String shooterName) {
            this.shooterName = shooterName;
            return this;
        }

        public Builder withShooterHunterNumber(String shooterHunterNumber) {
            this.shooterHunterNumber = shooterHunterNumber;
            return this;
        }

        public Builder withPointOfTime(LocalDateTime pointOfTime) {
            this.pointOfTime = pointOfTime;
            return this;
        }

        public Builder withSpecies(LocalisedString species) {
            this.species = species;
            return this;
        }

        public Builder withSpeciesCode(Integer speciesCode) {
            this.speciesCode = speciesCode;
            return this;
        }

        public Builder withAmount(Integer amount) {
            this.amount = amount;
            return this;
        }

        public Builder withGender(GameGender gender) {
            this.gender = gender;
            return this;
        }

        public Builder withAge(GameAge age) {
            this.age = age;
            return this;
        }

        public Builder withWeight(Double weight) {
            this.weight = weight;
            return this;
        }

        public Builder withGeoLocation(GeoLocation geoLocation) {
            this.geoLocation = geoLocation;
            return this;
        }

        public Builder withMunicipality(LocalisedString municipality) {
            this.municipality = municipality;
            return this;
        }

        public Builder withHarvestArea(LocalisedString harvestArea) {
            this.harvestArea = harvestArea;
            return this;
        }

        public Builder withRka(LocalisedString rka) {
            this.rka = rka;
            return this;
        }

        public Builder withRhy(LocalisedString rhy) {
            this.rhy = rhy;
            return this;
        }

        public Builder withShooterAddress(HarvestRegistryShooterAddressDTO shooterAddress) {
            this.shooterAddress = shooterAddress;
            return this;
        }

        public HarvestRegistryItemDTO build() {
            return new HarvestRegistryItemDTO(id, shooterName, shooterHunterNumber, pointOfTime, species, speciesCode,
                    amount, gender, age, weight, geoLocation, municipality, harvestArea, rka, rhy, shooterAddress);
        }
    }
}
