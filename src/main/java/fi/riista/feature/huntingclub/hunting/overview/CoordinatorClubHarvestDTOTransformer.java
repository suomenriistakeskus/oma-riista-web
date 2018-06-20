package fi.riista.feature.huntingclub.hunting.overview;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.impl.JPAQuery;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformerBase;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static fi.riista.util.Collect.idSet;
import static java.util.stream.Collectors.toList;

@Component
public class CoordinatorClubHarvestDTOTransformer extends HarvestDTOTransformerBase<HarvestDTO> {

    @PersistenceContext
    private EntityManager entityManager;

    @Nonnull
    @Override
    protected List<HarvestDTO> transform(@Nonnull final List<Harvest> harvests) {
        final Function<Harvest, GameSpecies> harvestToSpecies = getGameDiaryEntryToSpeciesMapping(harvests);
        final Map<Harvest, List<HarvestSpecimen>> groupedSpecimens = getSpecimensGroupedByHarvests(harvests);
        final Function<Harvest, HuntingClub> harvestToHuntingClubMapping = getHarvestToHuntingClubMapping(harvests);

        return harvests.stream()
                .filter(Objects::nonNull)
                .map(harvest -> createDTO(harvest,
                        harvestToSpecies.apply(harvest),
                        groupedSpecimens.get(harvest),
                        harvestToHuntingClubMapping.apply(harvest)))
                .collect(toList());
    }

    private Function<Harvest, HuntingClub> getHarvestToHuntingClubMapping(final List<Harvest> harvestList) {
        final Set<Long> huntingDayIdSet = harvestList.stream().map(Harvest::getHuntingDayOfGroup).collect(idSet());
        final Map<Long, HuntingClub> result = new JPAQuery<>(entityManager)
                .from(QGroupHuntingDay.groupHuntingDay)
                .join(QGroupHuntingDay.groupHuntingDay.group, QHuntingClubGroup.huntingClubGroup)
                .join(QHuntingClubGroup.huntingClubGroup.parentOrganisation, QHuntingClub.huntingClub._super)
                .where(QGroupHuntingDay.groupHuntingDay.id.in(huntingDayIdSet))
                .transform(GroupBy.groupBy(QGroupHuntingDay.groupHuntingDay.id).as(QHuntingClub.huntingClub));

        return harvest -> result.get(F.getId(harvest.getHuntingDayOfGroup()));
    }

    private static HarvestDTO createDTO(final Harvest harvest,
                                        final GameSpecies species,
                                        final List<HarvestSpecimen> specimens,
                                        final HuntingClub huntingClub) {

        final HarvestDTO dto = HarvestDTO.builder()
                .populateWith(harvest)
                .populateWith(species)
                .populateSpecimensWith(specimens)
                .withDescription(null)
                .withCanEdit(false)
                .build();

        if (huntingClub != null) {
            final HuntingClubDTO clubDTO = new HuntingClubDTO();
            clubDTO.setNameFI(huntingClub.getNameFinnish());
            clubDTO.setNameSV(huntingClub.getNameSwedish());

            dto.setHuntingClub(clubDTO);
        }

        return dto;
    }
}
