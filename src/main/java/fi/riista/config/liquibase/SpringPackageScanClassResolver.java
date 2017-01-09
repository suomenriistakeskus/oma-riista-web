package fi.riista.config.liquibase;

import liquibase.servicelocator.DefaultPackageScanClassResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;

public class SpringPackageScanClassResolver extends DefaultPackageScanClassResolver {
    private static final Logger LOG = LoggerFactory.getLogger(SpringPackageScanClassResolver.class);

    @Override
    protected void findAllClasses(String packageName, ClassLoader loader) {
        final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(loader);

        try {
            for (final Resource resource : scan(loader, packageName)) {
                try {
                    final MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);
                    addFoundClass(ClassUtils.forName(reader.getClassMetadata().getClassName(), loader));
                } catch (ClassNotFoundException | LinkageError ex) {
                    LOG.debug("Ignoring candidate class resource " + resource + " due to " + ex);
                } catch (Throwable ex) {
                    LOG.warn("Unexpected failure when loading class resource " + resource, ex);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static Resource[] scan(final ClassLoader loader, final String packageName) throws IOException {
        return new PathMatchingResourcePatternResolver(loader).getResources(
                ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                        ClassUtils.convertClassNameToResourcePath(packageName) + "/**/*.class");
    }
}
