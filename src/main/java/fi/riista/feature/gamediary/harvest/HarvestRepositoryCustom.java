package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.Interval;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface HarvestRepositoryCustom {

    List<Harvest> findGroupHarvest(HuntingClubGroup group, Interval interval);

    Map<Long, Integer> countClubHarvestAmountGroupByGameSpeciesId(HuntingClub huntingClub,
                                                                  int huntingYear,
                                                                  Interval interval,
                                                                  Set<Integer> mooselike);

    List<Harvest> findHarvestsLinkedToHuntingDayWithinAreaOfRhy(Riistanhoitoyhdistys rhy,
                                                                GameSpecies species,
                                                                Interval interval);

    List<Harvest> findHarvestsLinkedToHuntingDayAndPermitOfRhy(Riistanhoitoyhdistys rhy,
                                                               GameSpecies species,
                                                               Interval interval);

    Map<Long, Integer> countByHarvestPermitIdAndSpeciesCode(final Collection<Long> permits, final int speciesCode);

    Map<HarvestArea.HarvestAreaDetailedType, Integer> countQuotaHarvestsByArea(final int speciesCode, final int huntingYear);

    Map<HarvestArea.HarvestAreaDetailedType, Integer> countQuotasByArea(final int speciesCode, final int huntingYear);

    List<Long> getHarvestIdsWhereOnlyAuthor(Person person);
}
