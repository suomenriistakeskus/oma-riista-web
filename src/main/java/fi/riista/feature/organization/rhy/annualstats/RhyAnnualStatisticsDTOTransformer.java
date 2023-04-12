package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.common.service.BaseEntityEventService;
import fi.riista.feature.organization.rhy.annualstats.statechange.RhyAnnualStatisticsStateChangeEventRepository;
import fi.riista.util.DtoUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countAllNonSubsidizableTrainingEvents;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countNonSubsidizableAllTrainingParticipants;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countSubsidizableOtherTrainingEvents;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countSubsidizableStudentAndYouthTrainingEvents;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countSubsidizableTrainingEvents;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsYearDependentCalculations.countSubsidizableTrainingParticipants;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.UNDER_INSPECTION;
import static java.util.Objects.requireNonNull;

@Component
public class RhyAnnualStatisticsDTOTransformer {

    @Resource
    private RhyAnnualStatisticsStateChangeEventRepository stateChangeEventRepository;

    @Resource
    private BaseEntityEventService baseEventService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public RhyAnnualStatisticsDTO transform(@Nonnull final RhyAnnualStatistics entity) {
        requireNonNull(entity);

        final int calendarYear = entity.getYear();

        final RhyAnnualStatisticsDTO dto = new RhyAnnualStatisticsDTO(entity.getRhy().getId(), calendarYear);
        DtoUtil.copyBaseFields(entity, dto);

        final RhyAnnualStatisticsState state = entity.getState();
        dto.setState(state);

        final HunterTrainingStatistics hunterTraining = entity.getOrCreateHunterTraining();

        final YouthTrainingStatistics youthTraining = entity.getOrCreateYouthTraining();

        dto.setBasicInfo(RhyBasicInfoDTO.create(entity.getOrCreateBasicInfo()));
        dto.setHunterExams(HunterExamStatisticsDTO.create(entity.getOrCreateHunterExams()));
        dto.setShootingTests(AnnualShootingTestStatisticsDTO.create(entity.getOrCreateShootingTests()));
        dto.setGameDamage(entity.getOrCreateGameDamage());
        dto.setHuntingControl(entity.getOrCreateHuntingControl());
        dto.setOtherPublicAdmin(entity.getOrCreateOtherPublicAdmin());
        dto.setSrva(entity.getOrCreateSrva());
        dto.setHunterExamTraining(HunterExamTrainingStatisticsDTO.create(entity.getOrCreateHunterExamTraining()));
        dto.setJhtTraining(entity.getOrCreateJhtTraining());
        dto.setHunterTraining(hunterTraining);
        dto.setYouthTraining(youthTraining);
        dto.setOtherHunterTraining(entity.getOrCreateOtherHunterTraining());
        dto.setPublicEvents(entity.getOrCreatePublicEvents());
        dto.setOtherHuntingRelated(entity.getOrCreateOtherHuntingRelated());
        dto.setCommunication(entity.getOrCreateCommunication());
        dto.setShootingRanges(entity.getOrCreateShootingRanges());
        dto.setLuke(entity.getOrCreateLuke());
        dto.setMetsahallitus(entity.getOrCreateMetsahallitus());

        dto.setSubsidizableOtherTrainingEvents(countSubsidizableOtherTrainingEvents(entity));
        dto.setSubsidizableStudentAndYouthTrainingEvents(countSubsidizableStudentAndYouthTrainingEvents(youthTraining));

        dto.setQuantitiesContributingToSubsidyLastModified(entity.getLastModifiedTimeOfQuantitiesContributingToSubsidy());
        dto.setJhtQuantitiesLastModified(entity.getLastModifiedTimeOfJhtQuantities());

        dto.setAllTrainingEvents(countSubsidizableTrainingEvents(entity));
        dto.setAllTrainingParticipants(countSubsidizableTrainingParticipants(entity));
        dto.setAllNonSubsidizableTrainingEvents(countAllNonSubsidizableTrainingEvents(entity));
        dto.setAllNonSubsidizableTrainingParticipants(countNonSubsidizableAllTrainingParticipants(entity));

        dto.setReadyForInspection(entity.isReadyForInspection());
        dto.setMissingParticipants(entity.listMissingParticipants());
        dto.setCompleteForApproval(entity.isCompleteForApproval());

        if (state == UNDER_INSPECTION || state == APPROVED) {
            stateChangeEventRepository
                    .findFirstByStatisticsAndStateOrderByEventTimeDesc(entity, UNDER_INSPECTION)
                    .map(baseEventService::getBaseEntityEventDTO)
                    .ifPresent(dto::setSubmitEvent);
        }

        return dto;
    }
}
