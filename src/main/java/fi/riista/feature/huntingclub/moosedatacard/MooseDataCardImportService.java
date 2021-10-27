package fi.riista.feature.huntingclub.moosedatacard;

import com.google.common.collect.Streams;
import fi.riista.config.Constants;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.moosedatacard.converter.MooseDataCardHuntingDayConverter;
import fi.riista.feature.huntingclub.moosedatacard.converter.MooseDataCardLargeCarnivoreObservationConverter;
import fi.riista.feature.huntingclub.moosedatacard.converter.MooseDataCardMooseCalfConverter;
import fi.riista.feature.huntingclub.moosedatacard.converter.MooseDataCardMooseFemaleConverter;
import fi.riista.feature.huntingclub.moosedatacard.converter.MooseDataCardMooseMaleConverter;
import fi.riista.feature.huntingclub.moosedatacard.converter.MooseDataCardMooseObservationConverter;
import fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException;
import fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardValidationException;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardFilenameValidator;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardPage1Validator;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardValidationResult;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardValidator;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCard;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardContainer;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage1;
import fi.riista.util.F;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardValidationException.parsingXmlFileOfMooseDataCardFailed;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Component
public class MooseDataCardImportService {

    private static final Logger LOG = LoggerFactory.getLogger(MooseDataCardImportService.class);

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepo;

    @Resource
    private PersonRepository personRepo;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private MooseDataCardImportRepository importRepo;

    @Resource
    private GameSpeciesService gameSpeciesService;


    @Resource
    private MooseDataCardHuntingSummaryTransferer huntingSummaryTransferer;

    @Resource
    private MooseDataCardImportHelper helper;

