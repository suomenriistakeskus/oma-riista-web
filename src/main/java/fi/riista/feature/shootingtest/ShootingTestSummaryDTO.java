package fi.riista.feature.shootingtest;

import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import org.joda.time.DateTime;

import java.util.List;

public class ShootingTestSummaryDTO {

    private final ShootingTestCalendarEventDTO calendarEventDTO;
    private final List<ShootingTestParticipantDTO> shootingTestParticipantDTOS;
    private final RiistanhoitoyhdistysDTO riistanhoitoyhdistysDTO;
    private final DateTime timestamp;
    private final int totalAttempts;

    public static ShootingTestSummaryDTO create(
            final ShootingTestCalendarEventDTO shootingTestCalendarEventDTO,
            final RiistanhoitoyhdistysDTO riistanhoitoyhdistysDTO,
            final List<ShootingTestParticipantDTO> shootingTestParticipantList,
            final DateTime timestamp
    ) {

        final ShootingTestSummaryDTO dto = new ShootingTestSummaryDTO(
                shootingTestCalendarEventDTO,
                shootingTestParticipantList,
                riistanhoitoyhdistysDTO,
                timestamp);

        return dto;
    }

    public ShootingTestSummaryDTO(
            final ShootingTestCalendarEventDTO calendarEventDTO,
            final List<ShootingTestParticipantDTO> shootingTestParticipantDTOS,
            final RiistanhoitoyhdistysDTO riistanhoitoyhdistysDTO,
            final DateTime timestamp) {
        this.calendarEventDTO = calendarEventDTO;
        this.shootingTestParticipantDTOS = shootingTestParticipantDTOS;
        this.riistanhoitoyhdistysDTO = riistanhoitoyhdistysDTO;
        this.timestamp = timestamp;
        this.totalAttempts = shootingTestParticipantDTOS.stream()
                .flatMap(p -> p.getAttempts().stream())
                .mapToInt(a -> a.getAttemptCount())
                .sum();
    }

    public ShootingTestCalendarEventDTO getCalendarEventDTO() {

        return calendarEventDTO;
    }


    public List<ShootingTestParticipantDTO> getShootingTestParticipantDTOS() {

        return shootingTestParticipantDTOS;
    }


    public RiistanhoitoyhdistysDTO getRiistanhoitoyhdistysDTO() {
        return riistanhoitoyhdistysDTO;
    }


    public int getTotalAttempts() {
        return totalAttempts;
    }


    public DateTime getTimestamp() {

        return timestamp;
    }

    @Override
    public String toString() {
        return "ShootingTestSummaryDTO{" +
                "calendarEventDTO=" + calendarEventDTO +
                '}';
    }
}
