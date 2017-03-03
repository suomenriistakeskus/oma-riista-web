package fi.riista.util;

import com.vividsolutions.jts.geom.Polygon;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.validation.FinnishCreditorReferenceValidator;
import fi.riista.validation.FinnishHuntingPermitNumberValidator;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toSet;

public final class ValueGenerator {

    private static final int TEN_MILLION = 10000000;

    private static final DecimalFormat HTA_NUMBER_FORMATTER;

    static {
        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator('-');

        HTA_NUMBER_FORMATTER = new DecimalFormat("000,000", symbols);
    }

    @Nonnull
    public static String personName() {
        // Must start with capital letter
        return StringUtils.capitalize(RandomStringUtils.randomAlphabetic(10));
    }

    @Nonnull
    public static String email(@Nonnull final NumberGenerator numberGenerator,
                               @Nullable final String firstName,
                               @Nullable final String lastName) {

        return String.format("%s.%s.%d@example.invalid", firstName, lastName, numberGenerator.nextPositiveInt());
    }

    @Nonnull
    public static String postalCode(@Nonnull final NumberGenerator numberGenerator) {
        Objects.requireNonNull(numberGenerator);
        return zeroPaddedNumber(numberGenerator.nextNonNegativeIntBelow(100000), 5);
    }

    @Nonnull
    public static String phoneNumber(@Nonnull final NumberGenerator numberGenerator) {
        Objects.requireNonNull(numberGenerator);
        return String.format("+35850%s", zeroPaddedNumber(numberGenerator.nextNonNegativeIntBelow(10000000), 7));
    }

    @Nonnull
    public static String htaNumber(@Nonnull final NumberGenerator numberGenerator) {
        Objects.requireNonNull(numberGenerator);
        return htaNumber(numberGenerator.nextPositiveInt());
    }

    @Nonnull
    public static String htaNumber(final int num) {
        return HTA_NUMBER_FORMATTER.format(Math.abs(num) % 1_000_000);
    }

    @Nonnull
    public static Double weight(@Nonnull final NumberGenerator numberGenerator) {
        Objects.requireNonNull(numberGenerator);
        return Integer.valueOf(numberGenerator.nextNonNegativeIntBelow(1000)).doubleValue();
    }

    @Nonnull
    public static String zeroPaddedNumber(final int maxDigits, @Nonnull final NumberGenerator numberGenerator) {
        Objects.requireNonNull(numberGenerator);
        final int modulo = Double.valueOf(Math.pow(10.0, maxDigits)).intValue();
        return String.format("%0" + maxDigits + "d", numberGenerator.nextInt() % modulo);
    }

    @Nonnull
    public static String zeroPaddedNumber(final int number, final int minDigits) {
        return String.format("%0" + minDigits + "d", number);
    }

    @Nonnull
    public static String zeroPad(final String s, final int minLength) {
        return StringUtils.leftPad(s, minLength, '0');
    }

    @Nonnull
    public static <E extends Enum<E>> E some(@Nonnull final Class<E> clazz,
                                             @Nonnull final NumberGenerator numberGenerator) {

        Objects.requireNonNull(clazz, "clazz must not be null");

        return pickEnumValue(EnumSet.allOf(clazz), numberGenerator);
    }

    @Nonnull
    public static <E extends Enum<E>> E some(@Nonnull final EnumSet<E> enumSet,
                                             @Nonnull final NumberGenerator numberGenerator) {

        return pickEnumValue(enumSet, numberGenerator);
    }

    public static <E extends Enum<E>> E someOtherThan(@Nullable final E enumValue,
                                                      @Nonnull final Class<E> clazz,
                                                      @Nonnull final NumberGenerator numberGenerator) {

        Objects.requireNonNull(clazz, "clazz must not be null");
        Objects.requireNonNull(numberGenerator, "numberGenerator must not be null");

        if (clazz.getEnumConstants().length <= 1) {
            throw new IllegalArgumentException("Enum class does not have more than one value: " + clazz.getName());
        }

        final EnumSet<E> enumSet = enumValue != null
                ? EnumSet.complementOf(EnumSet.of(enumValue))
                : EnumSet.allOf(clazz);

        return pickEnumValue(enumSet, numberGenerator);
    }

    public static <E extends Enum<E>> E someOtherThan(@Nullable final E enumValue,
                                                      @Nonnull final EnumSet<E> enumSet,
                                                      @Nonnull final NumberGenerator numberGenerator) {

        Objects.requireNonNull(enumSet, "enumSet must not be null");
        Objects.requireNonNull(numberGenerator, "numberGenerator must not be null");

        final Set<E> filteredEnumSet = enumSet.stream().filter(e -> e != enumValue).collect(toSet());
        return pickEnumValue(filteredEnumSet, numberGenerator);
    }