    @Resource(name = "mooseDataCardMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    private final MooseDataCardFilenameValidator filenameValidator = new MooseDataCardFilenameValidator();

    private final MooseDataCardPage1Validator page1Validator = new MooseDataCardPage1Validator();

    @Nonnull
    @Transactional(readOnly = true, rollbackFor = MooseDataCardValidationException.class)
    public MooseDataCardValidationResult parseAndValidateMooseDataCard(
            @Nonnull final MultipartFile xmlFile, @Nonnull final MultipartFile pdfFile)
            throws MooseDataCardValidationException {

        Objects.requireNonNull(xmlFile, "xmlFile must not be null");
        Objects.requireNonNull(pdfFile, "pdfFile must not be null");

        final String xmlFileName = xmlFile.getOriginalFilename();
        final String pdfFileName = pdfFile.getOriginalFilename();

        return tryFrom(filenameValidator.validate(xmlFileName, pdfFileName)).flatMapTry(filenameValidation -> {

            return parseMooseDataCard(xmlFile).flatMapTry(mooseDataCard -> {

                final MooseDataCardPage1 page1 = mooseDataCard.getPage1();

                return tryFrom(page1Validator.validate(page1, filenameValidation)).flatMapTry(page1Validation -> {

                    return tryFrom(helper.resolveEntities(page1Validation).flatMap(resolvedEntities -> {

                        LOG.debug("Harvest permit and hunting club successfully resolved by tokens extracted from " +
                                "file name '{}'.", xmlFileName);

                        final HarvestPermitSpeciesAmount speciesAmount = resolvedEntities.speciesAmount;

                        return new MooseDataCardValidator(speciesAmount, page1Validation.clubCoordinates)
                                .validate(mooseDataCard)
                                .map(tuple -> tuple.apply((validatedCard, validationMessages) -> {

                                    return new MooseDataCardValidationResult(
                                            validatedCard, resolvedEntities.getClubOfficialCode(),
                                            page1Validation.clubCoordinates, speciesAmount,
                                            resolvedEntities.huntingYear, resolvedEntities.contactPersonId,
                                            filenameValidation.timestamp, validationMessages);
                                }));
                    }));
                });
            });

        }).getOrElseThrow(exceptionProvider(
                MooseDataCardValidationException.class,
                exception -> {
                    final StringBuilder logMsg = new StringBuilder(
                            String.format("Parsing or validation of moose data card failed with file %s", xmlFileName));

                    filterAndFormatMessagesToLog(exception.getMessages())
                            .ifPresent(userMsg -> logMsg.append(". Output messages:\n").append(userMsg));

                    LOG.info(logMsg.toString());
                },
                throwable -> {

                    // Log and translate unexpected exception.
                    LOG.error("Parsing or validation of moose data card failed unexpectedly with file {}:",
                            xmlFileName, throwable);

                    return MooseDataCardValidationException.internalServerError();
                }));
    }

    private static <T> Try<T> tryFrom(final Validation<List<String>, T> validation) {
        return validation.mapError(MooseDataCardValidationException::of).<Try<T>> fold(Try::failure, Try::success);
    }

    private Try<MooseDataCard> parseMooseDataCard(final MultipartFile xmlFile) {
        final String xmlFileName = xmlFile.getOriginalFilename();

        try (final InputStream is = xmlFile.getInputStream()) {
            LOG.debug("Starting to parse moose data card from file '{}'.", xmlFileName);

            // A hacky way of transforming Swedish version of moose data card into conformance with
            // Finnish schema version.
            final String xmlContent = IOUtils.readLines(is, Constants.DEFAULT_ENCODING).stream()
                    .map(line -> line.replaceAll("Älginformationskort", "Hirvitietokortti"))
                    .collect(joining("\n"));
            final ByteArrayInputStream bais = new ByteArrayInputStream(xmlContent.getBytes(Constants.DEFAULT_ENCODING));
            final MooseDataCard mooseDataCard =
                    ((MooseDataCardContainer) jaxbMarshaller.unmarshal(new StreamSource(bais))).getReport();

            LOG.debug("Parsing of moose data card from file '{}' succeeded.", xmlFileName);
            return Try.success(mooseDataCard);

        } catch (final Exception e) {
            LOG.warn("Parsing of moose data card from file '{}' failed: {}", xmlFileName, e.getMessage());
            return Try.failure(parsingXmlFileOfMooseDataCardFailed(xmlFileName, e));
        }
    }

    @Nonnull
    @Transactional(rollbackFor = MooseDataCardImportException.class)
    public List<String> importMooseDataCard(@Nonnull final MooseDataCardValidationResult input,
                                            @Nonnull final MultipartFile xmlFile,
                                            @Nonnull final MultipartFile pdfFile) throws MooseDataCardImportException {

        Objects.requireNonNull(input, "input is null");
        Objects.requireNonNull(xmlFile, "xmlFile is null");
        Objects.requireNonNull(pdfFile, "pdfFile is null");

        return Try.success(input.mooseDataCard).flatMapTry(mooseDataCard -> {

            final HuntingClub club = huntingClubRepository.findByOfficialCode(input.clubCode);
            final Person contactPerson = personRepo.findById(input.contactPersonId).orElse(null);

            final HarvestPermitSpeciesAmount speciesAmount =
                    speciesAmountRepo.getOne(input.harvestPermitSpeciesAmountId);
            final LocalDate seasonBeginDate = speciesAmount.getFirstDate();

            return helper.assignClubMembershipToPerson(contactPerson, club, seasonBeginDate).flatMapTry(membership -> {

                final GameSpecies mooseSpecies = speciesAmount.getGameSpecies();
                final HarvestPermit permit = speciesAmount.getHarvestPermit();

                final Try<HuntingClubGroup> groupResult = helper.findOrCreateGroupForMooseDataCardImport(
                        club, permit, mooseSpecies, input.huntingYear, contactPerson);

                return groupResult.flatMapTry(group -> {

                    final int huntingYear = input.huntingYear;
                    final GeoLocation defaultLoc = input.clubCoordinates;

                    final Try<Tuple2<List<GroupHuntingDay>, List<String>>> huntingDaysResult =
                            helper.persistHuntingDayData(
                                    group,
                                    createTransientHuntingDays(mooseDataCard, speciesAmount),
                                    createTransientHarvests(mooseDataCard, speciesAmount, contactPerson, defaultLoc),
                                    createTransientObservations(
                                            mooseDataCard, mooseSpecies, contactPerson, huntingYear, defaultLoc));

                    return huntingDaysResult.flatMapTry(t2 -> t2.apply((newHuntingDays, huntingDayMsgs) -> {

                        final List<String> messages = F.concat(input.messages, huntingDayMsgs);

                        return huntingSummaryTransferer
                                .upsertHuntingSummaryData(mooseDataCard, club, permit)
                                .flatMapTry(summary -> helper.saveImportData(
                                        xmlFile, pdfFile, group, newHuntingDays, input.timestamp, messages))
                                .map(importResult -> {

                                    LOG.info("Successfully imported data from moose data card file '{}'.",
                                            xmlFile.getOriginalFilename());

                                    return messages;
                                });
                    }));
                });
            });

        }).getOrElseThrow(exceptionProvider(
                MooseDataCardImportException.class,
                exception -> {
                    final StringBuilder logMsg = new StringBuilder(String.format(
                            "Importing data from moose data card failed with file %s", xmlFile.getOriginalFilename()));

                    filterAndFormatMessagesToLog(exception.getMessages())
                            .ifPresent(userMsg -> logMsg.append(". Output messages:\n").append(userMsg));

                    LOG.info(logMsg.toString());
                },
                throwable -> {

                    // Log and translate unexpected exception.
                    LOG.error("Importing data from moose data card failed unexpectedly with file {}:",
                            xmlFile.getOriginalFilename(), throwable);

                    return MooseDataCardImportException.internalServerError();
                }));
    }

    @Nonnull
    private static <X extends Exception> Function<Throwable, X> exceptionProvider(
            @Nonnull final Class<X> expectedExceptionClass,
            @Nonnull final Consumer<X> expectedExceptionConsumer,
            @Nonnull final Function<Throwable, ? extends X> unexpectedExceptionTranslator) {

        Objects.requireNonNull(expectedExceptionClass, "expectedExceptionClass must not be null");
        Objects.requireNonNull(expectedExceptionConsumer, "expectedExceptionConsumer must not be null");
        Objects.requireNonNull(unexpectedExceptionTranslator, "unexpectedExceptionTranslator must not be null");

        return throwable -> {
            Objects.requireNonNull(throwable);

            if (expectedExceptionClass.isAssignableFrom(throwable.getClass())) {
                final X castException = expectedExceptionClass.cast(throwable);
                expectedExceptionConsumer.accept(castException);
                return castException;
            }

            return unexpectedExceptionTranslator.apply(throwable);
        };
    }

    private static List<GroupHuntingDay> createTransientHuntingDays(final MooseDataCard mooseDataCard,
                                                                    final Has2BeginEndDates permitSeason) {

        final MooseDataCardHuntingDayConverter converter = new MooseDataCardHuntingDayConverter(permitSeason);
        return MooseDataCardExtractor.streamHuntingDays(mooseDataCard).map(converter::convert).collect(toList());
    }

    private static List<Tuple2<Harvest, HarvestSpecimen>> createTransientHarvests(
            final MooseDataCard mooseDataCard,
            final HarvestPermitSpeciesAmount speciesAmount,
            final Person contactPerson,
            final GeoLocation clubCoordinates) {

        final MooseDataCardMooseMaleConverter maleConverter =
                new MooseDataCardMooseMaleConverter(speciesAmount, contactPerson, clubCoordinates);

        final MooseDataCardMooseFemaleConverter femaleConverter =
                new MooseDataCardMooseFemaleConverter(speciesAmount, contactPerson, clubCoordinates);

        final MooseDataCardMooseCalfConverter calfConverter =
                new MooseDataCardMooseCalfConverter(speciesAmount, contactPerson, clubCoordinates);

        return Streams
                .concat(MooseDataCardExtractor.streamMooseMaleHarvests(mooseDataCard).map(maleConverter),
                        MooseDataCardExtractor.streamMooseFemaleHarvests(mooseDataCard).map(femaleConverter),
                        MooseDataCardExtractor.streamMooseCalfHarvests(mooseDataCard).map(calfConverter))
                .collect(toList());
    }

    private List<Observation> createTransientObservations(final MooseDataCard mooseDataCard,
                                                          final GameSpecies mooseSpecies,
                                                          final Person contactPerson,
                                                          final int huntingYear,
                                                          final GeoLocation clubCoordinates) {

        final MooseDataCardMooseObservationConverter mooseConverter =
                new MooseDataCardMooseObservationConverter(mooseSpecies, contactPerson, huntingYear, clubCoordinates);

        final MooseDataCardLargeCarnivoreObservationConverter carnivoreConverter =
                new MooseDataCardLargeCarnivoreObservationConverter(gameSpeciesService, contactPerson, huntingYear, clubCoordinates);

        return Stream
                .concat(MooseDataCardExtractor.streamMooseObservations(mooseDataCard).flatMap(mooseConverter),
                        MooseDataCardExtractor.streamLargeCarnivoreObservations(mooseDataCard).flatMap(carnivoreConverter))
                .collect(toList());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<MooseDataCardImportDTO> getMooseDataCardImports(final HuntingClubGroup huntingGroup) {
        return importRepo.findByGroupOrderByIdAsc(huntingGroup).stream()
                .map(imp -> MooseDataCardImportDTO.from(imp, imp.getMessages()))
                .collect(toList());
    }

    // Do sanitation for messages containing SSNs and cut too long lists.
    private static Optional<String> filterAndFormatMessagesToLog(@Nullable final List<String> msgs) {
        return Optional.ofNullable(msgs).map(list -> {

            final Stream<String> first10 = list.stream()
                    // Remove last 5 characters from SSNs.
                    .map(msg -> msg.replaceAll("([0123]\\d[01]\\d{3}[+-A])\\d{3}\\w", "$1????"))
                    // Add asterisk at beginning of each message.
                    .map("* "::concat)
                    // Do not show more than 10 messages.
                    .limit(10);

            return Stream
                    .concat(first10, list.size() > 10 ? Stream.of("  ...") : Stream.empty())
                    .collect(joining("\n"));
        });
    }
}
