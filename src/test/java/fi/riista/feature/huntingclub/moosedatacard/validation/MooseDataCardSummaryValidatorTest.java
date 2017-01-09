package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newMooseDataCard;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newPage7;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newPage8;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingEndDateNotWithinPermitSeason;
import static fi.riista.util.Asserts.assertValidationErrors;
import static fi.riista.util.DateUtil.today;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.Has2BeginEndDatesDTO;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCard;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage7;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage8;

import javaslang.control.Validation;

import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

// Acts mainly as integration test for composed validations but tests also validation of hunting end
// date.
public class MooseDataCardSummaryValidatorTest {

    @Test
    public void testCoverage_whenValidationFails() {
        final LocalDate today = today();
        final Has2BeginEndDates permitSeason = newPermitSeason(today, today);

        final LocalDate huntingEndDate = today.plusDays(1);
        final MooseDataCardPage8 page8 = newPage8().withHuntingEndDate(huntingEndDate);
        page8.getSection_8_1().setTotalHuntingArea(-1.0);
        page8.getSection_8_3().setNumberOfDrownedMooses(-1);

        final MooseDataCard card = newMooseDataCard(page8);

        final List<String> expectedMessages = Stream.of(
                MooseDataCardSection81Validator.validate(page8.getSection_8_1()),
                MooseDataCardSection83Validator.validate(page8.getSection_8_3()))
                .map(validation -> {
                    assertTrue("Validation should have failed", validation.isInvalid());
                    return validation.getError();
                })
                .flatMap(List::stream)
                .collect(toList());
        expectedMessages.add(huntingEndDateNotWithinPermitSeason(huntingEndDate, permitSeason));

        assertValidationErrors(MooseDataCardSummaryValidator.validate(card, permitSeason), expectedMessages);
    }

    @Test
    public void testCoverage_forNullingOfInvalidValues() {
        final MooseDataCardPage7 page7 = newPage7().withEstimatedSpecimenAmountOfWhiteTailedDeer(-1);

        final MooseDataCardPage8 page8 = newPage8();
        page8.getSection_8_4().setNumberOfAdultMoosesHavingFlies(-1);

        final MooseDataCard card = newMooseDataCard(page7, page8);

        final LocalDate today = today();
        final Validation<List<String>, MooseDataCard> validation =
                MooseDataCardSummaryValidator.validate(card, newPermitSeason(today, today));
        assertTrue(validation.isValid());

        validation.peek(validCard -> {
            assertNull(validCard.getPage7().get(0).getEstimatedSpecimenAmountOfWhiteTailedDeer());
            assertNull(validCard.getPage8().get(0).getSection_8_4().getNumberOfAdultMoosesHavingFlies());
        });
    }

    private static Has2BeginEndDates newPermitSeason(final LocalDate beginDate, final LocalDate endDate) {
        return new Has2BeginEndDatesDTO(beginDate, endDate, null, null);
    }

}
