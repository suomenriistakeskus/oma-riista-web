package fi.riista.feature.huntingclub.hunting.overview;

import fi.riista.feature.gamediary.harvest.HarvestDTOTransformerBase;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Component
public class SharedPermitHarvestDTOTransformer extends HarvestDTOTransformerBase<HarvestDTO> {

    @Transactional(readOnly = true)
    @Nonnull
    @Override
    protected List<HarvestDTO> transform(@Nonnull final List<Harvest> harvests) {
        Objects.requireNonNull(harvests, "harvests must not be null");

        final Function<Harvest, GameSpecies> harvestToSpecies = getGameDiaryEntryToSpeciesMapping(harvests);
        final Map<Harvest, List<HarvestSpecimen>> groupedSpecimens = getSpecimensGroupedByHarvests(harvests);

        return harvests.stream()
                .filter(Objects::nonNull)
                .map(harvest -> createDTO(harvest, harvestToSpecies.apply(harvest), groupedSpecimens.get(harvest)))
                .collect(toList());
    }

    private static HarvestDTO createDTO(
            final Harvest harvest,
            final GameSpecies species,
            final List<HarvestSpecimen> specimens) {

        final HarvestDTO dto = HarvestDTO.builder()
                .populateWith(harvest)
                .populateWith(species)
                .populateSpecimensWith(specimens)
                .withDescription(null)
                .withCanEdit(false)
                .build();

        dto.setReadOnly(true);
        dto.setReportedForMe(true);

        final HuntingClub club = harvest.getHuntingClub();

        if (club != null) {
            final HuntingClubDTO clubDTO = new HuntingClubDTO();
            clubDTO.setNameFI(club.getNameFinnish());
            clubDTO.setNameSV(club.getNameSwedish());

            dto.setHuntingClub(clubDTO);
        }

        return dto;
    }
}
