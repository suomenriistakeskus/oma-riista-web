package fi.riista.feature.permit.decision.informationrequest;

import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.permit.decision.DecisionInformationLinkDTO;
import fi.riista.feature.permit.decision.DecisionInformationPublishingDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.sql.SQInformationRequestLink;
import fi.riista.sql.SQInformationRequestLog;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class InformationRequestLinkRepositoryImpl implements InformationRequestLinkRepositoryCustom {

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public DecisionInformationLinkDTO getValidLinkIdByLinkKey(final String linkKey, final PermitDecision decision) {
        final SQInformationRequestLink LINK = SQInformationRequestLink.informationRequestLink;
        final Tuple t = sqlQueryFactory
                .select(LINK.informationRequestLinkId,
                        LINK.recipientName,
                        LINK.recipientEmail,
                        LINK.validUntil,
                        LINK.creationTime,
                        LINK.informationRequestLinkType)
                .from(LINK)
                .where(LINK.permitDecisionId.eq(decision.getId())
                        .and(LINK.linkIdentifier.eq(linkKey))
                        .and(LINK.validUntil.goe(Timestamp.from(Instant.now()))
                        )
                )
                .fetchOne();
        if(t == null) {
            return null;
        }

        return new DecisionInformationLinkDTO(
                t.get(LINK.informationRequestLinkId),
                t.get(LINK.recipientEmail),
                t.get(LINK.recipientName),
                new LocalDateTime(Objects.requireNonNull(t.get(LINK.validUntil)).getTime()),
                new LocalDateTime(Objects.requireNonNull(t.get(LINK.creationTime)).getTime()),
                InformationRequestLinkType.valueOf(t.get(LINK.informationRequestLinkType))
        );
    }

    @Transactional(readOnly = true)
    public List<DecisionInformationPublishingDTO> getDecisionLinkList(final PermitDecision decision) {
        final SQInformationRequestLink LINK = SQInformationRequestLink.informationRequestLink;
        final SQInformationRequestLog LOG = SQInformationRequestLog.informationRequestLog;

        return sqlQueryFactory
                .select(LINK.informationRequestLinkId, LINK.recipientName, LINK.recipientEmail, LINK.validUntil, LINK.creationTime, LINK.informationRequestLinkType, LOG.count())
                .from(LINK)
                .leftJoin(LOG)
                .on(LINK.informationRequestLinkId.eq(LOG.informationRequestLinkId))
                .where(LINK.permitDecisionId.eq(decision.getId())
                        .and(LINK.validUntil.goe(Timestamp.from(Instant.now())))
                )
                .groupBy(LINK.informationRequestLinkId)
                .fetch()
                .stream()
                .map(t -> new DecisionInformationPublishingDTO(
                        t.get(LINK.informationRequestLinkId),
                        t.get(LINK.recipientEmail),
                        t.get(LINK.recipientName),
                        t.get(LOG.count()),
                        new LocalDateTime(Objects.requireNonNull(t.get(LINK.validUntil)).getTime()),
                        new LocalDateTime(Objects.requireNonNull(t.get(LINK.creationTime)).getTime()),
                        InformationRequestLinkType.valueOf(t.get(LINK.informationRequestLinkType))
                ))
                .collect(Collectors.toList());
    }
}
