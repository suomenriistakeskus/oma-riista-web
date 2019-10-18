package fi.riista.feature.gamediary.search;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameDiaryEntryDTO;
import fi.riista.feature.gamediary.GameDiaryEntry_;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationDTOTransformer;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventDTOTransformer;
import fi.riista.feature.gamediary.srva.SrvaEventRepository;
import fi.riista.feature.gamediary.srva.SrvaSpecs;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.gamediary.GameDiarySpecs.authorButNotObserver;
import static fi.riista.feature.gamediary.GameDiarySpecs.authorButNotShooter;
import static fi.riista.feature.gamediary.GameDiarySpecs.observer;
import static fi.riista.feature.gamediary.GameDiarySpecs.shooter;
import static fi.riista.util.jpa.JpaSpecs.and;
import static fi.riista.util.jpa.JpaSpecs.withinInterval;
import static java.util.Collections.emptyList;

@Component
public class GameDiarySearchFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private SrvaEventRepository srvaEventRepository;

    @Resource
    private HarvestDTOTransformer harvestDtoTransformer;

    @Resource
    private ObservationDTOTransformer observationDtoTransformer;

    @Resource
    private SrvaEventDTOTransformer srvaEventDTOTransformer;

    @Transactional(readOnly = true)
    public List<GameDiaryEntryDTO> listDiaryEntriesForActiveUser(@Nonnull final GameDiarySearchDTO dto) {
        Objects.requireNonNull(dto);

        final Person activePerson = activeUserService.requireActivePerson();

        final List<Harvest> harvests = dto.isIncludeHarvest()
                ? harvestRepository.findAll(harvestSpecification(dto, activePerson))
                : emptyList();

        final List<Observation> observations = dto.isIncludeObservation() && !dto.isOnlyReports() && !dto.isOnlyTodo()
                ? observationRepository.findAll(observationSpecification(dto, activePerson))
                : emptyList();

        final List<SrvaEvent> srvaEvents = dto.isIncludeSrva() && !dto.isOnlyReports() && !dto.isReportedForOthers() && !dto.isOnlyTodo()
                ? srvaEventRepository.findAll(srvaSpecification(dto, activePerson))
                : emptyList();

        return F.concat(
                harvestDtoTransformer.apply(harvests),
                observationDtoTransformer.apply(observations),
                srvaEventDTOTransformer.apply(srvaEvents));
    }

    private static Specification<Harvest> harvestSpecification(@Nonnull final GameDiarySearchDTO dto,
                                                               @Nonnull final Person person) {
        final Specification<Harvest> authorSpec = dto.isReportedForOthers()
                ? authorButNotShooter(person)
                : shooter(person);

        final Specification<Harvest> reportStateSpec;

        if (dto.isOnlyReports()) {
            reportStateSpec = JpaSpecs.isNotNull(Harvest_.harvestReportState);
        } else if (dto.isOnlyTodo()) {
            reportStateSpec = JpaSpecs.and(JpaSpecs.isNull(Harvest_.harvestReportState), JpaSpecs.equal(Harvest_.harvestReportRequired, true));
        } else {
            reportStateSpec = JpaSpecs.conjunction();
        }

        final Specification<Harvest> pointOfTimeSpec = withinInterval(GameDiaryEntry_.pointOfTime, dto.getInterval());

        return and(authorSpec, reportStateSpec, pointOfTimeSpec);
    }

    @Nonnull
    private static Specification<Observation> observationSpecification(final @Nonnull GameDiarySearchDTO dto,
                                                                       final Person person) {
        final Specification<Observation> authorSpec = dto.isReportedForOthers()
                ? authorButNotObserver(person)
                : observer(person);

        final Specification<Observation> pointOfTimeSpec = withinInterval(GameDiaryEntry_.pointOfTime, dto.getInterval());

        return and(authorSpec, pointOfTimeSpec);
    }

    @Nonnull
    private static Specifications<SrvaEvent> srvaSpecification(final @Nonnull GameDiarySearchDTO dto,
                                                               final Person person) {
        return Specifications.where(SrvaSpecs.author(person))
                .and(SrvaSpecs.withinInterval(dto.getInterval()));
    }
}
