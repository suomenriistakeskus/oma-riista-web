package fi.riista.feature.shootingtest;


import com.google.common.base.Preconditions;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.DateUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ShootingTestSummaryExportFeature {

    @Resource
    private ShootingTestFeature shootingTestFeature;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;


    @Transactional(readOnly = true)
    public ShootingTestSummaryDTO getShootingTestSummary(final long eventId) {

        final ShootingTestCalendarEventDTO calendarEvent =
                shootingTestFeature.getCalendarEvent(eventId);

        Preconditions.checkNotNull(
                calendarEvent,
                "Calendar event not found with id {}.",
                eventId);

        final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.
                getOne(calendarEvent.getRhyId());
        final RiistanhoitoyhdistysDTO rhyDTO = new RiistanhoitoyhdistysDTO(rhy);
        final List<ShootingTestParticipantDTO> participants =
                shootingTestFeature.listParticipants(calendarEvent.getShootingTestEventId(), false);
        return ShootingTestSummaryDTO.create(
                calendarEvent,
                rhyDTO,
                participants,
                DateUtil.now());
    }
}
