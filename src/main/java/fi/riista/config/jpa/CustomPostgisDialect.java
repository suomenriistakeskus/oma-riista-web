package fi.riista.config.jpa;

import org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect;

public class CustomPostgisDialect extends PostgisPG95Dialect {
    public CustomPostgisDialect() {
        super();

        registerTrigramExtensionFunctions();
    }

    // @see http://www.postgresql.org/docs/9.3/static/pgtrgm.html
    // @see http://www.depesz.com/2011/02/19/waiting-for-9-1-faster-likeilike/
    private void registerTrigramExtensionFunctions() {
        // fuzzy string match if trigram similarity is larger than 0.3 (default)
        registerFunction("trgm_match", new PostgreSQLTrigramFunction());

        // return trigram distance (0-1.0) for sorting using fuzzy string similarity
        registerFunction("trgm_dist", new PostgreSQLDistanceFunction());
    }
}
