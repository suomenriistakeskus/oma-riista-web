package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorization;
import fi.riista.util.F;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Component
public class ListPermitApplicationConflictsFeature {

    @Resource
    private HarvestPermitApplicationConflictRepository harvestPermitApplicationConflictRepository;

    @Resource
    private HarvestPermitApplicationConflictPalstaRepository harvestPermitApplicationConflictPalstaRepository;

    @Resource
    private HarvestPermitApplicationConflictBatchRepository harvestPermitApplicationConflictBatchRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationConflictPalstaDTO> listConflictsWithAnotherApplication(
            final long firstApplicationId, final long secondApplicationId) {
        final HarvestPermitApplication firstApplication = requireEntityService.requireHarvestPermitApplication(
                firstApplicationId, HarvestPermitApplicationAuthorization.Permission.LIST_CONFLICTS);

        final HarvestPermitApplication secondApplication = requireEntityService.requireHarvestPermitApplication(
                secondApplicationId, HarvestPermitApplicationAuthorization.Permission.LIST_CONFLICTS);

        final Long batchId = harvestPermitApplicationConflictBatchRepository.findLatestCompletedBatchId();

        if (batchId == null) {
            return emptyList();
        }

        return harvestPermitApplicationConflictPalstaRepository
                .listAll(batchId, firstApplication, secondApplication)
                .stream()
                .map(entity -> new HarvestPermitApplicationConflictPalstaDTO(
                        entity.getPalstaId(),
                        PropertyIdentifier.formatPropertyIdentifier(entity.getPalstaTunnus()),
                        entity.getPalstaNimi(),
                        entity.getConflictAreaSize(),
                        entity.isMetsahallitus()))
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public HarvestPermitApplicationConflictExcelView getConflictsExcel(final long permitApplicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                permitApplicationId, HarvestPermitApplicationAuthorization.Permission.LIST_CONFLICTS);

        return new HarvestPermitApplicationConflictExcelView(
                new EnumLocaliser(messageSource, LocaleContextHolder.getLocale()),
                application.getApplicationNumber(), listConflictsForExcel(application));
    }

    private static boolean isActiveOrAmending(final HarvestPermitApplication application) {
        return application.getStatus() == HarvestPermitApplication.Status.ACTIVE ||
                application.getStatus() == HarvestPermitApplication.Status.AMENDING;
    }

    private List<HarvestPermitApplicationConflictExcelDTO> listConflictsForExcel(final HarvestPermitApplication firstApplication) {
        final Long batchId = harvestPermitApplicationConflictBatchRepository.findLatestCompletedBatchId();

        if (batchId == null) {
            return emptyList();
        }

        final List<HarvestPermitApplication> otherApplicationList = harvestPermitApplicationConflictRepository.listAllConflicting(batchId, firstApplication);

        return harvestPermitApplicationConflictPalstaRepository.listAll(batchId, firstApplication, otherApplicationList).stream()
                .filter(conflictPalsta -> isActiveOrAmending(conflictPalsta.getFirstApplication()))
                .filter(conflictPalsta -> isActiveOrAmending(conflictPalsta.getSecondApplication()))
                .map(conflictPalsta -> {
                    final Person firstContact = conflictPalsta.getFirstApplication().getContactPerson();
                    final Person secondContact = conflictPalsta.getSecondApplication().getContactPerson();

                    return new HarvestPermitApplicationConflictExcelDTO(
                            conflictPalsta.getFirstApplication().getApplicationNumber(),
                            conflictPalsta.getSecondApplication().getApplicationNumber(),
                            HarvestPermitApplicationConflictExcelDTO.contactPerson(firstContact),
                            HarvestPermitApplicationConflictExcelDTO.contactPerson(secondContact),
                            PropertyIdentifier.create(conflictPalsta.getPalstaTunnus()).getDelimitedValue(),
                            conflictPalsta.getPalstaNimi(),
                            conflictPalsta.getConflictAreaSize(),
                            conflictPalsta.isMetsahallitus());
                }).collect(toList());
    }

    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationConflictDTO> listConflicts(final long permitApplicationId) {
        final Long batchId = harvestPermitApplicationConflictBatchRepository.findLatestCompletedBatchId();

        if (batchId == null) {
            return emptyList();
        }

        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                permitApplicationId, HarvestPermitApplicationAuthorization.Permission.LIST_CONFLICTS);
        final List<HarvestPermitApplication> activeConflicting = harvestPermitApplicationConflictRepository.listAllConflicting(batchId, application);
        final Map<Long, ConfictSummaryDTO> summaries = harvestPermitApplicationConflictPalstaRepository.countConflictSummaries(batchId, application, activeConflicting);

        return toDTO(activeConflicting, summaries);
    }

    private static List<HarvestPermitApplicationConflictDTO> toDTO(final List<HarvestPermitApplication> list,
                                                                   final Map<Long, ConfictSummaryDTO> summaries) {

        return F.mapNonNullsToList(list, a -> {
            final ConfictSummaryDTO summary = summaries.get(a.getId());

            // summary might be null if conflicts are not processed yet
            return summary == null
                    ? HarvestPermitApplicationConflictDTO.create(a, a.getHuntingClub(), a.getRhy(),
                    false, false, null)
                    : HarvestPermitApplicationConflictDTO.create(a, a.getHuntingClub(), a.getRhy(),
                    summary.isOnlyPrivateConflicts(), summary.isOnlyMhConflicts(), summary.getConflictSum());

        });
    }
}
