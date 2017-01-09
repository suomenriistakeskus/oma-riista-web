package fi.riista.util;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ResourceUtils {

    private static final ResourcePatternResolver RESOURCE_PATTERN_RESOLVER = new PathMatchingResourcePatternResolver();

    private static final MetadataReaderFactory METADATA_READER_FACTORY =
            new CachingMetadataReaderFactory(RESOURCE_PATTERN_RESOLVER);

    private static final Function<MetadataReader, Class<?>> METADATA_READER_TO_CLASS = reader -> {
        try {
            final String className = reader.getClassMetadata().getClassName();
            return ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    };

    private ResourceUtils() {
        throw new AssertionError();
    }

    @Nonnull
    public static Set<Class<?>> getClasses(@Nonnull final String basePackage, @Nonnull final String pathComponent) {
        return streamMetadataReaders(basePackage)
                .filter(getPathComponentPredicate(pathComponent))
                .map(METADATA_READER_TO_CLASS)
                .collect(toSet());
    }

    @Nonnull
    public static List<MetadataReader> getMetadataReaders(@Nonnull final String basePackage) {
        return streamMetadataReaders(basePackage).collect(toList());
    }

    @Nonnull
    public static List<MetadataReader> getMetadataReaders(
            @Nonnull final String basePackage, @Nonnull final String requiredPathComponent) {

        return streamMetadataReaders(basePackage)
                .filter(getPathComponentPredicate(requiredPathComponent))
                .collect(toList());
    }

    @Nonnull
    private static Stream<MetadataReader> streamMetadataReaders(@Nonnull final String basePackage) {
        Objects.requireNonNull(basePackage);

        final String packageSearchPath = String.format("classpath*:%s/**/*.class", getResourcePath(basePackage));

        final Function<? super Resource, MetadataReader> fn = resource -> {
            try {
                return METADATA_READER_FACTORY.getMetadataReader(resource);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        };

        try {
            return Stream.of(RESOURCE_PATTERN_RESOLVER.getResources(packageSearchPath))
                    .filter(Resource::isReadable)
                    .map(fn);

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getResourcePath(final String packageName) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(packageName));
    }

    private static final Predicate<MetadataReader> getPathComponentPredicate(final String requiredPathComponent) {
        Objects.requireNonNull(requiredPathComponent);

        return input -> {
            try {
                return input.getResource().getFile().getAbsolutePath().toString()
                        .replace("\\", "/")
                        .contains(requiredPathComponent);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

}
