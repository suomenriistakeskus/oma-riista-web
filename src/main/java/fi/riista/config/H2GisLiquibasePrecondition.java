package fi.riista.config;

import liquibase.database.Database;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.precondition.CustomPrecondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

public class H2GisLiquibasePrecondition implements CustomPrecondition {
    private static final Logger LOG = LoggerFactory.getLogger(H2GisLiquibasePrecondition.class);

    @Override
    public void check(final Database database) throws CustomPreconditionFailedException {
        if (!ClassUtils.isPresent("org.h2gis.functions.factory.H2GISFunctions",
                H2GisLiquibasePrecondition.class.getClassLoader())) {
            LOG.info("Could not initialize H2 spatial");
            throw new CustomPreconditionFailedException("H2GIS not available");
        }
    }
}
