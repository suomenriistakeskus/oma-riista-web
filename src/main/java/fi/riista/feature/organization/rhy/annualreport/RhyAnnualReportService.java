package fi.riista.feature.organization.rhy.annualreport;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.OrganisationCrudFeature;
import fi.riista.feature.organization.calendar.CalendarEventDTO;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysCoordinatorService;
import fi.riista.feature.organization.rhy.annualstats.AnnualShootingTestStatisticsDTO;
import fi.riista.feature.organization.rhy.annualstats.GameDamageStatistics;
import fi.riista.feature.organization.rhy.annualstats.HunterExamStatisticsDTO;
import fi.riista.feature.organization.rhy.annualstats.HunterExamTrainingStatisticsDTO;
import fi.riista.feature.organization.rhy.annualstats.HunterTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.HuntingControlStatistics;
import fi.riista.feature.organization.rhy.annualstats.JHTTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.LukeStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherHunterTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsCrudFeature;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsDTO;
import fi.riista.feature.organization.rhy.annualstats.RhyBasicInfoDTO;
import fi.riista.feature.organization.rhy.annualstats.SrvaEventStatistics;
import fi.riista.feature.organization.rhy.annualstats.YouthTrainingStatistics;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import io.vavr.Tuple3;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.xml.bind.JAXBElement;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.MAANOMISTAJIEN_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationBoardRepresentationRole.METSAHALLITUKSEN_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.HALLITUKSEN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.PUHEENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.VARAPUHEENJOHTAJA;
import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static fi.riista.util.NumberUtils.getIntValueOrZero;

@Component
public class RhyAnnualReportService {