    private static <E extends Enum<E>> E pickEnumValue(@Nonnull final Set<E> enumSet,
                                                       @Nonnull final NumberGenerator numberGenerator) {

        Objects.requireNonNull(enumSet, "enumSet must not be null");
        Objects.requireNonNull(numberGenerator, "numberGenerator must not be null");

        final int numElementsToBypass = numberGenerator.nextNonNegativeIntBelow(enumSet.size());
        final Iterator<E> iter = enumSet.iterator();

        for (int i = 0; i < numElementsToBypass; i++) {
            iter.next();
        }

        return iter.next();
    }

    @Nonnull
    public static GeoLocation geoLocation(@Nonnull final GeoLocation.Source geoLocationSource,
                                          @Nonnull final NumberGenerator numberGenerator) {

        Objects.requireNonNull(geoLocationSource, "geoLocationSource must not be null");
        Objects.requireNonNull(numberGenerator, "numberGenerator must not be null");

        // Some rough magic numbers that can be fine-tuned if needed.
        final int minLatitude = 6754304;
        final int maxLatitude = 7593984;
        final int minLongitude = 375072;
        final int maxLongitude = 563488;

        final int latOffset = numberGenerator.nextNonNegativeInt() * 100 % (maxLatitude - minLatitude);
        final int lngOffset = numberGenerator.nextNonNegativeInt() * 100 % (maxLongitude - minLongitude);

        return new GeoLocation(minLatitude + latOffset, minLongitude + lngOffset, geoLocationSource);
    }

    @Nonnull
    public static String hunterNumber(@Nonnull final NumberGenerator numberGenerator) {
        Objects.requireNonNull(numberGenerator);

        int num = (TEN_MILLION / 2 + numberGenerator.nextNonNegativeInt()) % TEN_MILLION;
        if (num < 1000000) {
            num = TEN_MILLION - num;
        }
        final String str = String.valueOf(num);
        return str + FinnishCreditorReferenceValidator.calculateChecksum(str);
    }

    @Nonnull
    public static String permitNumber(@Nonnull final String rka, @Nonnull final NumberGenerator numberGenerator) {
        Objects.requireNonNull(rka, "rka must not be null");
        Objects.requireNonNull(numberGenerator, "numberGenerator must not be null");

        final int year = DateUtil.now().getYear();
        final int yearsValid = 1;
        final String counter = zeroPaddedNumber(5, numberGenerator);
        final String permitNumberWithoutChecksum =
                String.format("%s-%s-%s-%s-", year, yearsValid, StringUtils.right(rka, 3), counter);
        final char checksum = FinnishHuntingPermitNumberValidator.calculateChecksum(permitNumberWithoutChecksum);
        return permitNumberWithoutChecksum + checksum;
    }

    public static CreditorReference creditorReference(@Nonnull final NumberGenerator numberGenerator) {
        final String c = String.valueOf(numberGenerator.nextInt());
        return CreditorReference.fromNullable(c + FinnishCreditorReferenceValidator.calculateChecksum(c));
    }

    // Returns a base36 representation of a number generated from monotonically increasing
    // sequence during test execution.
    public static String externalAreaId(@Nonnull final NumberGenerator numberGenerator) {
        Objects.requireNonNull(numberGenerator);

        final LongStream ls = LongStream.concat(
                LongStream.of(System.currentTimeMillis()),
                LongStream.generate(numberGenerator::nextLong));

        final BigInteger bigInt = ls.mapToObj(BigInteger::valueOf).limit(6).reduce(BigInteger::multiply).get();

        // Zero padding likely not needed because integer values are probably large enough.
        return zeroPad(bigInt.toString(36).toUpperCase(), 10);
    }

    @Nonnull
    public static Polygon geometryContaining(@Nonnull final GeoLocation location) {
        return squareFromLocationAndOffsets(location, -1, 1);
    }

    @Nonnull
    public static Polygon geometryNotContaining(@Nonnull final GeoLocation location) {
        return squareFromLocationAndOffsets(location, 1, 2);
    }

    @Nonnull
    public static Polygon squareFromLocationAndOffsets(
            final GeoLocation location, final int firstOffset, final int secondOffset) {

        Objects.requireNonNull(location, "location must not be null");

        final List<Tuple2<Integer, Integer>> offsets = Arrays.asList(
                Tuple.of(firstOffset, firstOffset),
                Tuple.of(firstOffset, secondOffset),
                Tuple.of(secondOffset, secondOffset),
                Tuple.of(secondOffset, firstOffset));

        return GISUtils.createPolygon(location, offsets);
    }

    private ValueGenerator() {
        throw new AssertionError();
    }
}
