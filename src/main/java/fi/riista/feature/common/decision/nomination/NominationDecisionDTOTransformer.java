package fi.riista.feature.common.decision.nomination;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.decision.ModeratorDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DtoUtil;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Component
public class NominationDecisionDTOTransformer extends ListTransformer<NominationDecision, NominationDecisionDTO> {


    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private ActiveUserService activeUserService;

    @Nonnull
    @Override
    protected List<NominationDecisionDTO> transform(@Nonnull final List<NominationDecision> list) {

        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        checkState(activeUserService.isModeratorOrAdmin());

        final Function<NominationDecision, SystemUser> handlerMapping =
                createDecisionHandlerMapping(list);

        final Function<NominationDecision, Person> contactPersons =
                createDecisionContactPersonMapping(list);

        final Function<NominationDecision, Riistanhoitoyhdistys> rhyMapping = createDecisionRhyMapping(list);

        return list.stream().map(decision -> {

            final OrganisationNameDTO rhy = OrganisationNameDTO.createWithOfficialCode(rhyMapping.apply(decision));

            final SystemUser decisionHandler = handlerMapping.apply(decision);

            final NominationDecisionDTO dto = new NominationDecisionDTO();

            DtoUtil.copyBaseFields(decision, dto);

            dto.setStatus(decision.getStatus());
            dto.setAppealStatus(decision.getAppealStatus());
            dto.setLocale(decision.getLocale());
            dto.setOccupationType(decision.getOccupationType());
            dto.setDecisionType(decision.getDecisionType());
            dto.setDecisionNumber(decision.getDecisionNumber());
            dto.setRhy(rhy);

            ofNullable(decisionHandler)
                    .map(ModeratorDTO::new)
                    .ifPresent(dto::setHandler);

            ofNullable(contactPersons.apply(decision))
                    .map(PersonContactInfoDTO::create)
                    .ifPresent(dto::setContactPerson);

            return dto;

        }).collect(toList());
    }

    private Function<NominationDecision, SystemUser> createDecisionHandlerMapping(
            final List<NominationDecision> decisions) {

        final QNominationDecision DECISION = QNominationDecision.nominationDecision;

        final Map<Long, SystemUser> mapping = jpqlQueryFactory
                .select(DECISION.id, DECISION.handler)
                .from(DECISION)
                .where(DECISION.in(decisions), DECISION.handler.isNotNull())
                .transform(GroupBy.groupBy(DECISION.id).as(DECISION.handler));

        return d -> mapping.get(d.getId());
    }

    private Function<NominationDecision, Person> createDecisionContactPersonMapping(
            final List<NominationDecision> decisions) {
        final QNominationDecision DECISION = QNominationDecision.nominationDecision;

        final Map<Long, Person> mapping = jpqlQueryFactory
                .select(DECISION.id, DECISION.contactPerson)
                .from(DECISION)
                .where(DECISION.in(decisions), DECISION.contactPerson.isNotNull())
                .transform(GroupBy.groupBy(DECISION.id).as(DECISION.contactPerson));

        return d -> mapping.get(d.getId());
    }

    private Function<NominationDecision, Riistanhoitoyhdistys> createDecisionRhyMapping(
            final List<NominationDecision> decisions) {
        final QNominationDecision DECISION = QNominationDecision.nominationDecision;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        final Map<Long, Riistanhoitoyhdistys> mapping = jpqlQueryFactory
                .from(DECISION)
                .innerJoin(DECISION.rhy, RHY)
                .where(DECISION.in(decisions))
                .transform(GroupBy.groupBy(DECISION.id).as(RHY));

        return d -> mapping.get(d.getId());
    }
}
