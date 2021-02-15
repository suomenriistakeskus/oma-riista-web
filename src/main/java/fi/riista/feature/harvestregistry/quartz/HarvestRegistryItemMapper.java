package fi.riista.feature.harvestregistry.quartz;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class HarvestRegistryItemMapper {

    public static final LocalDate END_TIMESTAMP_2019 = new LocalDate(2020, 8, 1);

    public static Stream<HarvestRegistryItem> transform(final Harvest harvest,
                                                        final Person actualShooter,
                                                        final Set<HarvestSpecimen> specimens,
                                                        final boolean derogation,
                                                        final String rkaCode,
                                                        final String rhyCode,
                                                        final Map<String, LocalisedString> municipalities) {

        // Poikkeusluvilla/metsästyksenaikaiset
        if (derogation) {
            return HarvestRegistryDerogationMapper.transform(harvest, actualShooter, specimens, rkaCode, rhyCode);
        } else if (harvest.getPointOfTime().toLocalDate().isBefore(END_TIMESTAMP_2019)) {
            return HarvestRegistryHarvest2019Mapper.transform(harvest, actualShooter, specimens, rkaCode, rhyCode);
        } else {
            return HarvestRegistryHarvest2020Mapper.transform(harvest, actualShooter, specimens, rkaCode, rhyCode, municipalities);
        }
    }

    /*package*/
    static HarvestRegistryItem createFrom(final Harvest harvest, final Person actualShooter, final String rkaCode,
                                          final String rhyCode) {
        final HarvestRegistryItem item = new HarvestRegistryItem();
        item.setHarvest(harvest);
        item.setShooterName(actualShooter.getFullName());
        item.setShooterHunterNumber(actualShooter.getHunterNumber());
        item.setSpecies(harvest.getSpecies());
        item.setAmount(harvest.getAmount());
        item.setPointOfTime(harvest.getPointOfTime());
        item.setGeoLocation(harvest.getGeoLocation());
        item.setMunicipalityCode(harvest.getMunicipalityCode());
        item.setRkaCode(rkaCode);
        item.setRhyCode(rhyCode);
        return item;
    }
}
