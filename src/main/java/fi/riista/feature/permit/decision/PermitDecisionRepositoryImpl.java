package fi.riista.feature.permit.decision;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.DocumentNumberUtil;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.application.amendment.QAmendmentApplicationData;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Transactional
@Repository
public class PermitDecisionRepositoryImpl implements PermitDecisionRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<String> findCancelledAndIgnoredPermitNumbersByOriginalPermit(
            @Nonnull final HarvestPermit originalPermit) {

        Objects.requireNonNull(originalPermit);

        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QAmendmentApplicationData AMEND_DATA = QAmendmentApplicationData.amendmentApplicationData;

        return jpqlQueryFactory.select(DECISION.decisionYear, DECISION.decisionNumber, DECISION.validityYears)
                .from(AMEND_DATA)
                .join(AMEND_DATA.application, APPLICATION)
                .join(APPLICATION.decision, DECISION)
                .where(AMEND_DATA.originalPermit.eq(originalPermit))
                .where(DECISION.decisionType.in(PermitDecision.DecisionType.CANCEL_APPLICATION, PermitDecision.DecisionType.IGNORE_APPLICATION))
                .where(DECISION.status.eq(DecisionStatus.PUBLISHED))
                .fetch().stream().map(tuple -> DocumentNumberUtil.createDocumentNumber(
                        tuple.get(DECISION.decisionYear),
                        tuple.get(DECISION.validityYears),
                        tuple.get(DECISION.decisionNumber)))
                .collect(Collectors.toList());
    }
}
