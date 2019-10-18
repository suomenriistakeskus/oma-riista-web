package fi.riista.feature.huntingclub.permit.partner;

import fi.riista.feature.harvestpermit.allocation.MoosePermitAllocationDTO;
import fi.riista.feature.harvestpermit.payment.HuntingClubPermitPaymentDTO;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.huntingclub.permit.statistics.HuntingDayStatisticsDTO;
import fi.riista.feature.huntingclub.permit.todo.MoosePermitTodoDTO;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public class HarvestPermitPartnerDTO {
    private final long huntingClubId;
    private final Map<String, String> huntingClubName;
    private final MoosePermitAllocationDTO allocation;
    private final MoosePermitTodoDTO todo;
    private final HarvestCountDTO harvestCount;
    private final HuntingDayStatisticsDTO statistics;
    private final HuntingClubPermitPaymentDTO payment;
    private final ClubHuntingSummaryBasicInfoDTO summary;
    private final List<HarvestPermitPartnerAreaDTO> partnerAreas;

    public HarvestPermitPartnerDTO(final long huntingClubId,
                                   final @Nonnull Map<String, String> huntingClubName,
                                   final @Nonnull MoosePermitAllocationDTO allocation,
                                   final @Nonnull MoosePermitTodoDTO todo,
                                   final @Nonnull HarvestCountDTO harvestCount,
                                   final @Nonnull HuntingClubPermitPaymentDTO payment,
                                   final @Nonnull ClubHuntingSummaryBasicInfoDTO summary,
                                   final List<HarvestPermitPartnerAreaDTO> partnerAreas,
                                   final HuntingDayStatisticsDTO statistics) {
        this.huntingClubId = huntingClubId;
        this.huntingClubName = requireNonNull(huntingClubName);
        this.todo = requireNonNull(todo);
        this.allocation = requireNonNull(allocation);
        this.harvestCount = requireNonNull(harvestCount);
        this.payment = requireNonNull(payment);
        this.summary = requireNonNull(summary);
        this.partnerAreas = partnerAreas != null ? partnerAreas : emptyList();
        this.statistics = statistics != null ? statistics : HuntingDayStatisticsDTO.zeros(huntingClubId);
    }

    public long getHuntingClubId() {
        return huntingClubId;
    }

    public Map<String, String> getHuntingClubName() {
        return huntingClubName;
    }

    public MoosePermitAllocationDTO getAllocation() {
        return allocation;
    }

    public MoosePermitTodoDTO getTodo() {
        return todo;
    }

    public HarvestCountDTO getHarvestCount() {
        return harvestCount;
    }

    public HuntingDayStatisticsDTO getStatistics() {
        return statistics;
    }

    public HuntingClubPermitPaymentDTO getPayment() {
        return payment;
    }

    public ClubHuntingSummaryBasicInfoDTO getSummary() {
        return summary;
    }

    public List<HarvestPermitPartnerAreaDTO> getPartnerAreas() {
        return partnerAreas;
    }
}
