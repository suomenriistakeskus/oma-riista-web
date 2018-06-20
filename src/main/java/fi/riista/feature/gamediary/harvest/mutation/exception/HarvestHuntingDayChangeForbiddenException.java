package fi.riista.feature.gamediary.harvest.mutation.exception;

public class HarvestHuntingDayChangeForbiddenException extends IllegalArgumentException {
    public HarvestHuntingDayChangeForbiddenException(final Long currentValue, final Long newValue) {
        super(String.format("Group hunting day change is not allowed currentHuntingDayId=%d newHuntingDayID=%d",
                currentValue, newValue));
    }
}
