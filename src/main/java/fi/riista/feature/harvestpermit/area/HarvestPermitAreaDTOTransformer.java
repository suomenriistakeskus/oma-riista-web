package fi.riista.feature.harvestpermit.area;

import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Component
public class HarvestPermitAreaDTOTransformer extends ListTransformer<HarvestPermitArea, HarvestPermitAreaDTO> {
    @Nonnull
    @Override
    protected List<HarvestPermitAreaDTO> transform(@Nonnull final List<HarvestPermitArea> list) {
        if (list.isEmpty()) {
            return emptyList();
        }

        return list.stream().map(area -> {
            final HarvestPermitAreaDTO dto = new HarvestPermitAreaDTO();

            dto.setId(area.getId());
            dto.setRev(area.getConsistencyVersion());
            dto.setClubId(F.getId(area.getClub()));
            dto.setNameFinnish(area.getNameFinnish());
            dto.setNameSwedish(area.getNameSwedish());
            dto.setExternalId(area.getExternalId());
            dto.setHuntingYear(area.getHuntingYear());

            return dto;
        }).collect(toList());
    }
}
