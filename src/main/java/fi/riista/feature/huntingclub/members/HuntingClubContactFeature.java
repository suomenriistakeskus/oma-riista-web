package fi.riista.feature.huntingclub.members;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitAuthorization;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.members.rhy.RhyClubLeadersExcelView;
import fi.riista.feature.huntingclub.members.rhy.RhyClubOccupationDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.F;
import io.vavr.Tuple2;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    public PermitHuntingLeaderContactInfoDTO listClubHuntingLeaders(final long harvestPermitId,
                                                                    final int huntingYear,
                                                                    final int gameSpeciesCode) {
        final GameSpecies species = gameSpeciesService.requireByOfficialCode(gameSpeciesCode);
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId,
                HarvestPermitAuthorization.Permission.LIST_LEADERS);

        final Riistanhoitoyhdistys riistanhoitoyhdistys = harvestPermit.getRhy();

        return new PermitHuntingLeaderContactInfoDTO(
                occupationsToDTOs(huntingClubContactService.listClubHuntingLeaders(
                        harvestPermit, species, huntingYear)),
                occupationsToDTOs(huntingClubContactService.listRHYClubHuntingLeaders(
                        riistanhoitoyhdistys, species, huntingYear, harvestPermit)));
    }

    @Transactional(readOnly = true)
    public PermitHuntingLeaderContactInfoDTO listClubHuntingLeadersForContactPerson(final long harvestPermitId,
                                                                                    final int huntingYear,
                                                                                    final int gameSpeciesCode) {
        final GameSpecies species = gameSpeciesService.requireByOfficialCode(gameSpeciesCode);
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId,
                HarvestPermitAuthorization.Permission.LIST_LEADERS_CONTACT_PERSON);

        final Riistanhoitoyhdistys riistanhoitoyhdistys = harvestPermit.getRhy();

        List<HuntingClubContactDetailDTO> ownPermitLeaders =
                huntingClubContactService.listClubAllHuntingLeaders(harvestPermit, species, huntingYear).stream()
                        .map(HuntingClubContactDetailDTO::createOwnLeaderForContactPerson)
                        .collect(Collectors.toList());

        // No special treatment for leaders outside own permit
        final List<HuntingClubContactDetailDTO> otherLeaders =
                occupationsToDTOs(huntingClubContactService.listRHYClubHuntingLeaders(
                        riistanhoitoyhdistys, species, huntingYear, harvestPermit));

        return new PermitHuntingLeaderContactInfoDTO(
                ownPermitLeaders,
                otherLeaders);
    }

    @Transactional(readOnly = true)
    public List<RhyClubOccupationDTO> listRhyContacts(final long rhyId) {
        userAuthorizationHelper.assertCoordinatorOrModerator(rhyId);
        return listOccupationContacts(rhyId);
    }

    @Transactional(readOnly = true)
    public List<RhyClubOccupationDTO> listRhyHuntingLeaders(final long rhyId, final int huntingYear) {
        userAuthorizationHelper.assertCoordinatorOrModerator(rhyId);
        return listLeaderOccupations(rhyId, huntingYear);
    }

    @Transactional(readOnly = true)
    public View exportRhyOccupationsToExcel(final long rhyId, final int huntingYear, final Locale locale) {
        userAuthorizationHelper.assertCoordinatorOrModerator(rhyId);

        final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.getOne(rhyId);
        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);

        final List<RhyClubOccupationDTO> contacts = listOccupationContacts(rhyId);

        final List<RhyClubOccupationDTO> leaders = listLeaderOccupations(rhyId, huntingYear);

        return new RhyClubLeadersExcelView(localiser, huntingYear, rhy.getNameLocalisation(), contacts, leaders);
    }

    private List<RhyClubOccupationDTO> listOccupationContacts(final long rhyId) {
        final List<Occupation> contactOccupations = huntingClubContactService.listRhyContactPersons(rhyId);
        final Map<Occupation, HuntingClub> contactOccupationToClub = huntingClubContactService.getClubOccupationToClub(contactOccupations);
        return contactOccupations.stream()
                .map(occupation -> RhyClubOccupationDTO.createForClub(occupation, contactOccupationToClub.get(occupation)))
                .collect(Collectors.toList());
    }

    private List<RhyClubOccupationDTO> listLeaderOccupations(final long rhyId, final int huntingYear) {
        final List<Occupation> leaderOccupations = huntingClubContactService.listRhyHuntingLeaders(rhyId, huntingYear);
        final Map<Occupation, Tuple2<HuntingClubGroup, HuntingClub>> leaderOccupationToGroupAndClub =
                huntingClubContactService.getLeaderOccupationToGroupAndClub(leaderOccupations);
        return leaderOccupations.stream()
                .map(occupation -> {
                    final Tuple2<HuntingClubGroup, HuntingClub> groupAndClub = leaderOccupationToGroupAndClub.get(occupation);
                    return RhyClubOccupationDTO.createForGroup(occupation, groupAndClub._1, groupAndClub._2);
                })
                .collect(Collectors.toList());
    }

    private List<HuntingClubContactDetailDTO> occupationsToDTOs(List<Occupation> occupations) {
        return F.mapNonNullsToList(occupations, HuntingClubContactDetailDTO::createForGroup);
    }
}
