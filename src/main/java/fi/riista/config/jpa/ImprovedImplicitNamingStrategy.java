package fi.riista.config.jpa;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyHbmImpl;

public class ImprovedImplicitNamingStrategy extends ImplicitNamingStrategyLegacyHbmImpl {
    @Override
    public Identifier determineJoinColumnName(final ImplicitJoinColumnNameSource source) {
        final String name;

        if (source.getNature() == ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION
                || source.getAttributePath() == null) {
            name = transformEntityName(source.getEntityNaming()) + "_id";
        } else {
            name = transformAttributePath(source.getAttributePath()) + "_id";
        }

        return toIdentifier(name, source.getBuildingContext());
    }
}
