package fi.riista.feature.permit.decision;

import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionRepository;
import fi.riista.util.F;
import org.joda.time.LocalDateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.feature.permit.PermitTypeCode.DISABILITY_BASED;
import static fi.riista.feature.permit.PermitTypeCode.DOG_DISTURBANCE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.DOG_UNLEASH_BASED;
import static fi.riista.feature.permit.PermitTypeCode.GAME_MANAGEMENT;
import static fi.riista.feature.permit.PermitTypeCode.IMPORTING;
import static fi.riista.feature.permit.PermitTypeCode.LAW_SECTION_TEN_BASED;
import static fi.riista.feature.permit.PermitTypeCode.MOOSELIKE;
import static fi.riista.util.DateUtil.toLocalDateTimeNullSafe;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
public class PermitDecisionPaymentAmountUpdateFeature {

    @Resource
    private PermitDecisionRepository decisionRepository;

    @Resource
    private PermitDecisionRevisionRepository revisionRepository;

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Set<Long> updateNewPayments() {
        final List<String> permitTypesToBeUpdated = Arrays.asList(
                MOOSELIKE,
                LAW_SECTION_TEN_BASED,
                DISABILITY_BASED,
                DOG_UNLEASH_BASED,
                DOG_DISTURBANCE_BASED,
                IMPORTING,
                GAME_MANAGEMENT);

        final LocalDateTime dateAfter = new LocalDateTime(2021, 12, 31, 23, 59, 59);

        final Set<PermitDecision> decisions =
                decisionRepository.findByTypeCodeAndScheduledPublishingAfter(
                                permitTypesToBeUpdated,
                                dateAfter)
                        .stream().collect(Collectors.toSet());

        if (!decisions.isEmpty()) {
            final List<PermitDecisionRevision> revisions = revisionRepository.findByPermitDecisionIn(decisions);
            final Map<PermitDecision, List<PermitDecisionRevision>> decisionToRevisions = revisions.stream()
                    .collect(groupingBy(PermitDecisionRevision::getPermitDecision, mapping(rev -> rev, toList())));

            // Filter out decisions which have already published revision,
            // i.e. only decisions which haven't been published will be updated
            final Set<PermitDecision> filteredDecisions = decisions.stream()
                            .filter(decision -> {
                                final List<PermitDecisionRevision> revisionList = decisionToRevisions.get(decision);
                                return revisionList.stream()
                                        .map(PermitDecisionRevision::getScheduledPublishDate)
                                        .allMatch(date -> toLocalDateTimeNullSafe(date).isAfter(dateAfter));
                            }).collect(Collectors.toSet());

            filteredDecisions.forEach(decision ->
                    decision.setPaymentAmount(PermitDecisionPaymentAmount.getPaymentAmountForYear(
                            decision.getDecisionType(),
                            decision.getPermitTypeCode(),
                            2022)));

            return F.indexById(filteredDecisions).keySet();
        }

        return Collections.emptySet();
    }
}
