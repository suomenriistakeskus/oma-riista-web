package fi.riista.feature.account.mobile;

import fi.riista.feature.gamediary.mobile.MobileOccupationDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.SortedSet;

public class MobileAccountV1DTO extends MobileAccountDTO {

    public static MobileAccountV1DTO create(
            @Nonnull final String username,
            @Nonnull final Person person,
            @Nonnull final Address address,
            @Nonnull final Riistanhoitoyhdistys rhy,
            @Nonnull final SortedSet<Integer> availableGameDiaryYears,
            @Nonnull final List<MobileOccupationDTO> occupations) {

        // Instead of traversing entity graph here, all the needed entities are
        // lifted up as parameters in order to not introduce hidden N+1 issues.

        final MobileAccountV1DTO dto = new MobileAccountV1DTO();
        dto.populateWith(username, person, address, rhy, availableGameDiaryYears, occupations);
        return dto;
    }

    private MobileAccountV1DTO() {
    }

}
