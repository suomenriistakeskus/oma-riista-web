package fi.riista.feature.permit.decision.revision;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.QPersistentFileMetadata;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

import static fi.riista.feature.common.decision.DecisionStatus.PUBLISHED;
import static fi.riista.feature.permit.PermitTypeCode.CARNIVORE_PERMIT_CODES;

@Repository
public class PermitDecisionRevisionRepositoryImpl implements PermitDecisionRevisionRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public Optional<PersistentFileMetadata> findLatestDecisionMetadataForInformationRequest(final int decisionNumber){
        final QPermitDecisionRevision REV = QPermitDecisionRevision.permitDecisionRevision;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QPersistentFileMetadata METADATA = QPersistentFileMetadata.persistentFileMetadata;

        return Optional.ofNullable(jpqlQueryFactory
                .select(METADATA)
                .from(REV)
                .innerJoin(REV.permitDecision, DECISION)
                .innerJoin(REV.pdfMetadata, METADATA)
                .where(DECISION.decisionNumber.eq(decisionNumber))
                .where(DECISION.status.eq(PUBLISHED))
                .orderBy(REV.lockedDate.desc())
                .limit(1)
                .fetchOne());
    }

    @Transactional(readOnly = true)
    public Optional<PersistentFileMetadata> findLatestPublicDecisionPdf(final int decisionNumber){
        final QPermitDecisionRevision REV = QPermitDecisionRevision.permitDecisionRevision;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QPersistentFileMetadata METADATA = QPersistentFileMetadata.persistentFileMetadata;

        return Optional.ofNullable(jpqlQueryFactory
                .select(METADATA)
                .from(REV)
                .innerJoin(REV.permitDecision, DECISION)
                .innerJoin(REV.publicPdfMetadata, METADATA)
                .where(DECISION.decisionNumber.eq(decisionNumber))
                .where(DECISION.permitTypeCode.in(CARNIVORE_PERMIT_CODES))
                .where(DECISION.status.eq(PUBLISHED))
                .orderBy(REV.lockedDate.desc())
                .limit(1)
                .fetchOne());
    }

    @Transactional(readOnly = true)
    public Optional<PermitDecisionRevision> findCarnivoreRevisionWithNoPublicPdf() {
        final QPermitDecisionRevision REV = QPermitDecisionRevision.permitDecisionRevision;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;

        return Optional.ofNullable(jpqlQueryFactory
                .selectFrom(REV)
                .innerJoin(REV.permitDecision, DECISION)
                .where(REV.publicPdfMetadata.isNull())
                .where(DECISION.permitTypeCode.in(CARNIVORE_PERMIT_CODES))
                .where(DECISION.status.eq(PUBLISHED))
                .orderBy(REV.lockedDate.desc())
                .limit(1)
                .fetchOne());

    }
}
