package fi.riista.util;

import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.money.FinnishBank;
import fi.riista.feature.common.money.FinnishBankAccount;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import org.iban4j.Bic;
import org.iban4j.Iban;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

@FunctionalInterface
public interface ValueGeneratorMixin {

    NumberGenerator getNumberGenerator();

    default long nextLong() {
        return getNumberGenerator().nextLong();
    }

    default int nextIntBetween(final int closedLowerBound, final int closedUpperBound) {
        return getNumberGenerator().nextIntBetween(closedLowerBound, closedUpperBound);
    }

    default int nextPositiveInt() {
        return getNumberGenerator().nextPositiveInt();
    }

    default int nextPositiveIntAtMost(final int closedUpperBound) {
        return getNumberGenerator().nextPositiveIntAtMost(closedUpperBound);
    }

    default int nextPositiveIntBelow(final int openUpperBound) {
        return getNumberGenerator().nextPositiveIntBelow(openUpperBound);
    }

    default int nextNonNegativeInt() {
        return getNumberGenerator().nextNonNegativeInt();
    }

    default int nextNonNegativeIntAtMost(final int closedUpperBound) {
        return getNumberGenerator().nextNonNegativeIntAtMost(closedUpperBound);
    }

    default int nextNonNegativeIntBelow(final int openUpperBound) {
        return getNumberGenerator().nextNonNegativeIntBelow(openUpperBound);
    }

    default boolean someBoolean() {
        return nextPositiveInt() % 2 == 0;
    }

    default Boolean someOtherThan(@Nullable final Boolean value) {
        return !Boolean.TRUE.equals(value);
    }

    default <E extends Enum<E>> E some(@Nonnull final Class<E> enumClass) {
        return ValueGenerator.some(enumClass, getNumberGenerator());
    }

    default <E extends Enum<E>> E some(@Nonnull final EnumSet<E> enumSet) {
        return ValueGenerator.some(enumSet, getNumberGenerator());
    }

    default <E extends Enum<E>> E someOtherThan(@Nullable final E enumValue, @Nonnull final Class<E> clazz) {
        return ValueGenerator.someOtherThan(enumValue, clazz, getNumberGenerator());
    }

    default <E extends Enum<E>> E someOtherThan(@Nullable final E enumValue, @Nonnull final EnumSet<E> enumSet) {
        return ValueGenerator.someOtherThan(enumValue, enumSet, getNumberGenerator());
    }

    default String postalCode() {
        return ValueGenerator.postalCode(getNumberGenerator());
    }

    default String phoneNumber() {
        return ValueGenerator.phoneNumber(getNumberGenerator());
    }

    default String artificialSsn() {
        return SsnSequence.nextArtificialSsn();
    }

    default String ssn() {
        return SsnSequence.nextRealSsn();
    }

    default String personName() {
        return ValueGenerator.personName();
    }

    default GeoLocation geoLocation() {
        return geoLocation(some(GeoLocation.Source.class));
    }

    default GeoLocation geoLocation(@Nonnull final GeoLocation.Source geoLocationSource) {
        return ValueGenerator.geoLocation(geoLocationSource, getNumberGenerator());
    }

    default String hunterNumber() {
        return ValueGenerator.hunterNumber(getNumberGenerator());
    }

    default String permitNumber() {
        return ValueGenerator.permitNumber(getNumberGenerator());
    }

    default String permitNumber(final int year) {
        return permitNumber(year, 1);
    }

    default String permitNumber(final int year, final int yearsValid) {
        return ValueGenerator.permitNumber(year, yearsValid, getNumberGenerator());
    }

    default CreditorReference creditorReference() {
        return ValueGenerator.creditorReference(getNumberGenerator());
    }

    default Bic bic() {
        return ValueGenerator.bic(getNumberGenerator());
    }

    default Iban iban() {
        return ValueGenerator.iban(getNumberGenerator());
    }

    default Iban iban(final FinnishBank bank) {
        return ValueGenerator.iban(bank, getNumberGenerator());
    }

    default FinnishBankAccount bankAccount() {
        return ValueGenerator.bankAccount(getNumberGenerator());
    }

    default Double weight() {
        return ValueGenerator.weight(getNumberGenerator());
    }

    default String externalAreaId() {
        return ValueGenerator.externalAreaId(getNumberGenerator());
    }

    default String zeroPaddedNumber(final int maxDigits) {
        return ValueGenerator.zeroPaddedNumber(maxDigits, getNumberGenerator());
    }

    default String zeroPaddedNumber(final int number, final int minDigits) {
        return ValueGenerator.zeroPaddedNumber(number, minDigits);
    }

    default String htaNumber() {
        return ValueGenerator.htaNumber(getNumberGenerator());
    }

    default String email(@Nullable final String firstName,
                         @Nullable final String lastName) {
        return ValueGenerator.email(getNumberGenerator(), firstName, lastName);
    }

    default EnumSet<HarvestSpecimenType> harvestSpecimenTypes(final Predicate<HarvestSpecimenType> condition) {
        return F.filterToEnumSet(HarvestSpecimenType.class, condition);
    }

    default HarvestSpecimenType harvestSpecimenType(final Predicate<HarvestSpecimenType> condition) {
        return some(harvestSpecimenTypes(condition));
    }
}
