package fi.riista.feature.permit.application.search;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class HarvestPermitApplicationSearchFeature {
    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private HarvestPermitApplicationSearchResultDTOTransformer harvestPermitApplicationSearchResultDTOTransformer;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationSearchResultDTO> search(final HarvestPermitApplicationSearchDTO dto) {
        userAuthorizationHelper.assertCoordinatorAnywhereOrModerator();

        final List<HarvestPermitApplication> results = harvestPermitApplicationRepository.search(dto);

        return harvestPermitApplicationSearchResultDTOTransformer.apply(results);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationSearchResultDTO> listApplicationsAssignedToMe() {
        final HarvestPermitApplicationSearchDTO dto = new HarvestPermitApplicationSearchDTO();
        dto.setStatus(EnumSet.of(HarvestPermitApplicationSearchDTO.StatusSearch.DRAFT));
        dto.setHandlerId(activeUserService.requireActiveUserId());

        final List<HarvestPermitApplication> results = harvestPermitApplicationRepository.search(dto);

        return harvestPermitApplicationSearchResultDTOTransformer.apply(results);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationSearchResultDTO> listDecisionsAssignedToMe() {
        final List<HarvestPermitApplication> results = harvestPermitApplicationRepository.listByRevisionCreator(activeUserService.requireActiveUserId());

        return harvestPermitApplicationSearchResultDTOTransformer.apply(results);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationSearchResultDTO> listPostalQueue() {
        final List<HarvestPermitApplication> results = harvestPermitApplicationRepository.listPostalQueue();

        return harvestPermitApplicationSearchResultDTOTransformer.apply(results);
    }

    @Transactional(readOnly = true)
    public List<Integer> listRhyYears(final String rhyOfficialCode) {
        userAuthorizationHelper.assertCoordinatorAnywhereOrModerator();

        final HarvestPermitApplicationSearchDTO dto = new HarvestPermitApplicationSearchDTO();
        dto.setRhyOfficialCode(rhyOfficialCode);

        return harvestPermitApplicationRepository.searchYears(dto);
    }

    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationSearchResultDTO> listRhyApplications(
            final String officialCode, final int year, final Integer gameSpeciesCode, final Locale locale) {

        userAuthorizationHelper.assertCoordinatorAnywhereOrModerator();

        final HarvestPermitApplicationSearchDTO dto = new HarvestPermitApplicationSearchDTO();
        dto.setStatus(EnumSet.allOf(HarvestPermitApplicationSearchDTO.StatusSearch.class));
        dto.setRhyOfficialCode(officialCode);
        dto.setHuntingYear(year);
        dto.setGameSpeciesCode(gameSpeciesCode);

        return ImmutableList.sortedCopyOf(createRhyResultOrdering(officialCode, locale), search(dto));
    }

    private static Comparator<HarvestPermitApplicationSearchResultDTO> createRhyResultOrdering(
            final String officialCode, final Locale locale) {
        final Comparator<HarvestPermitApplicationSearchResultDTO> sameRhyFirst = (a, b) -> {
            final int aa = Math.abs(a.getRhy().getOfficialCode().compareTo(officialCode));
            final int bb = Math.abs(b.getRhy().getOfficialCode().compareTo(officialCode));

            return aa - bb;
        };

        final Comparator<HarvestPermitApplicationSearchResultDTO> byPermitHolderName = Comparator.comparing(dto ->
                dto.getPermitHolder() != null
                        ? dto.getPermitHolder().getNameLocalisation().getAnyTranslation(locale)
                        : dto.getContactPerson().getLastName() + dto.getContactPerson().getByName());

        return sameRhyFirst.thenComparing(byPermitHolderName);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationHandlerDTO> listHandlers() {
        return harvestPermitApplicationRepository.listHandlers().stream()
                .map(u -> new HarvestPermitApplicationHandlerDTO(u.getId(), u.getFullName()))
                .collect(Collectors.toList());
    }
}
