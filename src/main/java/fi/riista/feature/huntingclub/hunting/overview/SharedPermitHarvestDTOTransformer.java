package fi.riista.feature.huntingclub.hunting.overview;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformerBase;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Component
public class SharedPermitHarvestDTOTransformer extends HarvestDTOTransformerBase<HarvestDTO> {

    @Nonnull
    @Override
    protected List<HarvestDTO> transform(@Nonnull final List<Harvest> harvests,
                                         @Nonnull final HarvestSpecVersion specVersion) {

        final Function<Harvest, GameSpecies> harvestToSpecies = getHarvestToSpeciesMapping(harvests);
        final Map<Harvest, List<HarvestSpecimen>> groupedSpecimens = getSpecimensGroupedByHarvests(harvests);

        return harvests.stream()
                .filter(Objects::nonNull)
                .map(harvest -> createDTO(
                        harvest, harvestToSpecies.apply(harvest), groupedSpecimens.get(harvest), specVersion))
                .collect(toList());
    }

    private static HarvestDTO createDTO(final Harvest harvest,
                                        final GameSpecies species,
                                        final List<HarvestSpecimen> specimens,
                                        final HarvestSpecVersion specVersion) {

        final HarvestDTO dto = HarvestDTO.builder(specVersion)
                .populateWith(harvest)
                .withGameSpeciesCode(species.getOfficialCode())
                .withSpecimensMappedFrom(specimens)
                .withDescription(null)
                .withCanEdit(false)
                .build();

        final HuntingClub club = harvest.getHuntingClubForStatistics();

        if (club != null) {
            final HuntingClubDTO clubDTO = new HuntingClubDTO();
            clubDTO.setNameFI(club.getNameFinnish());
            clubDTO.setNameSV(club.getNameSwedish());

            dto.setHuntingClub(clubDTO);
        }

        return dto;
    }
}
