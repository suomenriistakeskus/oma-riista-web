package fi.riista.feature.shootingtest;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.QUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.REBATED;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class ShootingTestParticipantDTOTransformer
        extends ListTransformer<ShootingTestParticipant, ShootingTestParticipantDTO> {

    @Resource
    private JPAQueryFactory queryFactory;

    private static ShootingTestParticipantDTO create(@Nonnull final ShootingTestParticipant participant,
                                                     @Nonnull final PersonInfo person,
                                                     @Nonnull final List<ShootingTestParticipantDTO.AttemptDTO> attemptDTOs) {

        Objects.requireNonNull(participant);
        Objects.requireNonNull(person);
        Objects.requireNonNull(attemptDTOs);

        final ShootingTestParticipantDTO dto = new ShootingTestParticipantDTO();
        dto.setId(participant.getId());
        dto.setRev(participant.getConsistencyVersion());

        dto.setLastName(person.lastName);
        dto.setFirstName(person.firstName);
        dto.setHunterNumber(person.hunterNumber);

        dto.setMooseTestIntended(participant.isMooseTestIntended());
        dto.setBearTestIntended(participant.isBearTestIntended());
        dto.setDeerTestIntended(participant.isDeerTestIntended());
        dto.setBowTestIntended(participant.isBowTestIntended());

        dto.setAttempts(attemptDTOs);

        dto.setTotalDueAmount(participant.getTotalDueAmountOrZero());
        dto.setPaidAmount(participant.getPaidAmountOrZero());
        dto.setRemainingAmount(participant.getRemainingAmount());

        dto.setRegistrationTime(participant.getRegistrationTime());
        dto.setCompleted(participant.isCompleted());

        return dto;
    }

    @Nonnull
    @Override
    protected List<ShootingTestParticipantDTO> transform(@Nonnull final List<ShootingTestParticipant> list) {
        if (list.isEmpty()) {
            return emptyList();
        }

        final Map<Long, PersonInfo> participantIdToPersonMapping = getParticipantIdToPersonMapping(list);
        final Map<Long, Map<ShootingTestType, Map<ShootingTestAttemptResult, Long>>> allAttempts =
                countAllAttempts(list);

        return list.stream()
                .sorted(comparing(ShootingTestParticipant::getRegistrationTime))
                .map(participant -> {

                    final List<ShootingTestParticipantDTO.AttemptDTO> attempts = allAttempts
                            .computeIfAbsent(participant.getId(), id -> emptyMap())
                            .entrySet()
                            .stream()
                            .map(entry -> {

                                final ShootingTestType type = entry.getKey();
                                final Map<ShootingTestAttemptResult, Long> resultToCount = entry.getValue();

                                final int numberOfChargeableAttempts = resultToCount.entrySet()
                                        .stream()
                                        .mapToInt(e -> e.getKey() == REBATED ? 0 : e.getValue().intValue())
                                        .sum();

                                final boolean qualified = resultToCount.containsKey(QUALIFIED);

                                return new ShootingTestParticipantDTO.AttemptDTO(type, numberOfChargeableAttempts,
                                        qualified);
                            })
                            .sorted(comparing(dto -> dto.getType()))
                            .collect(toList());

                    return create(
                            participant,
                            participantIdToPersonMapping.get(participant.getId()),
                            attempts);
                })
                .collect(toList());
    }

    private Map<Long, PersonInfo> getParticipantIdToPersonMapping(final List<ShootingTestParticipant> participants) {
        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;
        final QPerson PERSON = QPerson.person;

        return queryFactory
                .select(PARTICIPANT.id, PERSON.firstName, PERSON.lastName, PERSON.hunterNumber)
                .from(PARTICIPANT)
                .leftJoin(PARTICIPANT.person, PERSON)
                .where(PARTICIPANT.in(participants))
                .fetch()
                .stream()
                .collect(toMap(
                        t -> t.get(PARTICIPANT.id),
                        t -> new PersonInfo(t.get(PERSON.firstName), t.get(PERSON.lastName),
                                t.get(PERSON.hunterNumber))));
    }

    private Map<Long, Map<ShootingTestType, Map<ShootingTestAttemptResult, Long>>> countAllAttempts(final List<ShootingTestParticipant> participants) {

        final QShootingTestAttempt ATTEMPT = QShootingTestAttempt.shootingTestAttempt;

        return queryFactory
                .select(ATTEMPT.participant.id, ATTEMPT.type, ATTEMPT.result, ATTEMPT.type.count())
                .from(ATTEMPT)
                .where(ATTEMPT.participant.in(participants))
                .groupBy(ATTEMPT.participant.id, ATTEMPT.type, ATTEMPT.result)
                .fetch()
                .stream()
                .collect(groupingBy(
                        t -> t.get(ATTEMPT.participant.id),
                        groupingBy(
                                t -> t.get(ATTEMPT.type),
                                toMap(t -> t.get(ATTEMPT.result), t -> t.get(ATTEMPT.type.count())))));
    }

    private static class PersonInfo {

        private final String firstName;
        private final String lastName;
        private final String hunterNumber;

        public PersonInfo(final String firstName, final String lastName, final String hunterNumber) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.hunterNumber = hunterNumber;
        }
    }
}
