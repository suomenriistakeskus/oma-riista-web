package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Component
public class MobileOccupationDTOFactory {

    @Resource
    private EnumLocaliser enumLocaliser;

    public List<MobileOccupationDTO> create(@Nonnull Iterable<Occupation> occupations) {
        return F.mapNonNullsToList(occupations, this::create);
    }

    public MobileOccupationDTO create(@Nonnull Occupation occupation) {
        Objects.requireNonNull(occupation);

        MobileOccupationDTO dto = new MobileOccupationDTO();
        dto.setOrganisation(MobileOrganisationDTO.create(occupation.getOrganisation()));
        dto.setOccupationType(occupation.getOccupationType());
        dto.setBeginDate(occupation.getBeginDate());
        dto.setEndDate(occupation.getEndDate());

        LocalisedString occTypeLocalized = enumLocaliser.getLocalisedString(occupation.getOccupationType());
        dto.setName(occTypeLocalized.asMap());
        return dto;
    }
}
