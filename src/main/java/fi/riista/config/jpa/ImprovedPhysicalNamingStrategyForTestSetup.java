package fi.riista.config.jpa;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class ImprovedPhysicalNamingStrategyForTestSetup extends ImprovedPhysicalNamingStrategy {

    // Ensure quoting for year and system_user which are reserved words in H2.
    @Override
    public Identifier toPhysicalColumnName(final Identifier identifier, final JdbcEnvironment jdbcEnv) {
        final Identifier superIdentifier = super.toPhysicalColumnName(identifier, jdbcEnv);
        if (superIdentifier.getText().equals("year")) {
            return Identifier.toIdentifier("\"year\"");
        }
        return superIdentifier;
    }

    @Override
    public Identifier toPhysicalTableName(final Identifier identifier, final JdbcEnvironment jdbcEnv) {
        final Identifier superIdentifier = super.toPhysicalColumnName(identifier, jdbcEnv);
        if (superIdentifier.getText().equals("system_user")) {
            return Identifier.toIdentifier("\"system_user\"");
        }
        return superIdentifier;
    }

}
