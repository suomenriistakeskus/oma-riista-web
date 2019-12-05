package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.DateUtil.today;

@Service
public class GameDamageInspectionEventCrudFeature extends AbstractCrudFeature<Long, GameDamageInspectionEvent, GameDamageInspectionEventDTO> {

    @Resource
    private GameDamageInspectionEventRepository eventRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private GameDamageInspectionEventDTOTransformer dtoTransformer;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Override
    protected JpaRepository<GameDamageInspectionEvent, Long> getRepository() {
        return eventRepository;
    }

    @Override
    protected void updateEntity(final GameDamageInspectionEvent entity, final GameDamageInspectionEventDTO dto) {
        final LocalDate today = today();
        final LocalDate date = dto.getDate();
        final LocalTime beginTime = dto.getBeginTime();
        final LocalTime endTime = dto.getEndTime();

        // Events can be created and updated by coordinator only up to the 15th of January next year
        checkArgument(activeUserService.isModeratorOrAdmin() || !(date.getYear() < today.minusDays(15).getYear()),
                "Game damage inspection event too far in the past.");
        checkArgument(endTime.isAfter(beginTime), "End time must be after begin time.");
        checkArgument(!date.isAfter(today), "Date can not be in the future.");

        if (entity.isNew()) {
            entity.setRhy(rhyRepository.getOne(dto.getRhy().getId()));
        }

        entity.setGameSpecies(gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode()));
        entity.setInspectorName(dto.getInspectorName());
        entity.setGeoLocation(dto.getGeoLocation());
        entity.setDate(dto.getDate().toDate());
        entity.setBeginTime(dto.getBeginTime());
        entity.setEndTime(dto.getEndTime());
        entity.setDescription(dto.getDescription());

        entity.setHourlyExpensesUnit(dto.getHourlyExpensesUnit());
        entity.setKilometers(dto.getKilometers());
        entity.setKilometerExpensesUnit(dto.getKilometerExpensesUnit());
        entity.setDailyAllowance(dto.getDailyAllowance());
    }

    @Override
    protected GameDamageInspectionEventDTO toDTO(@Nonnull final GameDamageInspectionEvent event) {
        return dtoTransformer.apply(event);
    }

    @Transactional(readOnly = true)
    public List<GameDamageInspectionEventDTO> listEvents(final Long rhyId, final int year) {
        final Date startTime = DateUtil.toDateNullSafe(new LocalDate(year, 1, 1));
        final Date endTime = DateUtil.toDateNullSafe(new LocalDate(year, 12, 31));

        return dtoTransformer.apply(eventRepository.findByRhyIdAndDateBetweenOrderByDateDesc(rhyId, startTime, endTime));
    }
}
