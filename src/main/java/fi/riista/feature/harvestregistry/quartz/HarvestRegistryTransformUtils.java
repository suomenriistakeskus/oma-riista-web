package fi.riista.feature.harvestregistry.quartz;

import com.google.common.collect.Lists;
import fi.riista.config.Constants;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.harvestregistry.ShooterAddress;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.LocalisedString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryExtraAttributes.AGE;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryExtraAttributes.GENDER;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryExtraAttributes.GEOLOCATION;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryExtraAttributes.GREY_SEAL_2020_FIELDS;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryExtraAttributes.MUNICIPALITY;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryExtraAttributes.TIME;
import static java.util.Optional.ofNullable;

public interface HarvestRegistryTransformUtils {

    Set<HarvestRegistryExtraAttributes> getExtraAttributes(int speciesCode);

    default Stream<HarvestRegistryItem> transform(final Harvest harvest,
                                                  final Person actualShooter,
                                                  final Set<HarvestSpecimen> specimens,
                                                  final String rkaCode, final String rhyCode,
                                                  final Map<String, LocalisedString> municipalities) {

        final Set<HarvestRegistryExtraAttributes> extraAttributes =
                getExtraAttributes(harvest.getSpecies().getOfficialCode());

        if (extraAttributes == null) {
            return Stream.empty();
        }

        // Amount that have no specimens specified for the harvest
        final int amountWithNoSpecimenInfo = harvest.getAmount() - specimens.size();

        final List<HarvestRegistryItem> items = Lists.newArrayListWithExpectedSize(specimens.size() + 1);

        specimens.forEach(specimen ->
                items.add(mapToItem(harvest, actualShooter,
                        rkaCode, rhyCode, extraAttributes, Optional.of(specimen), municipalities.get(harvest.getMunicipalityCode()))));

        if (amountWithNoSpecimenInfo > 0) {
            final HarvestRegistryItem itemWithNoSpecimenInfo = mapToItem(harvest, actualShooter,
                    rkaCode, rhyCode, extraAttributes, Optional.empty(), municipalities.get(harvest.getMunicipalityCode()));
            itemWithNoSpecimenInfo.setAmount(amountWithNoSpecimenInfo);
            items.add(itemWithNoSpecimenInfo);
        }

        return items.stream();
    }

    default HarvestRegistryItem mapToItem(final Harvest harvest,
                                          final Person actualShooter,
                                          final String rkaCode,
                                          final String rhyCode,
                                          final Set<HarvestRegistryExtraAttributes> extraAttributes,
                                          final Optional<HarvestSpecimen> specimen,
                                          final LocalisedString municipality) {
        final HarvestRegistryItem item = createFrom(harvest, actualShooter, rkaCode, rhyCode);

        if (specimen.isPresent()) {
            item.setAmount(1);
        }

        if (extraAttributes.contains(TIME)) {
            item.setPointOfTime(harvest.getPointOfTime());
            item.setTimeOfDayValid(true);
        } else {
            item.setPointOfTime(harvest.getPointOfTime().toLocalDate().toDateTimeAtStartOfDay(Constants.DEFAULT_TIMEZONE));
            item.setTimeOfDayValid(false);
        }

        if (extraAttributes.contains(AGE)) {
            specimen.ifPresent(s -> item.setAge(s.getAge()));
        }

        if (extraAttributes.contains(GENDER)) {
            specimen.ifPresent(s -> item.setGender(s.getGender()));
        }

        if (extraAttributes.contains(MUNICIPALITY)) {
            ofNullable(municipality).ifPresent(
                    name -> {
                        item.setMunicipalityFinnish(name.getFinnish());
                        item.setMunicipalitySwedish(name.getSwedish());
                    }
            );
        }

        if (extraAttributes.contains(GEOLOCATION)) {
            item.setGeoLocation(harvest.getGeoLocation());
        }

        if (extraAttributes.contains(GREY_SEAL_2020_FIELDS)) {
            // Shooter address is required only for grey seal and therefore not fetched in query for all harvests.
            // If future rules change to include several species, address should be fetched in the query
            // to avoid N+1 issues.
            item.setShooterAddress(ShooterAddress.createFrom(harvest.getActualShooter().getAddress()));

            ofNullable(harvest.getHarvestQuota())
                    .ifPresent(quota -> {
                        item.setHarvestAreaFinnish(quota.getHarvestArea().getNameFinnish());
                        item.setHarvestAreaSwedish(quota.getHarvestArea().getNameSwedish());
                    });
            ofNullable(harvest.getRhy())
                    .ifPresent(rhy -> {
                        item.setRkaFinnish(rhy.getParentOrganisation().getNameFinnish());
                        item.setRkaSwedish(rhy.getParentOrganisation().getNameSwedish());
                        item.setRhyFinnish(rhy.getNameFinnish());
                        item.setRhySwedish(rhy.getNameSwedish());
                    });

            specimen.ifPresent(s -> {
                item.setGender(s.getGender());
                item.setWeight(s.getWeight());
            });
        }

        return item;
    }

    default HarvestRegistryItem createFrom(final Harvest harvest, final Person actualShooter,
                                           final String rkaCode,
                                           final String rhyCode) {
        final HarvestRegistryItem item = new HarvestRegistryItem();
        item.setHarvest(harvest);
        item.setShooterName(actualShooter.getFullName());
        item.setShooterHunterNumber(actualShooter.getHunterNumber());
        item.setSpecies(harvest.getSpecies());
        item.setAmount(harvest.getAmount());
        item.setMunicipalityCode(harvest.getMunicipalityCode());
        item.setRkaCode(rkaCode);
        item.setRhyCode(rhyCode);
        return item;
    }
}