    private static final ClassPathResource ANNUAL_REPORT_FI_TEMPLATE = new ClassPathResource("/annual-report-fi.docx");
    private static final ClassPathResource ANNUAL_REPORT_SV_TEMPLATE = new ClassPathResource("/annual-report-sv.docx");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);

    @Resource
    private RhyAnnualStatisticsCrudFeature annualStatisticsCrudFeature;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private RiistanhoitoyhdistysCoordinatorService riistanhoitoyhdistysCoordinatorService;

    @Resource
    private OrganisationCrudFeature organisationCrudFeature;

    @Transactional
    public RhyAnnualReportDTO getOrCreateRhyAnnualReport(final long rhyId, final int year, final Locale locale) {
        final RhyAnnualStatisticsDTO annualStatisticsDTO = annualStatisticsCrudFeature.getOrCreate(rhyId, year);

        final Riistanhoitoyhdistys rhy =
                requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.READ);

        final LocalDate startDate = new LocalDate(year, 1, 1);
        final LocalDate endDate = new LocalDate(year, 12, 31);
        final List<Occupation> occupations =
                occupationRepository.findActiveByOrganisation(rhy, DateUtil.createDateInterval(startDate, endDate));
        final List<OccupationDTO> occupationDTOs = occupations.stream()
                .map(OccupationDTO::createWithPerson)
                .collect(Collectors.toList());

        final List<OccupationDTO> boardChairs = occupationDTOs.stream()
                .filter(occ -> occ.getOccupationType() == PUHEENJOHTAJA).collect(Collectors.toList());
        final List<OccupationDTO> boardViceChairs = occupationDTOs.stream()
                .filter(occ -> occ.getOccupationType() == VARAPUHEENJOHTAJA).collect(Collectors.toList());
        final List<OccupationDTO> boardMembers = occupationDTOs.stream()
                .filter(occ -> occ.getOccupationType() == HALLITUKSEN_JASEN &&
                        occ.getBoardRepresentation() == null).collect(Collectors.toList());
        final List<OccupationDTO> landOwnerRepresentatives = occupationDTOs.stream()
                .filter(occ -> occ.getBoardRepresentation() == MAANOMISTAJIEN_EDUSTAJA)
                .collect(Collectors.toList());
        final List<OccupationDTO> mhRepresentatives =  occupationDTOs.stream()
                .filter(occ -> occ.getBoardRepresentation() == METSAHALLITUKSEN_EDUSTAJA)
                .collect(Collectors.toList());

        final List<OccupationDTO> carnivoreOfficials = occupationDTOs.stream()
                .filter(occ -> occ.getOccupationType() == PETOYHDYSHENKILO).collect(Collectors.toList());

        final List<OccupationDTO> hunterExamTrainingOfficials = occupationDTOs.stream()
                .filter(occ -> occ.getOccupationType() == METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA)
                .collect(Collectors.toList());

        final Person coordinator = riistanhoitoyhdistysCoordinatorService.findCoordinator(rhy);

        final List<CalendarEventDTO> events = organisationCrudFeature.listEventsByYear(rhyId, year);
        final long rhyMeetingCount =
                events.stream().filter(event -> event.getCalendarEventType() == CalendarEventType.VUOSIKOKOUS).count();
        final long boardMeetingCount =
                events.stream().filter(event -> event.getCalendarEventType() == CalendarEventType.RHY_HALLITUKSEN_KOKOUS).count();

        return RhyAnnualReportDTO.create(annualStatisticsDTO,
                boardChairs, boardViceChairs, boardMembers,
                landOwnerRepresentatives,
                mhRepresentatives,
                carnivoreOfficials, hunterExamTrainingOfficials,
                coordinator == null ? "" : coordinator.getFullName(),
                rhyMeetingCount, boardMeetingCount,
                Locales.isSwedish(locale) ? rhy.getNameSwedish() : rhy.getNameFinnish());
    }

    private static Map<String, String> createReplacementVariables(final RhyAnnualReportDTO dto) {
        final Map<String, String> variables = new HashMap<>();

        variables.put("rhyName", dto.getRhyName().toUpperCase());
        // HALLINTO
        variables.put("year", Integer.toString(dto.getAnnualStatistics().getYear()));
        final RhyBasicInfoDTO basicInfo = dto.getAnnualStatistics().getBasicInfo();
        final int rhyArea = getIntValueOrZero(basicInfo.getOperationalLandAreaSize());
        variables.put("rhyArea", Integer.toString(rhyArea));
        final int rhyMemberCount = getIntValueOrZero(basicInfo.getRhyMembers());
        variables.put("members", Integer.toString(rhyMemberCount));
        variables.put("coordinator", dto.getCoordinatorName());
        variables.put("boardMeetings", Long.toString(dto.getBoardMeetingCount()));
        variables.put("rhyMeetings", Long.toString(dto.getRhyMeetingCount()));

        // RIISTANHOITOYHDISTYKSEN JULKISET HALLINTOTEHTÄVÄT
        // 1. Metsästäjätutkinto
        final HunterExamStatisticsDTO hunterExamDTO = dto.getAnnualStatistics().getHunterExams();
        final int hunterExams = Optional
                .ofNullable(hunterExamDTO.getModeratorOverriddenHunterExamEvents())
                .orElseGet(() -> getIntValueOrZero(hunterExamDTO.getHunterExamEvents()));
        variables.put("hunterExams", Integer.toString(hunterExams));
        final int passedHunterExams = getIntValueOrZero(hunterExamDTO.getPassedHunterExams());
        variables.put("passedHunterExams", Integer.toString(passedHunterExams));
        final int hunterExamAttempts = getIntValueOrZero(hunterExamDTO.getFailedHunterExams()) + passedHunterExams;
        variables.put("hunterExamAttempts", Integer.toString(hunterExamAttempts));
        final int hunterExamOfficials = getIntValueOrZero(hunterExamDTO.getHunterExamOfficials());
        variables.put("hunterExamOfficials", Integer.toString(hunterExamOfficials));

        // 2. Ampumakoe
        final AnnualShootingTestStatisticsDTO shootingTestDTO = dto.getAnnualStatistics().getShootingTests();
        final int fireArmEvents = Optional
                .ofNullable(shootingTestDTO.getModeratorOverriddenFirearmTestEvents())
                .orElseGet(() -> getIntValueOrZero(shootingTestDTO.getFirearmTestEvents()));
        variables.put("fireArmEvents", Integer.toString(fireArmEvents));
        final int fireArmAttempts =
                getIntValueOrZero(shootingTestDTO.getAllBearAttempts()) +
                        getIntValueOrZero(shootingTestDTO.getAllMooseAttempts()) +
                        getIntValueOrZero(shootingTestDTO.getAllRoeDeerAttempts());
        variables.put("fireArmAttempts", Integer.toString(fireArmAttempts));
        final int passedFireArmAttempts =
                getIntValueOrZero(shootingTestDTO.getQualifiedBearAttempts()) +
                        getIntValueOrZero(shootingTestDTO.getQualifiedMooseAttempts()) +
                        getIntValueOrZero(shootingTestDTO.getQualifiedRoeDeerAttempts());
        variables.put("passedFireArmAttempts", Integer.toString(passedFireArmAttempts));
        final int bowEvents = Optional
                .ofNullable(shootingTestDTO.getModeratorOverriddenBowTestEvents())
                .orElseGet(() -> getIntValueOrZero(shootingTestDTO.getBowTestEvents()));
        variables.put("bowEvents", Integer.toString(bowEvents));
        final int bowAttempts = getIntValueOrZero(shootingTestDTO.getAllBowAttempts());
        variables.put("bowAttempts", Integer.toString(bowAttempts));
        final int passedBowAttempts = getIntValueOrZero(shootingTestDTO.getQualifiedBowAttempts());
        variables.put("passedBowAttempts", Integer.toString(passedBowAttempts));

        // 3. Riistavahinkolain 25 §:ssä tarkoitettuihin maastotarkastuksiin osallistuminen
        final GameDamageStatistics gameDamage = dto.getAnnualStatistics().getGameDamage();
        final int mooselikeDamageInspectionLocations =
                getIntValueOrZero(gameDamage.getMooselikeDamageInspectionLocations());
        variables.put("mooselikeDamageInspectionLocations", Integer.toString(mooselikeDamageInspectionLocations));
        final int largeCarnivoreDamageInspectionLocations =
                getIntValueOrZero(gameDamage.getLargeCarnivoreDamageInspectionLocations());
        variables.put("largeCarnivoreDamageInspectionLocations", Integer.toString(largeCarnivoreDamageInspectionLocations));
        final int damageInspectionLocations = mooselikeDamageInspectionLocations + largeCarnivoreDamageInspectionLocations;
        variables.put("damageInspectionLocations", Integer.toString(damageInspectionLocations));
        final int gameDamageInspectors = getIntValueOrZero(gameDamage.getGameDamageInspectors());
        variables.put("gameDamageInspectors", Integer.toString(gameDamageInspectors));

        // 4. Metsästyksenvalvonta
        final HuntingControlStatistics huntingControl = dto.getAnnualStatistics().getHuntingControl();
        final int huntingControlEvents = getIntValueOrZero(huntingControl.getHuntingControlEvents());
        variables.put("huntingControlEvents", Integer.toString(huntingControlEvents));
        final int huntingControllers = getIntValueOrZero(huntingControl.getHuntingControllers());
        variables.put("huntingControllers", Integer.toString(huntingControllers));

        // RIISTANHOITOYHDISTYKSEN MUUT TEHTÄVÄT
        // 3. Riistanhoidon, riistaeläinkantojen kestävyyden...
        final LukeStatistics luke = dto.getAnnualStatistics().getLuke();
        final int gameTriangles = getIntValueOrZero(luke.sumOfWinterAndSummerGameTriangles());
        variables.put("gameTriangles", Integer.toString(gameTriangles));
        final int birdCounting = getIntValueOrZero(luke.sumOfWaterBirdCalculationLocations());
        variables.put("birdCounting", Integer.toString(birdCounting));
        final List<OccupationDTO> carnivoreOfficials = dto.getCarnivoreOfficials();
        variables.put("carnivoreOfficials", Integer.toString(carnivoreOfficials.size()));
        final int carnivoreDNACollectors = getIntValueOrZero(luke.getCarnivoreDnaCollectors());
        variables.put("carnivoreDNACollectors", Integer.toString(carnivoreDNACollectors));

        // 4. Metsästykseen, riistatalouteen ja riistaeläimiin liittyvien...
        final List<OccupationDTO> hunterExamTrainingOfficials = dto.getHunterExamTrainingOfficials();
        variables.put("hunterExamTrainingOfficials", Integer.toString(hunterExamTrainingOfficials.size()));

        // SUURRIISTAVIRKA-APU, SRVA
        final SrvaEventStatistics srvaEvent = dto.getAnnualStatistics().getSrva();
        final int srvaEvents = getIntValueOrZero(srvaEvent.countAllSrvaEvents());
        variables.put("SRVAEvents", Integer.toString(srvaEvents));
        final int srvaHours = getIntValueOrZero(srvaEvent.getTotalSrvaWorkHours());
        variables.put("SRVAHours", Integer.toString(srvaHours));
        final int srvaPersonnel = getIntValueOrZero(srvaEvent.getSrvaParticipants());
        variables.put("SRVAPersonnel", Integer.toString(srvaPersonnel));
        final int srvaTrafficAccidents = getIntValueOrZero(srvaEvent.getTrafficAccidents());
        final int srvaRailwayAccidents = getIntValueOrZero(srvaEvent.getRailwayAccidents());
        final int srvaOtherAccidents = getIntValueOrZero(srvaEvent.getOtherAccidents());
        variables.put("SRVAAccidents", Integer.toString(srvaTrafficAccidents + srvaRailwayAccidents + srvaOtherAccidents));
        final int srvaDeportations = getIntValueOrZero(srvaEvent.getDeportation().countAll());
        variables.put("SRVADeportations", Integer.toString(srvaDeportations));
        final int srvaInjuries = getIntValueOrZero(srvaEvent.getInjury().countAll());
        variables.put("SRVAInjury", Integer.toString(srvaInjuries));

        return variables;
    }

    public byte[] createAnnualReport(final RhyAnnualReportDTO dto, final Locale locale, final EnumLocaliser localiser) {
        WordprocessingMLPackage wordprocessingMLPackage;
        try {
            wordprocessingMLPackage = WordprocessingMLPackage.load(Locales.isSwedish(locale) ?
                    ANNUAL_REPORT_SV_TEMPLATE.getInputStream() :
                    ANNUAL_REPORT_FI_TEMPLATE.getInputStream());

            final MainDocumentPart main = wordprocessingMLPackage.getMainDocumentPart();

            // VARIABLES
            VariablePrepare.prepare(wordprocessingMLPackage);
            final Map<String, String> variables = createReplacementVariables(dto);
            main.variableReplace(variables);

            // TABLES

            // Rhy board
            final List<String> rhyChairPlaceholders = Arrays.asList("RHY_PJ", "RHY_PJ_VARA", "RHY_PJ_KAUSI");
            final List<Map<String, String>> rhyChairs =
                    createBoardTableVariables(dto.getBoardChairs(), rhyChairPlaceholders, localiser);
            replaceTable(rhyChairPlaceholders, rhyChairs, wordprocessingMLPackage);

            final List<String> rhyViceChairPlaceholders = Arrays.asList("RHY_VARAPJ", "RHY_VARAPJ_VARA", "RHY_VARAPJ_KAUSI");
            final List<Map<String, String>> rhyViceChairs =
                    createBoardTableVariables(dto.getBoardViceChairs(), rhyViceChairPlaceholders, localiser);
            replaceTable(rhyViceChairPlaceholders, rhyViceChairs, wordprocessingMLPackage);

            final List<String> rhyBoardPlaceholders = Arrays.asList("RHY_JASEN", "RHY_JASEN_VARA", "RHY_JASEN_KAUSI");
            final List<Map<String, String>> boardMembers =
                    createBoardTableVariables(dto.getBoardMembers(), rhyBoardPlaceholders, localiser);
            replaceTable(rhyBoardPlaceholders, boardMembers, wordprocessingMLPackage);

            // Rhy board representations
            final List<String> landOwnerPlaceholders = Arrays.asList("MO", "MO_VARA", "MO_KAUSI");
            final List<Map<String, String>> landOwnerRepresentativeMap =
                    createBoardTableVariables(dto.getLandOwnerRepresentatives(), landOwnerPlaceholders, localiser);
            replaceTable(landOwnerPlaceholders, landOwnerRepresentativeMap, wordprocessingMLPackage);

            final List<String> mhPlaceholders = Arrays.asList("MH", "MH_VARA", "MH_KAUSI");
            final List<Map<String, String>> mhRepresentativeMap =
                    createBoardTableVariables(dto.getMhRepresentatives(), mhPlaceholders, localiser);
            replaceTable(mhPlaceholders, mhRepresentativeMap, wordprocessingMLPackage);

            // Carnivore officials
            final List<String> carnivoreOfficialPlaceholders = Arrays.asList("PETOYHDYSHENKILO");
            final List<Map<String, String>> carnivoreOfficialTableVariables = new ArrayList<>();
            final List<OccupationDTO> carnivoreOfficials = dto.getCarnivoreOfficials();
            carnivoreOfficials.forEach(official -> {
                final PersonContactInfoDTO person = official.getPerson();
                final Map<String, String> carnivoreOfficialMap = new HashMap<>();
                if (person != null) {
                    final String personName = person.getFirstName() + " " + person.getLastName();
                    carnivoreOfficialMap.put(carnivoreOfficialPlaceholders.get(0), personName);
                } else {
                    carnivoreOfficialMap.put(carnivoreOfficialPlaceholders.get(0), "");
                }
                carnivoreOfficialTableVariables.add(carnivoreOfficialMap);
            });
            replaceTable(carnivoreOfficialPlaceholders, carnivoreOfficialTableVariables, wordprocessingMLPackage);

            // Trainings
            final List<String> trainingPlaceholders = Arrays.asList("KOULUTUS", "TAPAHTUMA_LKM", "OSALLISTUJA_LKM");
            final List<Tuple3<String, Integer, Integer>> trainingList = createTrainingList(dto, localiser);
            final List<Map<String, String>> trainingTableVariables = createTrainingTableVariables(trainingList, trainingPlaceholders);

            if (trainingTableVariables.size() > 0) {
                // Add table header row if there are events
                final Map<String, String> headerMap = new HashMap<>();
                headerMap.put(trainingPlaceholders.get(0), "");
                headerMap.put(trainingPlaceholders.get(1), localiser.getTranslation("RhyAnnualReport.events"));
                headerMap.put(trainingPlaceholders.get(2), localiser.getTranslation("RhyAnnualReport.participants"));

                trainingTableVariables.add(0, headerMap);
            }
            replaceTable(trainingPlaceholders, trainingTableVariables, wordprocessingMLPackage);

        } catch (Exception e) {
            throw new RuntimeException("Unable to open word template", e);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            wordprocessingMLPackage.save(outputStream);
        } catch (Exception e) {
            throw new RuntimeException("Unable to save annual report");
        }

        return outputStream.toByteArray();
    }

    private List<Object> getAllElementFromObject(final Object searchFrom, final Class<?> toSearch) {
        final List<Object> result = new ArrayList<>();
        Object tempSearchFrom = searchFrom;
        if (searchFrom instanceof JAXBElement) {
            tempSearchFrom = ((JAXBElement<?>) searchFrom).getValue();
        }

        if (tempSearchFrom.getClass().equals(toSearch)) {
            result.add(tempSearchFrom);

        } else if (tempSearchFrom instanceof ContentAccessor) {
            final List<?> children = ((ContentAccessor) tempSearchFrom).getContent();
            for (Object child : children) {
                result.addAll(getAllElementFromObject(child, toSearch));
            }

        }
        return result;
    }

    private void replaceTable(final List<String> placeholders,
                              final List<Map<String, String>> textToAdd,
                              final WordprocessingMLPackage wordprocessingMLPackage) {
        final List<Object> tables = getAllElementFromObject(wordprocessingMLPackage.getMainDocumentPart(), Tbl.class);
        final Tbl tempTable = (Tbl) getTemplateObject(tables, placeholders.get(0));

        final List<Object> rows = getAllElementFromObject(tempTable, Tr.class);
        final Tr tempRow = (Tr) getTemplateObject(rows, placeholders.get(0));

        textToAdd.forEach(text -> addRowToTable(tempTable, tempRow, text));

        tempTable.getContent().remove(tempRow);
    }

    private Object getTemplateObject(final List<Object> objects, final String templateKey) {
        for (Object obj : objects) {
            final List<?> textElements = getAllElementFromObject(obj, Text.class);
            for (Object text : textElements) {
                final Text textElement = (Text) text;
                if (textElement.getValue() != null && textElement.getValue().equals(templateKey)) {
                    return obj;
                }
            }
        }
        return null;
    }

    private void addRowToTable(final Tbl templateTable, final Tr templateRow, final Map<String, String> textToAdd) {
        final Tr workingRow = XmlUtils.deepCopy(templateRow);
        final List<Object> textElements = getAllElementFromObject(workingRow, Text.class);
        textElements.forEach(object -> {
            final Text text = (Text) object;
            final String replacementValue = textToAdd.get(text.getValue());
            if (replacementValue != null) {
                text.setValue(replacementValue);
            }
        });

        templateTable.getContent().add(workingRow);
    }

    private static List<Map<String, String>> createBoardTableVariables(final List<OccupationDTO> members,
                                                                       final List<String> placeholders,
                                                                       final EnumLocaliser localiser) {
        final List<Map<String, String>> memberVariables = new ArrayList<>();

        members.forEach(member -> {
            final Map<String, String> memberMap = new HashMap<>();
            final PersonContactInfoDTO person = member.getPerson();
            final String memberName = person.getFirstName() + " " + person.getLastName();
            final PersonContactInfoDTO substitute = member.getSubstitute();
            final String substituteName = substitute != null ?
                    substitute.getFirstName() + " " + substitute.getLastName() :
                    "";

            final LocalDate beginDate = member.getBeginDate();
            final LocalDate endDate = member.getEndDate();
            String occupationPeriod = "";

            if (beginDate == null && endDate == null) {
                occupationPeriod = localiser.getTranslation("RhyAnnualReport.indefinitely");
            } else {
                if (beginDate != null && endDate != null) {
                    occupationPeriod = DATE_FORMATTER.print(beginDate) + " - " + DATE_FORMATTER.print(endDate);
                } else if (beginDate != null) {
                    occupationPeriod = localiser.getTranslation("RhyAnnualReport.starting") + " " + DATE_FORMATTER.print(beginDate);
                } else if (endDate != null) {
                    occupationPeriod = localiser.getTranslation("RhyAnnualReport.ending") + " " + DATE_FORMATTER.print(endDate);
                }
            }

            memberMap.put(placeholders.get(0), memberName);
            memberMap.put(placeholders.get(1), substituteName);
            memberMap.put(placeholders.get(2), occupationPeriod);

            memberVariables.add(memberMap);
        });

        return memberVariables;
    }

    private static List<Map<String, String>> createTrainingTableVariables(final List<Tuple3<String, Integer, Integer>> trainings,
                                                                          final List<String> trainingPlaceHolders) {
        final List<Map<String, String>> trainingVariables = new ArrayList<>();

        trainings.forEach(training -> {
            final Map<String, String> trainingMap = new HashMap<>();
            trainingMap.put(trainingPlaceHolders.get(0), training._1);
            trainingMap.put(trainingPlaceHolders.get(1), training._2.toString());
            trainingMap.put(trainingPlaceHolders.get(2), training._3.toString());

            trainingVariables.add(trainingMap);
        });

        return trainingVariables;
    }

    private static void addIfEvent(final String placeholder,
                                   final Integer eventCount,
                                   final Integer participantCount,
                                   final List<Tuple3<String, Integer, Integer>> list) {
        final int events = getIntValueOrZero(eventCount);
        if (events > 0) {
            final int participants = getIntValueOrZero(participantCount);
            list.add(new Tuple3<>(placeholder, events, participants));
        }
    }

    private static List<Tuple3<String, Integer, Integer>> createTrainingList(final RhyAnnualReportDTO dto,
                                                                             final EnumLocaliser localiser) {
        final List<Tuple3<String, Integer, Integer>> trainingList = new ArrayList<>();

        final HunterExamTrainingStatisticsDTO hunterExamTrainingDTO = dto.getAnnualStatistics().getHunterExamTraining();
        final Integer hunterExamTrainingEvents = Optional
                .ofNullable(hunterExamTrainingDTO.getModeratorOverriddenHunterExamTrainingEvents())
                .orElseGet(() -> getIntValueOrZero(hunterExamTrainingDTO.getHunterExamTrainingEvents()));
        addIfEvent(localiser.getTranslation("RhyAnnualReport.hunterExamTrainings"), hunterExamTrainingEvents, hunterExamTrainingDTO.getHunterExamTrainingParticipants(), trainingList);

        final HunterTrainingStatistics hunterTraining = dto.getAnnualStatistics().getHunterTraining();
        addIfEvent(localiser.getTranslation("RhyAnnualReport.mooselikeHuntingLeaderTrainings"),
                hunterTraining.getMooselikeHuntingLeaderTrainingEvents(),
                hunterTraining.getMooselikeHuntingLeaderTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.carnivoreHuntingLeaderTrainings"),
                hunterTraining.getCarnivoreHuntingLeaderTrainingEvents(),
                hunterTraining.getCarnivoreHuntingLeaderTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.mooselikeHuntingTrainings"),
                hunterTraining.getMooselikeHuntingTrainingEvents(),
                hunterTraining.getMooselikeHuntingTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.carnivoreHuntingTrainings"),
                hunterTraining.getCarnivoreHuntingTrainingEvents(),
                hunterTraining.getCarnivoreHuntingTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.srvaTrainings"),
                hunterTraining.getSrvaTrainingEvents(),
                hunterTraining.getSrvaTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.carnivoreContactPersonTrainings"),
                hunterTraining.getCarnivoreContactPersonTrainingEvents(),
                hunterTraining.getCarnivoreContactPersonTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.accidentPreventionTrainings"),
                hunterTraining.getAccidentPreventionTrainingEvents(),
                hunterTraining.getAccidentPreventionTrainingParticipants(),
                trainingList);

        final YouthTrainingStatistics youthTraining = dto.getAnnualStatistics().getYouthTraining();
        addIfEvent(localiser.getTranslation("RhyAnnualReport.schoolTrainings"),
                youthTraining.getSchoolTrainingEvents(),
                youthTraining.getSchoolTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.collegeTrainings"),
                youthTraining.getCollegeTrainingEvents(),
                youthTraining.getCollegeTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.otherYouthTargetedTrainings"),
                youthTraining.getOtherYouthTargetedTrainingEvents(),
                youthTraining.getOtherYouthTargetedTrainingParticipants(),
                trainingList);

        final JHTTrainingStatistics jhtTraining = dto.getAnnualStatistics().getJhtTraining();
        addIfEvent(localiser.getTranslation("RhyAnnualReport.shootingTestTrainings"),
                jhtTraining.getShootingTestTrainingEvents(),
                jhtTraining.getShootingTestTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.hunterExamOfficialTrainings"),
                jhtTraining.getHunterExamOfficialTrainingEvents(),
                jhtTraining.getHunterExamOfficialTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.gameDamageTrainings"),
                jhtTraining.getGameDamageTrainingEvents(),
                jhtTraining.getGameDamageTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.huntingControlTrainings"),
                jhtTraining.getHuntingControlTrainingEvents(),
                jhtTraining.getHuntingControlTrainingParticipants(),
                trainingList);

        final OtherHunterTrainingStatistics otherHunterTraining = dto.getAnnualStatistics().getOtherHunterTraining();
        addIfEvent(localiser.getTranslation("RhyAnnualReport.smallCarnivoreHuntingTrainings"),
                otherHunterTraining.getSmallCarnivoreHuntingTrainingEvents(),
                otherHunterTraining.getSmallCarnivoreHuntingTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.gameCountingTrainings"),
                otherHunterTraining.getGameCountingTrainingEvents(),
                otherHunterTraining.getGameCountingTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.gamePopulationManagementTrainingEvents"),
                otherHunterTraining.getGamePopulationManagementTrainingEvents(),
                otherHunterTraining.getGamePopulationManagementTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.gameEnvironmentalCareTrainings"),
                otherHunterTraining.getGameEnvironmentalCareTrainingEvents(),
                otherHunterTraining.getGameEnvironmentalCareTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.otherGamekeepingTrainings"),
                otherHunterTraining.getOtherGamekeepingTrainingEvents(),
                otherHunterTraining.getOtherGamekeepingTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.shootingTrainingEvents"),
                otherHunterTraining.getShootingTrainingEvents(),
                otherHunterTraining.getShootingTrainingParticipants(),
                trainingList);
        addIfEvent(localiser.getTranslation("RhyAnnualReport.trackerTrainingEvents"),
                otherHunterTraining.getTrackerTrainingEvents(),
                otherHunterTraining.getTrackerTrainingParticipants(),
                trainingList);

        return trainingList;
    }
}
