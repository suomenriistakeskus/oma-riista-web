package fi.riista.feature.common.decision.nomination;

import fi.riista.api.decision.nomination.NominationDecisionHandlingStatisticsDTO;
import fi.riista.feature.account.user.SystemUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface NominationDecisionRepositoryCustom {

    List<SystemUser> listHandlers();

    Slice<NominationDecision> search(final NominationDecisionSearchDTO dto,
                                     final Pageable pageRequest);

    List<NominationDecision> search(final NominationDecisionSearchDTO dto);

    List<NominationDecisionHandlingStatisticsDTO> getStatistics(final int year);

}
