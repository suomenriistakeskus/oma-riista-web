package fi.riista.feature.huntingclub.members;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitAuthorization;
import fi.riista.feature.huntingclub.members.rhy.RhyClubLeadersExcelView;
import fi.riista.feature.huntingclub.members.rhy.RhyClubOccupationDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.F;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

@Component
public class HuntingClubContactFeature {

    @Resource
    private HuntingClubContactService huntingClubContactService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public List<HuntingClubContactDetailDTO> listClubHuntingLeaders(final long harvestPermitId,
                                                                    final int huntingYear,
                                                                    final int gameSpeciesCode) {
        final GameSpecies species = gameSpeciesService.requireByOfficialCode(gameSpeciesCode);
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId,
                HarvestPermitAuthorization.Permission.LIST_LEADERS);

        return F.mapNonNullsToList(
                huntingClubContactService.listClubHuntingLeaders(harvestPermit, species, huntingYear),
                HuntingClubContactDetailDTO::createForGroup);
    }

    @Transactional(readOnly = true)
    public List<RhyClubOccupationDTO> listRhyContacts(long rhyId) {
        userAuthorizationHelper.assertCoordinatorOrModerator(rhyId);

        return F.mapNonNullsToList(huntingClubContactService.listRhyContactPersons(rhyId),
                RhyClubOccupationDTO::createForClub);
    }

    @Transactional(readOnly = true)
    public List<RhyClubOccupationDTO> listRhyHuntingLeaders(long rhyId, int huntingYear) {
        userAuthorizationHelper.assertCoordinatorOrModerator(rhyId);

        return F.mapNonNullsToList(huntingClubContactService.listRhyHuntingLeaders(rhyId, huntingYear),
                RhyClubOccupationDTO::createForGroup);
    }

    @Transactional(readOnly = true)
    public View exportRhyOccupationsToExcel(final long rhyId, final int year, final Locale locale) {
        userAuthorizationHelper.assertCoordinatorOrModerator(rhyId);

        final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.getOne(rhyId);
        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);
        final List<RhyClubOccupationDTO> contacts = F.mapNonNullsToList(
                huntingClubContactService.listRhyContactPersons(rhyId),
                RhyClubOccupationDTO::createForClub);
        final List<RhyClubOccupationDTO> leaders = F.mapNonNullsToList(
                huntingClubContactService.listRhyHuntingLeaders(rhyId, year),
                RhyClubOccupationDTO::createForGroup);

        return new RhyClubLeadersExcelView(localiser, year, rhy.getNameLocalisation(), contacts, leaders);
    }
}
