package fi.riista.feature.shootingtest;

import fi.riista.feature.AbstractCrudFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.DtoUtil.assertNoVersionConflict;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Service
public class ShootingTestAttemptCrudFeature
        extends AbstractCrudFeature<Long, ShootingTestAttempt, ShootingTestAttemptDTO> {

    @Resource
    private ShootingTestAttemptRepository attemptRepository;

    @Resource
    private ShootingTestParticipantRepository participantRepository;

    @Override
    protected JpaRepository<ShootingTestAttempt, Long> getRepository() {
        return attemptRepository;
    }

    @Override
    protected ShootingTestAttemptDTO toDTO(final ShootingTestAttempt attempt) {
        return ShootingTestAttemptDTO.create(attempt, attempt.getParticipant());
    }

    @Override
    protected void updateEntity(final ShootingTestAttempt entity, final ShootingTestAttemptDTO dto) {
        final ShootingTestParticipant participant;

        if (entity.isNew()) {
            participant = participantRepository.getOne(dto.getParticipantId());
            entity.setParticipant(participant);
        } else {
            participant = entity.getParticipant();
            checkArgument(participant.getId().equals(dto.getParticipantId()), "Participant-ID mismatch");
        }

        assertNoVersionConflict(participant, dto.getParticipantRev());
        assertParticipantNotCompleted(participant, "Cannot update shooting test attempt after participant has finished");
        participant.getShootingTestEvent().assertOpen("Cannot create/update shooting test attempt after event was closed");

        entity.setType(dto.getType());
        entity.setResult(dto.getResult());
        entity.setHits(dto.getHits());
        entity.setNote(dto.getNote());

        if (!entity.isNew()) {
            updateTotalDueAmount(participant);
        }
    }

    @Override
    protected void delete(final ShootingTestAttempt entity) {
        final ShootingTestParticipant participant = entity.getParticipant();
        super.delete(entity);
        updateTotalDueAmount(participant);
    }

    @Override
    protected void afterCreate(final ShootingTestAttempt entity, final ShootingTestAttemptDTO dto) {
        updateTotalDueAmount(entity.getParticipant());
    }

    private void updateTotalDueAmount(final ShootingTestParticipant participant) {
        final List<ShootingTestAttempt> chargeableAttempts = attemptRepository.findChargeableByParticipant(participant);

        chargeableAttempts.stream()
                .collect(groupingBy(ShootingTestAttempt::getType, counting()))
                .forEach((type, count) -> {
                    if (count > ShootingTest.MAX_ATTEMPTS_PER_TYPE) {
                        throw new TooManyAttemptsException();
                    }
                });

        participant.updateTotalDueAmount(chargeableAttempts.size());
    }

    private static void assertParticipantNotCompleted(final ShootingTestParticipant participant,
                                                      final String errorMessage) {

        if (participant.isCompleted()) {
            throw new IllegalShootingTestParticipantStateException(errorMessage);
        }
    }
}
