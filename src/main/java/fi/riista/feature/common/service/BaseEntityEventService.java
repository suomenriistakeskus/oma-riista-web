package fi.riista.feature.common.service;

import com.querydsl.core.Tuple;
import com.querydsl.core.group.Group;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.common.dto.BaseEntityEventDTO;
import fi.riista.feature.common.entity.BaseEntityEvent;
import fi.riista.feature.organization.person.QPerson;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
public class BaseEntityEventService {

    @Resource
    private JPQLQueryFactory jqplQueryFactory;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public <T extends BaseEntityEvent> BaseEntityEventDTO getBaseEntityEventDTO(@Nonnull final T event) {
        requireNonNull(event);

        final QSystemUser USER = QSystemUser.systemUser;
        final QPerson PERSON = QPerson.person;

        final ComparableExpression<String> firstName = PERSON.byName.coalesce(USER.firstName).getValue();
        final ComparableExpression<String> lastName = PERSON.lastName.coalesce(USER.lastName).getValue();

        final Tuple tuple = jqplQueryFactory
                .select(firstName, lastName)
                .from(USER)
                .leftJoin(USER.person, PERSON)
                .where(USER.id.eq(event.getUserId()))
                .fetchOne();

        return Optional
                .ofNullable(tuple)
                .map(t -> new BaseEntityEventDTO(t.get(firstName), t.get(lastName), event.getEventTime(), event.getId()))
                .orElseGet(() -> new BaseEntityEventDTO(null, null, event.getEventTime(), event.getId()));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public <T extends BaseEntityEvent> List<BaseEntityEventDTO> getBaseEntityEventDTOList(@Nonnull final List<T> events) {
        final QSystemUser USER = QSystemUser.systemUser;
        final QPerson PERSON = QPerson.person;

        final ComparableExpression<String> firstName = PERSON.byName.coalesce(USER.firstName).getValue();
        final ComparableExpression<String> lastName = PERSON.lastName.coalesce(USER.lastName).getValue();

        final List<Long> userIds = events.stream().map(BaseEntityEvent::getUserId).collect(Collectors.toList());

        final Map<Long, Group> userIdToPersons = jqplQueryFactory
                .select(firstName, lastName, USER.id)
                .from(USER)
                .leftJoin(USER.person, PERSON)
                .where(USER.id.in(userIds))
                .transform(GroupBy.groupBy(USER.id).as(firstName, lastName));

        return events.stream()
                .map(e -> {
                    final Group group = userIdToPersons.get(e.getUserId());
                    return new BaseEntityEventDTO(group.getOne(firstName), group.getOne(lastName), e.getEventTime(), e.getId());
                })
                .collect(Collectors.toList());
    }
}
