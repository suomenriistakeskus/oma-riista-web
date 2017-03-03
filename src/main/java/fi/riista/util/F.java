package fi.riista.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fi.riista.feature.common.entity.HasID;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.control.Either;

import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * F stands for utilities for functional programming style (mainly operating with collections).
 */
public final class F {

    /**
     * Lazily evaluate given suppliers as long as supplier returns Optional having value present.
     *
     * @param suppliers
     * @param <T>
     * @return
     */
    @SafeVarargs
    public static <T> Optional<T> optionalFromSuppliers(Supplier<Optional<T>>... suppliers) {
        return Stream.of(suppliers)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Nullable
    @SafeVarargs
    public static <T> T firstNonNull(@Nullable final T... objects) {
        if (objects != null) {
            for (final T obj : objects) {
                if (obj != null) {
                    return obj;
                }
            }
        }
        return null;
    }

    @SafeVarargs
    public static boolean anyNull(@Nullable final Object... objects) {
        return objects == null || Stream.of(objects).anyMatch(Objects::isNull);
    }

    @SafeVarargs
    public static boolean anyNonNull(@Nullable final Object... objects) {
        return firstNonNull(objects) != null;
    }

    @SafeVarargs
    public static boolean allNull(@Nullable final Object... objects) {
        return objects == null || objects.length > 0 && Stream.of(objects).allMatch(Objects::isNull);
    }

    @SafeVarargs
    public static boolean allNotNull(@Nullable final Object... objects) {
        return !isNullOrEmpty(objects) && Stream.of(objects).allMatch(Objects::nonNull);
    }

    @Nonnull
    @SafeVarargs
    public static <T> TreeSet<T> newSortedSet(@Nullable final T... values) {
        return values == null ? new TreeSet<>() : Arrays.stream(values).collect(toCollection(TreeSet::new));
    }

    @Nonnull
    public static <T> ArrayList<T> newListWithCapacityOf(@Nonnull final Iterable<?> iterable) {
        final OptionalInt sizeOpt = sizeMaybe(iterable);
        return sizeOpt.isPresent() ? new ArrayList<>(sizeOpt.getAsInt()) : new ArrayList<>();
    }

    @Nonnull
    public static <T> HashSet<T> newSetWithExpectedSizeOf(@Nonnull final Iterable<?> iterable) {
        final OptionalInt sizeOpt = sizeMaybe(iterable);
        return sizeOpt.isPresent() ? Sets.newHashSetWithExpectedSize(sizeOpt.getAsInt()) : new HashSet<>();
    }

    @Nonnull
    public static <K, V> HashMap<K, V> newMapWithCapacityOf(@Nonnull final Iterable<?> iterable) {
        final OptionalInt sizeOpt = sizeMaybe(iterable);
        return sizeOpt.isPresent() ? Maps.newHashMapWithExpectedSize(sizeOpt.getAsInt()) : new HashMap<>();
    }

    @Nonnull
    @SafeVarargs
    public static <T> List<T> concat(@Nonnull final List<? extends T>... lists) {
        Objects.requireNonNull(lists);
        return Arrays.stream(lists).flatMap(List::stream).collect(toList());
    }

    @Nonnull
    public static <T> List<T> filterToList(@Nonnull final Iterable<? extends T> iterable,
                                           @Nonnull final Predicate<? super T> predicate) {

        Objects.requireNonNull(iterable, "iterable is null");
        Objects.requireNonNull(predicate, "predicate is null");

        return stream(iterable).filter(predicate).collect(toList());
    }

    @Nonnull
    public static <E extends Enum<E>> List<E> filterToList(@Nonnull final Class<E> enumType,
                                                           @Nonnull final Predicate<? super E> predicate) {

        Objects.requireNonNull(enumType, "enumType is null");
        Objects.requireNonNull(predicate, "predicate is null");

        return Stream.of(enumType.getEnumConstants()).filter(predicate).collect(toList());
    }

    @Nonnull
    public static <T> Set<T> filterToSet(@Nonnull final Iterable<? extends T> iterable,
                                         @Nonnull final Predicate<? super T> predicate) {

        Objects.requireNonNull(iterable, "iterable is null");
        Objects.requireNonNull(predicate, "predicate is null");

        return stream(iterable).filter(predicate).collect(toSet());
    }

    @Nonnull
    public static <S, T, C extends Collection<T>> C mapNonNulls(@Nonnull final Iterable<? extends S> iterable,
                                                                @Nonnull final C destination,
                                                                @Nonnull final Function<? super S, ? extends T> mapper) {

        Objects.requireNonNull(iterable, "iterable is null");
        Objects.requireNonNull(destination, "destination is null");
        Objects.requireNonNull(mapper, "mapper is null");

        return mapNonNullsInternal(stream(iterable), mapper, () -> destination);
    }

    @Nonnull
    public static <S, T> ArrayList<T> mapNonNullsToList(@Nonnull final Iterable<? extends S> iterable,
                                                        @Nonnull final Function<? super S, T> mapper) {

        Objects.requireNonNull(iterable, "iterable is null");
        Objects.requireNonNull(mapper, "mapper is null");

        return mapNonNullsInternal(stream(iterable), mapper, () -> newListWithCapacityOf(iterable));
    }

    @Nonnull
    public static <S, T> HashSet<T> mapNonNullsToSet(@Nonnull final Iterable<? extends S> iterable,
                                                     @Nonnull final Function<? super S, T> mapper) {

        Objects.requireNonNull(iterable, "iterable is null");
        Objects.requireNonNull(mapper, "mapper is null");

        return mapNonNullsInternal(stream(iterable), mapper, () -> newSetWithExpectedSizeOf(iterable));
    }

    @Nonnull
    public static <S, T> ArrayList<T> mapNonNullsToList(@Nonnull final S[] array,
                                                        @Nonnull final Function<? super S, ? extends T> mapper) {

        Objects.requireNonNull(array, "array is null");

        return mapNonNullsInternal(Arrays.stream(array), mapper, () -> new ArrayList<>(array.length));
    }

    @Nonnull
    public static <S, T> HashSet<T> mapNonNullsToSet(@Nonnull final S[] array,
                                                     @Nonnull final Function<? super S, ? extends T> mapper) {

        Objects.requireNonNull(array, "array is null");

        return mapNonNullsInternal(Arrays.stream(array), mapper, () -> Sets.newHashSetWithExpectedSize(array.length));
    }

    private static <S, T, C extends Collection<T>> C mapNonNullsInternal(final Stream<? extends S> sourceStream,
                                                                         final Function<? super S, ? extends T> mapper,
                                                                         final Supplier<C> collectionSupplier) {
        return sourceStream
                .map(mapper)
                .filter(Objects::nonNull)
                .collect(toCollection(collectionSupplier));
    }

    @Nonnull
    public static <T> Map<Boolean, List<T>> partition(@Nonnull final Iterable<? extends T> iterable,
                                                      @Nonnull final Predicate<? super T> predicate) {

        Objects.requireNonNull(iterable, "iterable is null");
        Objects.requireNonNull(predicate, "predicate is null");

        return stream(iterable).collect(partitioningBy(predicate));
    }

    @Nonnull
    public static <T> Tuple2<List<T>, List<T>> split(@Nonnull final Iterable<? extends T> iterable,
                                                     final int numberOfElementsToTakeIntoFirst) {

        final OptionalInt sizeOpt = sizeMaybe(iterable);
        if (sizeOpt.isPresent()) {
            final int size = sizeOpt.getAsInt();

            if (numberOfElementsToTakeIntoFirst > size) {
                throw new IllegalArgumentException(
                        "numberOfElementsToTakeIntoFirst must not be greater than number of iterables");
            }
        }

        final ArrayList<T> firstSpan = new ArrayList<>();
        final ArrayList<T> secondSpan = new ArrayList<>();

        int i = 0;
        for (final T t : iterable) {
            if (i < numberOfElementsToTakeIntoFirst) {
                firstSpan.add(t);
            } else {
                secondSpan.add(t);
            }
            i++;
        }

        return Tuple.of(firstSpan, secondSpan);
    }

    public static boolean isNullOrEmpty(@Nullable final Iterable<?> iterable) {
        return iterable == null || Iterables.isEmpty(iterable);
    }

    @SafeVarargs
    public static <T> boolean isNullOrEmpty(@Nullable final T... objects) {
        return objects == null || objects.length == 0;
    }

    public static boolean hasId(@Nullable final HasID<?> identifiable) {
        return identifiable != null && identifiable.getId() != null;
    }

    @Nullable
    public static <ID> ID getId(@Nullable final HasID<ID> identifiable) {
        return identifiable == null ? null : identifiable.getId();
    }

    @Nonnull
    public static <ID> List<ID> getNonNullIds(@Nonnull final Iterable<? extends HasID<ID>> identifiables) {
        return mapNonNullsToList(identifiables, F::getId);
    }

    @SafeVarargs
    @Nonnull
    public static <ID, T extends HasID<ID>> List<ID> getNonNullIds(@Nullable final T... identifiables) {
        return identifiables == null ? new ArrayList<>(0) : mapNonNullsToList(identifiables, F::getId);
    }

    @Nonnull
    public static <ID> Set<ID> getUniqueIds(@Nonnull final Iterable<? extends HasID<ID>> identifiables) {
        return mapNonNullsToSet(identifiables, F::getId);
    }

    @SafeVarargs
    @Nonnull
    public static <ID, T extends HasID<ID>> Set<ID> getUniqueIds(@Nullable final T... identifiables) {
        return identifiables == null ? new HashSet<>(0) : mapNonNullsToSet(identifiables, F::getId);
    }

    @Nonnull
    public static <T, ID> Set<ID> getUniqueIdsAfterTransform(@Nonnull final Iterable<? extends T> iterable,
                                                             @Nonnull final Function<? super T, ? extends HasID<ID>> mapper) {

        return mapNonNullsToSet(iterable, mapper.andThen(F::getId));
    }

    @Nonnull
    public static <ID extends Comparable<ID>, T extends HasID<ID>> List<T> sortedById(@Nonnull final Iterable<T> iterable) {
        return stream(iterable)
                .sorted(comparing(F::getId, nullsLast(naturalOrder())))
                .collect(toList());
    }

    @SafeVarargs
    @Nullable
    public static <T extends Comparable<? super T>> T getFirstByNaturalOrderNullsFirst(@Nullable final T... values) {
        return getFirst(naturalOrder(), true, values);
    }

    @SafeVarargs
    @Nullable
    public static <T extends Comparable<? super T>> T getFirstByNaturalOrderNullsLast(@Nullable final T... values) {
        return getFirst(naturalOrder(), false, values);
    }

    @SafeVarargs
    @Nullable
    public static <T extends Comparable<? super T>> T getFirst(@Nonnull final Comparator<T> comparator,
                                                               final boolean nullsFirst,
                                                               @Nullable final T... values) {

        Objects.requireNonNull(comparator, "comparator is null");

        return values == null || values.length == 0 ? null : Stream.of(values)
                .sorted(nullsFirst ? nullsFirst(comparator) : nullsLast(comparator))
                .limit(1L)
                .collect(toCollection(() -> new ArrayList<>(1)))
                .get(0);
    }

    @Nonnull
    public static <T> List<T> nonNullKeys(@Nonnull final Iterable<? extends Tuple2<? extends T, ?>> iterable) {
        return mapNonNullsToList(iterable, Tuple2::_1);
    }

    @Nonnull
    public static <ID, T extends HasID<ID>> Map<ID, T> indexById(@Nonnull final Iterable<? extends T> iterable) {
        return index(iterable, HasID::getId);
    }

    @Nonnull
    public static <T, U> Map<U, T> index(@Nonnull final Iterable<? extends T> iterable,
                                         @Nonnull final Function<? super T, U> keyMapper) {

        return stream(iterable).collect(toMap(keyMapper, identity()));
    }

    @Nonnull
    public static <K, V> Map<K, List<V>> nullSafeGroupBy(@Nonnull final Iterable<? extends V> iterable,
                                                         @Nonnull final Function<? super V, ? extends K> classifier) {

        Objects.requireNonNull(classifier, "classifier is null");

        final Map<K, List<V>> ret = newMapWithCapacityOf(iterable);

        for (final V value : iterable) {
            Objects.requireNonNull(value);

            final K key = classifier.apply(value);

            if (key != null) {
                ret.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }

        return ret;
    }

    @Nonnull
    public static <T, ID, U extends HasID<ID>> Map<ID, List<T>> groupByIdAfterTransform(
            @Nonnull final Iterable<? extends T> iterable, @Nonnull final Function<? super T, U> classifier) {

        return F.nullSafeGroupBy(iterable, t -> F.getId(classifier.apply(t)));
    }

    @SafeVarargs
    public static <T> boolean containsAny(@Nonnull final Iterable<? extends T> iterable, @Nullable final T... values) {
        Objects.requireNonNull(iterable, "iterable is null");

        if (values != null) {
            for (final T value : values) {
                if (Iterables.contains(iterable, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> boolean containsAny(@Nonnull final Iterable<T> iterable, @Nonnull final Iterable<T> values) {
        Objects.requireNonNull(iterable, "iterable is null");
        Objects.requireNonNull(values, "values is null");

        for (final T value : values) {
            if (Iterables.contains(iterable, value)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public static <T> boolean containsAll(@Nonnull final Iterable<? extends T> iterable, @Nullable final T... values) {
        Objects.requireNonNull(iterable, "iterable is null");

        if (values != null) {
            for (final T value : values) {
                if (!Iterables.contains(iterable, value)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static <T> boolean containsAll(@Nonnull final Iterable<T> iterable, @Nonnull final Iterable<T> values) {
        Objects.requireNonNull(iterable, "iterable is null");
        Objects.requireNonNull(values, "values is null");

        for (final T value : values) {
            if (!Iterables.contains(iterable, value)) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    public static <T> String join(@Nonnull final Iterable<? extends T> iterable,
                                  @Nonnull final Function<? super T, String> mapper,
                                  @Nonnull final CharSequence delimiter) {

        return F.stream(iterable).map(mapper).collect(joining(delimiter));
    }

    private static OptionalInt sizeMaybe(@Nonnull final Iterable<?> iterable) {
        Objects.requireNonNull(iterable);

        return Collection.class.isAssignableFrom(iterable.getClass())
                ? OptionalInt.of(Collection.class.cast(iterable).size())
                : Iterables.isEmpty(iterable) ? OptionalInt.of(0) : OptionalInt.empty();
    }

    @Nonnull
    public static <T> Stream<T> stream(@Nonnull final Iterable<T> iterable) {
        return StreamSupport.stream(Objects.requireNonNull(iterable).spliterator(), false);
    }

    @Nonnull
    public static <T> Stream<T> stream(@Nullable final T object, @Nonnull final Iterable<? extends T> iterable) {
        final Stream<T> first = object != null ? Stream.of(object) : Stream.empty();
        return Stream.concat(first, stream(iterable));
    }

    @Nonnull
    public static <T, U> Map<T, U> toMapAsKeySet(@Nonnull final Collection<? extends T> collection,
                                                 @Nonnull final Function<? super T, ? extends U> valueMapper) {

        Objects.requireNonNull(collection, "collection is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");

        return collection.stream().collect(toMap(identity(), valueMapper));
    }

    public static int coalesceAsInt(@Nullable final Number n, final int defaultValue) {
        return n != null ? n.intValue() : defaultValue;
    }

    public static <T> int sum(@Nonnull final Collection<? extends T> collection,
                              @Nonnull final ToIntFunction<? super T> mapper) {

        Objects.requireNonNull(collection, "collection is null");
        Objects.requireNonNull(mapper, "mapper is null");

        return collection.stream().mapToInt(mapper).sum();
    }

    public static <T> double sum(@Nonnull final Collection<? extends T> collection,
                                 @Nonnull final ToDoubleFunction<? super T> mapper) {

        Objects.requireNonNull(collection, "collection is null");
        Objects.requireNonNull(mapper, "mapper is null");

        return collection.stream().mapToDouble(mapper).sum();
    }

    @Nonnull
    public static <T> BigDecimal sum(@Nonnull final Collection<T> collection,
                                     @Nonnull final Function<? super T, BigDecimal> mapper) {

        Objects.requireNonNull(collection, "collection is null");
        Objects.requireNonNull(mapper, "mapper is null");

        return collection.stream().map(mapper).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Nonnull
    public static <T, U> Map<U, Integer> countByApplication(@Nonnull final Stream<? extends T> stream,
                                                            @Nonnull final Function<? super T, U> classifier) {

        Objects.requireNonNull(stream, "stream is null");
        Objects.requireNonNull(classifier, "classifier is null");

        return stream.collect(groupingBy(classifier, summingInt(t -> 1)));
    }

    public static <T> void consumeIfNonEmpty(@Nonnull final Collection<T> collection,
                                             @Nonnull final Consumer<Collection<T>> consumer) {

        Objects.requireNonNull(collection, "collection is null");
        Objects.requireNonNull(consumer, "consumer is null");

        if (!collection.isEmpty()) {
            consumer.accept(collection);
        }
    }

    @Nonnull
    public static Optional<String> trimToOptional(@Nullable final String str) {
        return Optional.ofNullable(StringUtils.trimToNull(str));
    }

    @Nonnull
    public static <R> Optional<R> toOptional(@Nonnull final Either<?, R> either) {
        Objects.requireNonNull(either);
        return either.fold(left -> Optional.empty(), Optional::ofNullable);
    }

    @Nonnull
    public static <L, R> Either<L, R> toEither(@Nonnull final Optional<R> optional,
                                               @Nonnull final Supplier<L> leftSupplier) {

        Objects.requireNonNull(optional, "optional is null");
        Objects.requireNonNull(leftSupplier, "leftSupplier is null");

        return optional.<Either<L, R>> map(Either::right).orElseGet(() -> Either.left(leftSupplier.get()));
    }

    @Nonnull
    public static <T, U> Optional<Either<T, U>> optionallyEither(@Nonnull final Optional<T> first,
                                                                 @Nonnull final Supplier<? extends Optional<U>> second) {
        Objects.requireNonNull(first, "first is null");
        Objects.requireNonNull(second, "second is null");

        return first.isPresent()
                ? first.map(Either::left)
                : Objects.requireNonNull(second.get(), "Second Optional is null").map(Either::right);
    }

    @Nullable
    public static <T, U extends T, V extends T> T reduceToCommonBase(@Nonnull final Either<U, V> either) {
        Objects.requireNonNull(either);
        return either.fold(identity(), identity());
    }

    private F() {
        throw new AssertionError();
    }

}
