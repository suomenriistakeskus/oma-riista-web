package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.integration.luke_import.model.v1_0.MooseDataCardClubInfo;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardContactPerson;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage1;
import fi.riista.util.DateUtil;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Validation;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.clubCoordinatesOutOfFinland;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.hunterNumberAndSsnMissingForContactPerson;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingClubCodeMismatchBetweenNameAndContentOfXmlFile;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidClubCode;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidClubCoordinates;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidHunterNumber;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidPermitNumber;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidSsn;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.missingClubCode;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.missingClubCoordinates;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.missingPermitNumber;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.permitNumberMismatchBetweenNameAndContentOfXmlFile;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MooseDataCardPage1ValidatorTest implements ValueGeneratorMixin {

    private static final String CLUB_CODE = "1234567";
    private static final String CLUB_CODE_2 = "12345678";

    private static final String BLANK = "   ";
    private static final String INVALID = "invalid";

    private static final MooseDataCardPage1Validator INSTANCE = new MooseDataCardPage1Validator();

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Test
    public void testUsingSsn() {
        final Tuple2<MooseDataCardPage1, MooseDataCardFilenameValidation> input = newPageAndValidation(false);
        final MooseDataCardPage1 page = input._1;

        final Validation<List<String>, MooseDataCardPage1Validation> validation = validate(input);
        assertTrue(validation.isValid());

        final MooseDataCardPage1Validation output = validation.get();
        assertEquals(page.getPermitNumber(), output.permitNumber);
        assertEquals(page.getHuntingClubCode(), output.clubCode);
        assertTrue(output.hunterNumberOrDateOfBirth.isRight());
        assertEquals(page.getContactPerson().getSsn(), output.hunterNumberOrDateOfBirth.get());
    }

    @Test
    public void testHunterNumberPreferredOverSsn() {
        final Tuple2<MooseDataCardPage1, MooseDataCardFilenameValidation> input = newPageAndValidation(true);
        final MooseDataCardPage1 page = input._1;
        page.getContactPerson().setSsn(ssn());

        final Validation<List<String>, MooseDataCardPage1Validation> validation = validate(input);
        assertTrue(validation.isValid());

        final MooseDataCardPage1Validation output = validation.get();
        assertEquals(page.getPermitNumber(), output.permitNumber);
        assertEquals(page.getHuntingClubCode(), output.clubCode);
        assertTrue(output.hunterNumberOrDateOfBirth.isLeft());
        assertEquals(page.getContactPerson().getHunterNumber(), output.hunterNumberOrDateOfBirth.getLeft());
    }

    @Test
    public void testMissingPermitNumber() {
        createInputAndAssertFailure(page -> page.setPermitNumber(null), missingPermitNumber());
    }

    @Test
    public void testEmptyPermitNumber() {
        createInputAndAssertFailure(page -> page.setPermitNumber(BLANK), missingPermitNumber());
    }

    @Test
    public void testInvalidPermitNumber() {
        createInputAndAssertFailure(page -> page.setPermitNumber(INVALID), invalidPermitNumber(INVALID));
    }

    @Test
    public void testPermitNumberMismatchAgainstFilename() {
        final String newPermitNumber = permitNumber();

        createInputAndAssertFailure(
                page -> page.setPermitNumber(newPermitNumber),
                permitNumberMismatchBetweenNameAndContentOfXmlFile(newPermitNumber));
    }

    @Test
    public void testMissingClubCode() {
        createInputAndAssertFailure(page -> page.setHuntingClubCode(null), missingClubCode());
    }

    @Test
    public void testEmptyClubCode() {
        createInputAndAssertFailure(page -> page.setHuntingClubCode(BLANK), missingClubCode());
    }

    @Test
    public void testInvalidClubCode() {
        createInputAndAssertFailure(page -> page.setHuntingClubCode(INVALID), invalidClubCode(INVALID));
    }

    @Test
    public void testClubCodeMismatchAgainstFilename() {
        createInputAndAssertFailure(
                page -> page.setHuntingClubCode(CLUB_CODE_2),
                huntingClubCodeMismatchBetweenNameAndContentOfXmlFile(CLUB_CODE_2));
    }

    @Test
    public void testMissingClubCoordinates() {
        createInputAndAssertFailure(
                page -> page.getClubInfo().setHuntingClubCoordinate(null), missingClubCoordinates());
    }

    @Test
    public void testEmptyClubCoordinates() {
        createInputAndAssertFailure(
                page -> page.getClubInfo().setHuntingClubCoordinate(BLANK), missingClubCoordinates());
    }

    @Test
    public void testInvalidClubCoordinates() {
        createInputAndAssertFailure(
                page -> page.getClubInfo().setHuntingClubCoordinate(INVALID), invalidClubCoordinates(INVALID));
    }

    @Test
    public void testClubCoordinatesOutOfFinland() {
        createInputAndAssertFailure(
                page -> page.getClubInfo().setHuntingClubCoordinate("0;0"), clubCoordinatesOutOfFinland(0, 0));
    }

    @Test
    public void testInvalidHunterNumber() {
        createInputAndAssertFailure(
                page -> page.getContactPerson().setHunterNumber(INVALID), invalidHunterNumber(INVALID));
    }

    @Test
    public void testInvalidSsn() {
        createInputAndAssertFailure(page -> page.getContactPerson().setSsn(INVALID), invalidSsn());
    }

    @Test
    public void testCombinationOfEmptyOrMissingHunterNumberAndSsn() {
        createInputAndAssertFailure(page -> {
            page.getContactPerson().setHunterNumber(null);
            page.getContactPerson().setSsn(null);
        }, hunterNumberAndSsnMissingForContactPerson());

        createInputAndAssertFailure(page -> {
            page.getContactPerson().setHunterNumber(null);
            page.getContactPerson().setSsn(BLANK);
        }, hunterNumberAndSsnMissingForContactPerson());

        createInputAndAssertFailure(page -> {
            page.getContactPerson().setHunterNumber(BLANK);
            page.getContactPerson().setSsn(null);
        }, hunterNumberAndSsnMissingForContactPerson());

        createInputAndAssertFailure(page -> {
            page.getContactPerson().setHunterNumber(BLANK);
            page.getContactPerson().setSsn(BLANK);
        }, hunterNumberAndSsnMissingForContactPerson());
    }

    @Test
    public void testMultipleErrorsUsingHunterNumber() {
        createInputAndAssertFailure(true, page -> {
            page.getContactPerson().setHunterNumber(INVALID);
            page.setPermitNumber(INVALID);
            page.setHuntingClubCode(INVALID);
            page.getClubInfo().setHuntingClubCoordinate(INVALID);
        }, Arrays.asList(
                invalidHunterNumber(INVALID), invalidPermitNumber(INVALID), invalidClubCode(INVALID),
                invalidClubCoordinates(INVALID)));
    }

    @Test
    public void testMultipleErrorsUsingSsn() {
        createInputAndAssertFailure(false, page -> {
            page.getContactPerson().setSsn(INVALID);
            page.setPermitNumber(INVALID);
            page.setHuntingClubCode(INVALID);
            page.getClubInfo().setHuntingClubCoordinate(INVALID);
        }, Arrays.asList(
                invalidSsn(), invalidPermitNumber(INVALID), invalidClubCode(INVALID), invalidClubCoordinates(INVALID)));
    }

    private void createInputAndAssertFailure(final Consumer<MooseDataCardPage1> consumer,
                                             final String expectedMessage) {

        createInputAndAssertFailure(true, consumer, expectedMessage);
    }

    private void createInputAndAssertFailure(final boolean usingHunterNumber,
                                             final Consumer<MooseDataCardPage1> consumer,
                                             final String expectedMessage) {

        createInputAndAssertFailure(usingHunterNumber, consumer, singletonList(expectedMessage));
    }

    private void createInputAndAssertFailure(final boolean usingHunterNumber,
                                             final Consumer<MooseDataCardPage1> consumer,
                                             final List<String> expectedMessages) {

        final Tuple2<MooseDataCardPage1, MooseDataCardFilenameValidation> tup = newPageAndValidation(usingHunterNumber);
        consumer.accept(tup._1);
        assertFailure(tup, expectedMessages);
    }

    private static void assertFailure(final Tuple2<MooseDataCardPage1, MooseDataCardFilenameValidation> input,
                                      final List<String> expectedMessages) {

        final Validation<List<String>, MooseDataCardPage1Validation> validation = validate(input);
        assertTrue(validation.isInvalid());
        assertEquals(expectedMessages, validation.getError());
    }

    private static Validation<List<String>, MooseDataCardPage1Validation> validate(
            final Tuple2<MooseDataCardPage1, MooseDataCardFilenameValidation> tuple) {

        return tuple.apply(INSTANCE::validate);
    }

    private Tuple2<MooseDataCardPage1, MooseDataCardFilenameValidation> newPageAndValidation(
            final boolean usingHunterNumber) {

        final MooseDataCardPage1 page = newPage1(usingHunterNumber);

        return Tuple.of(page, new MooseDataCardFilenameValidation(
                page.getPermitNumber(), page.getHuntingClubCode(), DateUtil.now()));
    }

    private MooseDataCardPage1 newPage1(final boolean usingHunterNumber) {
        final MooseDataCardPage1 page = new MooseDataCardPage1()
                .withContactPerson(new MooseDataCardContactPerson())
                .withPermitNumber(permitNumber())
                .withHuntingClubCode(CLUB_CODE)
                .withClubInfo(new MooseDataCardClubInfo().withHuntingClubCoordinate("6822384;310088"));

        if (usingHunterNumber) {
            page.getContactPerson().setHunterNumber(hunterNumber());
        } else {
            page.getContactPerson().setSsn(ssn());
        }

        return page;
    }

}
