package fi.riista.feature.largecarnivorereport;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventDTO;
import fi.riista.feature.gamediary.srva.SrvaEventRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceased;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedDTO;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedDTOTransformer;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.riista.feature.gamediary.GameSpecies.LARGE_CARNIVORES;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.DEPORTATION;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_BEAR;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_LYNX;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_LYNX_PORONHOITO;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_WOLF;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_WOLF_PORONHOITO;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MAMMAL;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.RESEARCH;
import static fi.riista.util.jpa.CriteriaUtils.singleQueryFunction;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


@Service
public class LargeCarnivoreReportExcelFeature {

    public final static Set<Integer> REPORT_SPECIES = new ImmutableSet.Builder<Integer>()
            .addAll(LARGE_CARNIVORES)
            .add(OFFICIAL_CODE_OTTER)
            .build();

    @Resource
    private HarvestPermitApplicationRepository applicationRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository applicationSpeciesAmountRepository;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitRepository permitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository permitSpeciesAmountRepository;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private SrvaEventRepository srvaEventRepository;

    @Resource
    private OtherwiseDeceasedRepository otherwiseDeceasedRepository;

    @Resource
    private OtherwiseDeceasedDTOTransformer otherwiseDeceasedDTOTransformer;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public LargeCarnivoreExcelExportDTO export(final int huntingYear) {
        // Derogations
        final Map<Integer, List<LargeCarnivorePermitInfoDTO>> derogations = new HashMap<>();
        REPORT_SPECIES.forEach(speciesCode -> {
            final List<LargeCarnivorePermitInfoDTO> derogationInfo = getPermitInfo(MAMMAL, speciesCode, huntingYear);
            derogations.put(speciesCode, derogationInfo);
        });

        // Stock management
        final Map<HarvestPermitCategory, List<LargeCarnivorePermitInfoDTO>> stockManagements = new HashMap<>();
        final List<LargeCarnivorePermitInfoDTO> stockMgmtBear =
                getPermitInfo(LARGE_CARNIVORE_BEAR, OFFICIAL_CODE_BEAR, huntingYear);
        stockManagements.put(LARGE_CARNIVORE_BEAR, stockMgmtBear);

        final List<LargeCarnivorePermitInfoDTO> stockMgmtLynx =
                getPermitInfo(LARGE_CARNIVORE_LYNX, OFFICIAL_CODE_LYNX, huntingYear);
        stockManagements.put(LARGE_CARNIVORE_LYNX, stockMgmtLynx);

        final List<LargeCarnivorePermitInfoDTO> stockMgmtLynxReindeerHusbandry =
                getPermitInfo(LARGE_CARNIVORE_LYNX_PORONHOITO, OFFICIAL_CODE_LYNX, huntingYear);
        stockManagements.put(LARGE_CARNIVORE_LYNX_PORONHOITO, stockMgmtLynxReindeerHusbandry);

        final List<LargeCarnivorePermitInfoDTO> stockMgmtWolf =
                getPermitInfo(LARGE_CARNIVORE_WOLF, OFFICIAL_CODE_WOLF, huntingYear);
        stockManagements.put(LARGE_CARNIVORE_WOLF, stockMgmtWolf);

        final List<LargeCarnivorePermitInfoDTO> stockMgmtWolfReindeerHusbandry =
                getPermitInfo(LARGE_CARNIVORE_WOLF_PORONHOITO, OFFICIAL_CODE_WOLF, huntingYear);
        stockManagements.put(LARGE_CARNIVORE_WOLF_PORONHOITO, stockMgmtWolfReindeerHusbandry);

        // Quota harvests
        final Map<HarvestArea.HarvestAreaDetailedType, Integer> bearQuotaHarvests = harvestRepository.countQuotaHarvestsByArea(OFFICIAL_CODE_BEAR, huntingYear);
        final Map<HarvestArea.HarvestAreaDetailedType, Integer> bearQuotas = harvestRepository.countQuotasByArea(OFFICIAL_CODE_BEAR, huntingYear);

        // Deportations
        final Map<Integer, List<LargeCarnivorePermitInfoDTO>> deportations = new HashMap<>();
        REPORT_SPECIES.forEach(speciesCode -> {
            final List<LargeCarnivorePermitInfoDTO> deporatationInfo =
                    getPermitInfo(DEPORTATION, speciesCode, huntingYear);
            deportations.put(speciesCode, deporatationInfo);
        });

        // Research
        final Map<Integer, List<LargeCarnivorePermitInfoDTO>> research = new HashMap<>();
        REPORT_SPECIES.forEach(speciesCode -> {
            final List<LargeCarnivorePermitInfoDTO> researchInfo =
                    getPermitInfo(RESEARCH, speciesCode, huntingYear);
            research.put(speciesCode, researchInfo);
        });

        // SRVA
        final Map<Integer, List<LargeCarnivoreSrvaEventDTO>> srvaEvents = getSrva(huntingYear);

        // Otherwise deceased
        final Map<Integer, List<LargeCarnivoreOtherwiseDeceasedDTO>> otherwiseDeceased = getOtherwiseDeceased(huntingYear);

        final List<GameSpeciesDTO> species =
                GameSpeciesDTO.transformList(gameSpeciesService.requireByOfficialCodes(new ArrayList<>(REPORT_SPECIES)));

        final Map<Integer, Integer> totalHarvests = new HashMap<>();
        final Map<Integer, Integer> reindeerAreaHarvests = new HashMap<>();

        final Map<Integer, Integer> totalOtherwiseDeceased = new HashMap<>();
        final Map<Integer, Integer> reindeerAreaOtherwiseDeceased = new HashMap<>();

        REPORT_SPECIES.forEach(speciesCode -> {
            // Derogations
            final List<LargeCarnivorePermitInfoDTO> derogationInfos = derogations.get(speciesCode);
            final int totalDerogations = derogationInfos.stream()
                    .map(LargeCarnivorePermitInfoDTO::getHarvests)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue).sum();
            final int reindeerAreaDerogations = derogationInfos.stream()
                    .filter(LargeCarnivorePermitInfoDTO::isOnReindeerArea)
                    .map(LargeCarnivorePermitInfoDTO::getHarvests)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue).sum();

            // Otherwise deceased
            final List<LargeCarnivoreOtherwiseDeceasedDTO> allOtherwiseDeceased = otherwiseDeceased.get(speciesCode);
            if (allOtherwiseDeceased != null ) {
                totalOtherwiseDeceased.put(speciesCode, allOtherwiseDeceased.size());
                reindeerAreaOtherwiseDeceased.put(speciesCode,
                        (int) allOtherwiseDeceased.stream().filter(LargeCarnivoreOtherwiseDeceasedDTO::isReindeerArea).count());
            } else {
                totalOtherwiseDeceased.put(speciesCode, 0);
                reindeerAreaOtherwiseDeceased.put(speciesCode, 0);
            }

            // Stock management
            int reindeerAreaStockMgmt = 0;
            int totalStockMgmt = 0;
            if (speciesCode == OFFICIAL_CODE_BEAR) {
                reindeerAreaStockMgmt = stockMgmtBear.stream()
                        .filter(LargeCarnivorePermitInfoDTO::isOnReindeerArea)
                        .map(LargeCarnivorePermitInfoDTO::getHarvests)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue).sum();
                totalStockMgmt = stockMgmtBear.stream()
                        .map(LargeCarnivorePermitInfoDTO::getHarvests)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue).sum();
            } else if (speciesCode == OFFICIAL_CODE_LYNX) {
                reindeerAreaStockMgmt = stockMgmtLynxReindeerHusbandry.stream()
                        .map(LargeCarnivorePermitInfoDTO::getHarvests)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue).sum();
                totalStockMgmt = stockMgmtLynx.stream()
                        .map(LargeCarnivorePermitInfoDTO::getHarvests)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue).sum() + reindeerAreaStockMgmt;
            }

            totalHarvests.put(speciesCode, totalDerogations + totalStockMgmt);
            reindeerAreaHarvests.put(speciesCode, reindeerAreaDerogations + reindeerAreaStockMgmt);
        });

        return new LargeCarnivoreExcelExportDTO(
                huntingYear,
                species,
                derogations,
                stockManagements,
                bearQuotaHarvests,
                bearQuotas,
                deportations,
                research,
                srvaEvents,
                otherwiseDeceased,
                totalHarvests,
                reindeerAreaHarvests,
                totalOtherwiseDeceased,
                reindeerAreaOtherwiseDeceased);
    }

    // Package private for testing
    /*private*/ List<LargeCarnivorePermitInfoDTO> getPermitInfo(final HarvestPermitCategory category,
                                                                final int speciesCode,
                                                                final int huntingYear) {
        final GameSpecies species = gameSpeciesService.requireByOfficialCode(speciesCode);
        final List<PermitDecision> decisions = permitDecisionRepository.findByHuntingYearAndSpeciesAndCategory(huntingYear, species, category);
        final List<PermitDecisionSpeciesAmount> decisionSpeciesAmounts = permitDecisionSpeciesAmountRepository.findByPermitDecisionIn(decisions);
        final Map<PermitDecision, PermitDecisionSpeciesAmount> decisionToSpeciesAmount =
                F.index(decisionSpeciesAmounts, PermitDecisionSpeciesAmount::getPermitDecision);

        final List<HarvestPermit> permits = permitRepository.findByPermitDecisionIn(decisions);
        final Map<PermitDecision, HarvestPermit> decisionToPermit = F.index(permits, HarvestPermit::getPermitDecision);

        final Function<PermitDecision, HarvestPermitApplication> decisionToApplication =
                singleQueryFunction(decisions, PermitDecision::getApplication, applicationRepository, true);

        final List<HarvestPermitApplication> applications = decisions.stream()
                .map(decisionToApplication)
                .collect(toList());
        final List<HarvestPermitApplication> draftApplications =
                applicationRepository.findNotHandledByHuntingYearAndSpeciesAndCategory(huntingYear, species, category);

        final List<HarvestPermitApplication> allApplications =
                Streams.concat(applications.stream(), draftApplications.stream()).collect(toList());

        final List<Long> reindeerAreaApps = applicationRepository.findApplicationIdsInReindeerArea(allApplications, category);

        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts =
                applicationSpeciesAmountRepository.findByHarvestPermitApplicationIn(allApplications);
        final Map<HarvestPermitApplication, HarvestPermitApplicationSpeciesAmount> applicationSpasByApp = speciesAmounts.stream()
                .collect(toMap(HarvestPermitApplicationSpeciesAmount::getHarvestPermitApplication, spa -> spa));

        final Map<Long, Integer> harvestAmountsByPermitIds =
                harvestRepository.countByHarvestPermitIdAndSpeciesCode(F.getUniqueIds(permits), speciesCode);

        final Function<HarvestPermitApplication, Riistanhoitoyhdistys> applicationToRhy =
                singleQueryFunction(allApplications, HarvestPermitApplication::getRhy, riistanhoitoyhdistysRepository, true);

        final List<Riistanhoitoyhdistys> rhys = allApplications.stream()
                .map(applicationToRhy)
                .collect(toList());
        final Function<Riistanhoitoyhdistys, Organisation> rhyToRka =
                singleQueryFunction(rhys, Riistanhoitoyhdistys::getParentOrganisation, organisationRepository, true);

        final List<LargeCarnivorePermitInfoDTO> permitInfo = decisions.stream()
                .map(decision -> {
                    final HarvestPermit permit = decisionToPermit.get(decision);
                    final HarvestPermitApplication application = decisionToApplication.apply(decision);
                    final HarvestPermitApplicationSpeciesAmount applicationSpa = applicationSpasByApp.get(application);
                    final PermitDecisionSpeciesAmount decisionSpa = decisionToSpeciesAmount.get(decision);
                    final Integer harvests = F.mapNullable(permit, p -> harvestAmountsByPermitIds.get(p.getId()));
                    final Riistanhoitoyhdistys rhy = applicationToRhy.apply(application);
                    final LocalisedString rhyName = LocalisedString.of(rhy.getNameFinnish(), rhy.getNameSwedish());
                    final Organisation rka = rhyToRka.apply(rhy);
                    final LocalisedString rkaName = LocalisedString.of(rka.getNameFinnish(), rka.getNameSwedish());
                    final boolean onReindeerArea = reindeerAreaApps.contains(application.getId());

                    return LargeCarnivorePermitInfoDTO.create(
                            application,
                            applicationSpa,
                            decision,
                            permit,
                            decisionSpa,
                            harvests,
                            rhyName,
                            rkaName,
                            onReindeerArea);
                })
                .collect(toList());

        final List<LargeCarnivorePermitInfoDTO> draftAppPermitInfo = draftApplications.stream()
                .map(application -> {
                    final HarvestPermitApplicationSpeciesAmount spa = applicationSpasByApp.get(application);
                    final Riistanhoitoyhdistys rhy = applicationToRhy.apply(application);
                    final LocalisedString rhyName = LocalisedString.of(rhy.getNameFinnish(), rhy.getNameSwedish());
                    final Organisation rka = rhyToRka.apply(rhy);
                    final LocalisedString rkaName = LocalisedString.of(rka.getNameFinnish(), rka.getNameSwedish());
                    final boolean onReindeerArea = reindeerAreaApps.contains(application.getId());

                    return LargeCarnivorePermitInfoDTO.create(
                            application,
                            spa,
                            null,
                            null,
                            null,
                            null,
                            rhyName,
                            rkaName,
                            onReindeerArea);
                })
                .collect(toList());

        return Streams.concat(permitInfo.stream(), draftAppPermitInfo.stream()).collect(toList());
    }

    /*private*/ Map<Integer, List<LargeCarnivoreSrvaEventDTO>> getSrva(final int huntingYear) {
        final Map<Integer, List<SrvaEvent>> eventMap = srvaEventRepository.findBySpeciesCodeAndPointOfTime(REPORT_SPECIES, huntingYear);

        final List<SrvaEvent> srvaEvents = eventMap.values().stream()
                .flatMap(Collection::stream)
                .collect(toList());
        final Function<SrvaEvent, Riistanhoitoyhdistys> srvaToRhy =
                singleQueryFunction(srvaEvents, SrvaEvent::getRhy, riistanhoitoyhdistysRepository, true);

        final List<Riistanhoitoyhdistys> srvaRhys = srvaEvents.stream()
                .map(event -> srvaToRhy.apply(event))
                .collect(toList());
        final Function<Riistanhoitoyhdistys, Organisation> rhyToRka =
                singleQueryFunction(srvaRhys, Riistanhoitoyhdistys::getParentOrganisation, organisationRepository, true);

        return eventMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entrySet -> entrySet.getValue().stream()
                        .map(e -> {
                            final SrvaEventDTO dto = SrvaEventDTO.create(e);
                            final Riistanhoitoyhdistys rhy = srvaToRhy.apply(e);
                            final RiistanhoitoyhdistysDTO rhyDto = RiistanhoitoyhdistysDTO.create(rhy);
                            final OrganisationNameDTO rkaDto = OrganisationNameDTO.create(rhyToRka.apply(rhy));

                            return LargeCarnivoreSrvaEventDTO.create(dto, rhyDto, rkaDto);
                        })
                        .collect(toList())));
    }

    /*private*/ Map<Integer, List<LargeCarnivoreOtherwiseDeceasedDTO>> getOtherwiseDeceased(final int huntingYear) {
        final DateTime huntingYearStart = DateUtil.toDateTimeNullSafe(DateUtil.huntingYearBeginDate(huntingYear));
        final DateTime huntingYearEnd = DateUtil.toDateTimeNullSafe(DateUtil.huntingYearEndDate(huntingYear));

        final List<OtherwiseDeceased> otherwiseDeceased =
                otherwiseDeceasedRepository.findAllByPointOfTimeBetween(huntingYearStart, huntingYearEnd);
        final List<OtherwiseDeceasedDTO> otherwiseDeceasedDTOS = otherwiseDeceasedDTOTransformer.transform(otherwiseDeceased);

        final List<Long> reindeerAreaOtherwiseDeceasedIds = otherwiseDeceasedRepository.findReindeerAreaLocated(otherwiseDeceased);

        final List<LargeCarnivoreOtherwiseDeceasedDTO> list = otherwiseDeceasedDTOS.stream()
                .map(dto -> new LargeCarnivoreOtherwiseDeceasedDTO(dto, reindeerAreaOtherwiseDeceasedIds.contains(dto.getId())))
                .collect(Collectors.toList());

        return list.stream()
                .collect(groupingBy(dto -> dto.getOtherwiseDeceased().getGameSpeciesCode(), toList()));
    }
}
