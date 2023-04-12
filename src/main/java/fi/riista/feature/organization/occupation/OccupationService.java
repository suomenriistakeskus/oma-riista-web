package fi.riista.feature.organization.occupation;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationContactInfoVisibilityRule.VisibilitySetting;
import fi.riista.feature.organization.person.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.organization.occupation.OccupationAuthorization.OccupationPermission.UPDATE_CONTACT_INFO_VISIBILITY;
import static fi.riista.feature.organization.occupation.OccupationContactInfoVisibilityRule.VisibilitySetting.ALWAYS;
import static fi.riista.feature.organization.occupation.OccupationContactInfoVisibilityRule.VisibilitySetting.NEVER;

@Service
public class OccupationService {

    @Resource
    private RequireEntityService requireEntityService;

    @Transactional
    public void updateContactInfoVisibility(final List<OccupationContactInfoVisibilityDTO> dtoList) {
        dtoList.forEach(dto -> {
            final Occupation occupation = requireEntityService.requireOccupation(dto.getId(), UPDATE_CONTACT_INFO_VISIBILITY);
            final Organisation organisation = occupation.getOrganisation();

            assertContactInfoVisibilitySettings(organisation.getOrganisationType(), occupation.getOccupationType(), dto);

            occupation.setNameVisibility(dto.isNameVisibility());
            occupation.setPhoneNumberVisibility(dto.isPhoneNumberVisibility());
            occupation.setEmailVisibility(dto.isEmailVisibility());
        });
    }

    private static void assertContactInfoVisibilitySettings(final OrganisationType organisationType,
                                                            final OccupationType occupationType,
                                                            final OccupationContactInfoVisibilityDTO dto) {
        final OccupationContactInfoVisibilityRule rule =
                OccupationContactInfoVisibilityRuleMapping.get(organisationType, occupationType);

        assertContactInfoVisibilitySetting(dto.isNameVisibility(), rule.getNameVisibility());
        assertContactInfoVisibilitySetting(dto.isPhoneNumberVisibility(), rule.getPhoneNumberVisibility());
        assertContactInfoVisibilitySetting(dto.isEmailVisibility(), rule.getEmailVisibility());
    }

    private static void assertContactInfoVisibilitySetting(final boolean visibility,
                                                           final VisibilitySetting setting) {
        if (visibility && setting == NEVER || !visibility && setting == ALWAYS) {
            throw new IllegalArgumentException("Incorrect contact info visibility");
        }
    }

}
