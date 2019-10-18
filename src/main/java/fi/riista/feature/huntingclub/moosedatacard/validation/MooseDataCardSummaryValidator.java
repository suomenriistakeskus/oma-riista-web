package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCard;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage7;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage8;
import fi.riista.util.ValidationUtils;
import io.vavr.control.Validation;
import org.joda.time.LocalDate;
import org.springframework.beans.BeanUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingEndDateNotGiven;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingEndDateNotWithinPermitSeason;

public class MooseDataCardSummaryValidator {

    public static Validation<List<String>, MooseDataCard> validate(@Nonnull final MooseDataCard mooseDataCard,
                                                                   @Nonnull final Has2BeginEndDates permitSeason) {

        Objects.requireNonNull(mooseDataCard, "mooseDataCard is null");
        Objects.requireNonNull(permitSeason, "permitSeason is null");

        final List<String> errorMsgs = new ArrayList<>();
        final AtomicReference<MooseDataCard> mutatedCardRef = new AtomicReference<>();

        final Supplier<MooseDataCard> mutatedCardSupplier = () -> Optional
                .ofNullable(mutatedCardRef.get())
                .orElseGet(() -> {
                    mutatedCardRef.set(mooseDataCard.createCopy());
                    return mutatedCardRef.get();
                });

        MooseDataCardExtractor.findFirstPage7ContainingHuntingSummaryData(mooseDataCard).ifPresent(originalPage7 -> {
            MooseDataCardPage7MooselikeValidator.validate(originalPage7)
                    .toEither()
                    .peekLeft(errorMsgs::addAll)
                    .peek(validPage7 -> {
                        final MooseDataCardPage7 resultPage7 = MooseDataCardExtractor
                                .findFirstPage7ContainingHuntingSummaryData(mutatedCardSupplier.get())
                                .orElseThrow(() -> new IllegalStateException("Could not find moose data card page 7"));

                        BeanUtils.copyProperties(validPage7, resultPage7);
                    });
        });

        MooseDataCardExtractor.findFirstNonEmptyPage8(mooseDataCard).ifPresent(originalPage8 -> {

            final Supplier<MooseDataCardPage8> resultPage8Supplier = () -> MooseDataCardExtractor
                    .findFirstNonEmptyPage8(mutatedCardSupplier.get())
                    .orElseThrow(() -> new IllegalStateException("Could not find moose data card page 8"));

            MooseDataCardSection81Validator.validate(originalPage8.getSection_8_1())
                    .toEither()
                    .peekLeft(errorMsgs::addAll)
                    .peek(resultPage8Supplier.get()::setSection_8_1);

            // Section 8.2 not included because it relates to harvests which are validated elsewhere.

            MooseDataCardSection83Validator.validate(originalPage8.getSection_8_3())
                    .toEither()
                    .peekLeft(errorMsgs::addAll)
                    .peek(resultPage8Supplier.get()::setSection_8_3);

            MooseDataCardSection84Validator.validate(originalPage8.getSection_8_4())
                    .toEither()
                    .peekLeft(errorMsgs::addAll)
                    .peek(resultPage8Supplier.get()::setSection_8_4);

            final LocalDate huntingEndDate = originalPage8.getHuntingEndDate();

            if (huntingEndDate == null) {
                errorMsgs.add(huntingEndDateNotGiven());
            } else if (!permitSeason.containsDate(huntingEndDate)) {
                errorMsgs.add(huntingEndDateNotWithinPermitSeason(huntingEndDate, permitSeason));
            }
        });

        return ValidationUtils.toValidation(errorMsgs, () -> {
            return Optional.ofNullable(mutatedCardRef.get()).orElse(mooseDataCard);
        });
    }
}
