package fi.riista.feature.shootingtest;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

public class ShootingTestAttemptDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    private long participantId;
    private int participantRev;

    @NotNull
    private ShootingTestType type;

    @NotNull
    private ShootingTestAttemptResult result;

    @Range(min = 0, max = 4)
    private int hits;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String note;

    public static ShootingTestAttemptDTO create(@Nonnull final ShootingTestAttempt attempt,
                                                @Nonnull final ShootingTestParticipant participant) {

        final ShootingTestAttemptDTO dto = new ShootingTestAttemptDTO();
        DtoUtil.copyBaseFields(attempt, dto);

        dto.setParticipantId(participant.getId());
        dto.setParticipantRev(participant.getConsistencyVersion());

        dto.setType(attempt.getType());
        dto.setResult(attempt.getResult());
        dto.setHits(attempt.getHits());
        dto.setNote(attempt.getNote());
        return dto;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(final long participantId) {
        this.participantId = participantId;
    }

    public int getParticipantRev() {
        return participantRev;
    }

    public void setParticipantRev(final int participantRev) {
        this.participantRev = participantRev;
    }

    public ShootingTestType getType() {
        return type;
    }

    public void setType(final ShootingTestType type) {
        this.type = type;
    }

    public ShootingTestAttemptResult getResult() {
        return result;
    }

    public void setResult(final ShootingTestAttemptResult result) {
        this.result = result;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(final int hits) {
        this.hits = hits;
    }

    public String getNote() {
        return note;
    }

    public void setNote(final String note) {
        this.note = note;
    }
}
