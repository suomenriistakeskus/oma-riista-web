package fi.riista.feature.shootingtest.official;

import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@Component
public class ShootingTestOfficialOccupationDTOTransformer extends ListTransformer<Occupation, ShootingTestOfficialOccupationDTO> {

    @Nonnull
    @Override
    protected List<ShootingTestOfficialOccupationDTO> transform(@Nonnull final List<Occupation> list) {
        List<ShootingTestOfficialOccupationDTO> officialOccupationDTOs = list.isEmpty()
                ? Collections.emptyList()
                : F.mapNonNullsToList(list, ShootingTestOfficialOccupationDTO::create);

        if (!officialOccupationDTOs.isEmpty()) {
            officialOccupationDTOs.get(0).setShootingTestResponsible(Boolean.TRUE);
        }

        return officialOccupationDTOs;
    }
}
