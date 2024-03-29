package fi.riista.feature.gamediary.summary;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.observation.Observation_;
import fi.riista.feature.gamediary.observation.QObservation;
import fi.riista.feature.gamediary.srva.QSrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventDTO;
import fi.riista.feature.gamediary.srva.SrvaEventRepository;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysNameDTO;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysNameService;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.riista.feature.gamediary.summary.AdminGameSummaryPredicates.createHarvestPredicate;
import static fi.riista.feature.gamediary.summary.AdminGameSummaryPredicates.createObservationPredicate;
import static fi.riista.feature.gamediary.summary.AdminGameSummaryPredicates.createSrvaPredicate;
import static fi.riista.feature.gamediary.summary.AdminGameSummaryPredicates.organisationPredicate;

@Component
public class AdminGameDiarySummaryExcelFeature {

    private static final Logger LOG = LoggerFactory.getLogger(AdminGameDiarySummaryExcelFeature.class);

    // Arbitrary maximum result row limit (500k) to limit memory consumption
    private static final int PAGE_SIZE = 1000;
    private static final int MAX_PAGES = 500;

    @Resource
    private MessageSource messageSource;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private SrvaEventRepository srvaEventRepository;

    @Resource
    private AdminSummaryHarvestDTOTransformer adminSummaryHarvestDTOTransformer;

    @Resource
    private AdminSummaryObservationDTOTransformer adminSummaryObservationDTOTransformer;

    @Resource
    private AdminSummarySrvaEventDTOTransformer adminSummarySrvaEventDTOTransformer;

    @Resource
    private RiistanhoitoyhdistysNameService riistanhoitoyhdistysNameService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public AdminGameDiarySummaryExcelView export(final AdminGameDiarySummaryRequestDTO dto) {
        final Locale locale = LocaleContextHolder.getLocale();

        final Map<Integer, LocalisedString> species = gameSpeciesService.getNameIndex();
        final Map<Long, RiistanhoitoyhdistysNameDTO> rhyNameMapping = riistanhoitoyhdistysNameService.getNameIndex();

        final GameSpecies gameSpecies = Optional.ofNullable(dto.getSpeciesCode())
                .map(gameSpeciesService::requireByOfficialCode).orElse(null);
        final Interval interval = DateUtil.createDateInterval(dto.getBeginDate(), dto.getEndDate());

        final List<HarvestDTO> harvestDTOList = loadHarvest(createHarvestPredicate(
                interval, gameSpecies, dto.getOrganisationType(),
                dto.getOfficialCode(), dto.isHarvestReportOnly(), dto.isOfficialHarvestOnly()))
                .stream().sorted(Comparator.comparing(HarvestDTO::getPointOfTime)).collect(Collectors.toList());

        final List<ObservationDTO> observationDTOList = loadObservations(
                createObservationPredicate(interval, gameSpecies, dto.getOrganisationType(), dto.getOfficialCode()))
                .stream().sorted(Comparator.comparing(ObservationDTO::getPointOfTime)).collect(Collectors.toList());

        final List<SrvaEventDTO> srvaEventDTOList = loadSrvaEvents(createSrvaPredicate(
                interval, gameSpecies, dto.getOrganisationType(), dto.getOfficialCode()))
                .stream().sorted(Comparator.comparing(SrvaEventDTO::getPointOfTime)).collect(Collectors.toList());

        return new AdminGameDiarySummaryExcelView(
                new EnumLocaliser(messageSource, locale),
                species, rhyNameMapping,
                harvestDTOList, observationDTOList, srvaEventDTOList);
    }


    /*package*/ List<HarvestDTO> loadHarvest(final Predicate predicate) {
        final List<HarvestDTO> result = new LinkedList<>();
        int pageCounter = 0;

        while (true) {
            LOG.info("Loading harvest page {}", pageCounter);

            final PageRequest pageRequest = PageRequest.of(pageCounter++, PAGE_SIZE,
                    JpaSort.of(Sort.Direction.ASC, Harvest_.id));
            final Slice<Harvest> slice = harvestRepository.findAllAsSlice(predicate, pageRequest);

            result.addAll(adminSummaryHarvestDTOTransformer.apply(slice.getContent(), HarvestSpecVersion.MOST_RECENT));

            entityManager.clear();

            if (!slice.hasNext()) {
                break;
            }
            if (pageCounter > MAX_PAGES) {
                LOG.error("Too many result pages {}", pageCounter);
                break;
            }
        }

        return result;
    }

    private List<ObservationDTO> loadObservations(final Predicate predicate) {
        final List<ObservationDTO> result = new LinkedList<>();
        int pageCounter = 0;

        while (true) {
            LOG.info("Loading observation page {}", pageCounter);

            final PageRequest pageRequest = PageRequest.of(pageCounter++, PAGE_SIZE,
                    JpaSort.of(Sort.Direction.ASC, Observation_.id));
            final Slice<Observation> slice = observationRepository.findAllAsSlice(predicate, pageRequest);
            result.addAll(adminSummaryObservationDTOTransformer.apply(slice.getContent()));
            entityManager.clear();

            if (!slice.hasNext()) {
                break;
            }

            if (pageCounter > MAX_PAGES) {
                LOG.error("Too many result pages {}", pageCounter);
                break;
            }
        }

        return result;
    }

    private List<SrvaEventDTO> loadSrvaEvents(final Predicate predicate) {
        final List<SrvaEventDTO> result = new LinkedList<>();
        int pageCounter = 0;

        while (true) {
            LOG.info("Loading SRVA page {}", pageCounter);

            final PageRequest pageRequest = PageRequest.of(pageCounter++, PAGE_SIZE,
                    JpaSort.of(Sort.Direction.ASC, Observation_.id));
            final Slice<SrvaEvent> slice = srvaEventRepository.findAllAsSlice(predicate, pageRequest);
            result.addAll(adminSummarySrvaEventDTOTransformer.apply(slice.getContent()));
            entityManager.clear();

            if (!slice.hasNext()) {
                break;
            }

            if (pageCounter > MAX_PAGES) {
                LOG.error("Too many result pages {}", pageCounter);
                break;
            }
        }

        return result;
    }
}
