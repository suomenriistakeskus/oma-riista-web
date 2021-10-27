package fi.riista.feature.organization.rhy.gamedamageinspection;

import com.google.common.base.Preconditions;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.rhy.RhyEventTimeException;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
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
import java.util.Optional;

import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission;

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

    @Resource
    private GameDamageInspectionEventRepository gameDamageInspectionEventRepository;

    @Resource
    private GameDamageInspectionEventDTOTransformer gameDamageInspectionEventDtoTransformer;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GameDamageInspectionKmExpensesService gameDamageInspectionKmExpensesService;

    @Resource
    private PersonLookupService personLookupService;

    @Resource
    private OccupationRepository occupationRepository;

    @Override
    protected JpaRepository<GameDamageInspectionEvent, Long> getRepository() {
        return eventRepository;
    }

    @Override
    protected void updateEntity(final GameDamageInspectionEvent entity, final GameDamageInspectionEventDTO dto) {
        final LocalDate date = dto.getDate();
        final LocalTime beginTime = dto.getBeginTime();
        final LocalTime endTime = dto.getEndTime();

        // Events can be created and updated by coordinator only up to the 15th of March next year
        final LocalDate lastModificationDate = new LocalDate(date.getYear() + 1, 3, 5);
        RhyEventTimeException.assertEventLastModificationTime(lastModificationDate, activeUserService.isModeratorOrAdmin());
        RhyEventTimeException.assertBeginTimeNotAfterEndTime(beginTime, endTime);
        RhyEventTimeException.assertEventNotInFuture(date);

        if (entity.isNew()) {
            entity.setRhy(rhyRepository.getOne(dto.getRhy().getId()));
        } else {
            gameDamageInspectionKmExpensesService.updateGameDamageInspectionKmExpenses(entity, dto.getGameDamageInspectionKmExpenses());
            entity.forceRevisionUpdate();
        }

        entity.setGameSpecies(gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode()));
        entity.setInspectorName(dto.getInspectorName());
        entity.setGeoLocation(dto.getGeoLocation());
        entity.setDate(dto.getDate().toDate());
        entity.setBeginTime(dto.getBeginTime());
        entity.setEndTime(dto.getEndTime());
        entity.setDescription(dto.getDescription());

        entity.setHourlyExpensesUnit(dto.getHourlyExpensesUnit());
        entity.setDailyAllowance(dto.getDailyAllowance());

        entity.setExpensesIncluded(dto.isExpensesIncluded());

        if (dto.getInspector() != null) {
            final Optional<Person> inspectorOpt = personLookupService.findById(dto.getInspector().getId(), true);
            Preconditions.checkState(inspectorOpt.isPresent(), "Unknown person for game damage inspector");

            final Person inspector = inspectorOpt.get();
            assertActiveOccupation(inspector, dto.getDate());

            entity.setInspector(inspector);
        }
    }

    @Override
    protected GameDamageInspectionEventDTO toDTO(@Nonnull final GameDamageInspectionEvent event) {
        return dtoTransformer.apply(event);
    }

    @Override
    protected void afterCreate(final GameDamageInspectionEvent entity, final GameDamageInspectionEventDTO dto) {
        if (entity.getExpensesIncluded()) {
            gameDamageInspectionKmExpensesService.addGameDamageInspectionKmExpenses(entity, dto.getGameDamageInspectionKmExpenses());
        }
    }

    @Transactional(readOnly = true)
    public List<GameDamageInspectionEventDTO> listGameDamageInspectionEvents(final Long rhyId, final int year) {
        final Riistanhoitoyhdistys rhy =
                requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.LIST_GAME_DAMAGE_INSPECTION_EVENTS);
        final Date startTime = DateUtil.toDateNullSafe(new LocalDate(year, 1, 1));
        final Date endTime = DateUtil.toDateNullSafe(new LocalDate(year, 12, 31));

        return gameDamageInspectionEventDtoTransformer.apply(gameDamageInspectionEventRepository.findByRhyAndDateBetweenOrderByDateDesc(rhy, startTime, endTime));
    }

    @Override
    protected void delete(GameDamageInspectionEvent entity) {
        gameDamageInspectionKmExpensesService.deleteGameDamageInspectionKmExpenses(entity);
        super.delete(entity);
    }

    private void assertActiveOccupation(final Person inspector, final LocalDate date) {
        final List<Occupation> occupations =
                occupationRepository.findActiveByPersonAndOccupationTypeAndDate(
                        inspector,
                        OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA,
                        date);
        Preconditions.checkState(!occupations.isEmpty(), "Invalid occupation for game damage inspector");
    }
}
