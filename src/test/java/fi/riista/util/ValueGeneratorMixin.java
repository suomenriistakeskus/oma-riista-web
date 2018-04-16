package fi.riista.util;

import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.entity.GeoLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.EnumSet;

public interface ValueGeneratorMixin {

    NumberGenerator getNumberGenerator();

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

    default GeoLocation geoLocation() {
        return geoLocation(some(GeoLocation.Source.class));
    }

    default GeoLocation geoLocation(@Nonnull final GeoLocation.Source geoLocationSource) {
        return ValueGenerator.geoLocation(geoLocationSource, getNumberGenerator());
    }

    default String hunterNumber() {
        return ValueGenerator.hunterNumber(getNumberGenerator());
    }

    default String permitNumber(@Nonnull final String rka) {
        return ValueGenerator.permitNumber(rka, getNumberGenerator());
    }

    default CreditorReference creditorReference() {
        return ValueGenerator.creditorReference(getNumberGenerator());
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

}
