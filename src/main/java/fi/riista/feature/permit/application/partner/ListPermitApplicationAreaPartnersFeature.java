package fi.riista.feature.permit.application.partner;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.huntingclub.members.HuntingClubContactService;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerDTO;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerService;
import fi.riista.security.EntityPermission;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Component
public class ListPermitApplicationAreaPartnersFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestPermitAreaPartnerService harvestPermitAreaPartnerService;

    @Resource
    private HuntingClubContactService huntingClubContactService;

    @Resource
    private MessageSource messageSource;

    @Resource
    private OccupationRepository occupationRepository;

    private HarvestPermitArea requirePermitAreaForRead(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService
                .requireHarvestPermitApplication(applicationId, EntityPermission.READ);
        application.assertHasPermitArea();
        return application.getArea();
    }

    @Transactional(readOnly = true)
    public List<OrganisationNameDTO> listAvailablePartners(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService
                .requireHarvestPermitApplication(applicationId, EntityPermission.READ);

        return occupationRepository.findActiveByPersonAndOrganisationTypes(
                application.getContactPerson(), EnumSet.of(OrganisationType.CLUB)).stream()
                .map(Occupation::getOrganisation)
                .map(OrganisationNameDTO::create)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<HarvestPermitAreaPartnerDTO> listPartners(final long applicationId, final Locale locale) {
        final HarvestPermitArea applicationArea = requirePermitAreaForRead(applicationId);

        return harvestPermitAreaPartnerService.listPartners(applicationArea, locale);
    }

    @Transactional(readOnly = true)
    public PermitApplicationPartnerExcelView listPartnersExcel(final long applicationId, final Locale locale) {
        final HarvestPermitArea applicationArea = requirePermitAreaForRead(applicationId);
        final List<HarvestPermitAreaPartnerDTO> dtoList = harvestPermitAreaPartnerService
                .listPartners(applicationArea, locale);

        return new PermitApplicationPartnerExcelView(new EnumLocaliser(messageSource, locale),
                applicationArea.getExternalId(), dtoList);
    }

    @Transactional(readOnly = true)
    public List<OrganisationNameDTO> listPartnerClubs(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.READ);

        final Map<Long, List<Person>> contactPersonMapping =
                huntingClubContactService.getContactPersonsSorted(application.getPermitPartners());

        return application.getPermitPartners().stream()
                .map(club -> new ApplicationPartnerClubDTO(club,
                        contactPersonMapping.getOrDefault(club.getId(), emptyList())))
                .collect(toList());
    }
}
