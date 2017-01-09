package fi.riista.config.jpa;

import org.hibernate.cfg.ImprovedNamingStrategy;

public class CustomHibernateNamingStrategy extends ImprovedNamingStrategy {
    @Override
    public String foreignKeyColumnName(String propertyName, String propertyEntityName,
                                       String propertyTableName, String referencedColumnName) {
        return super.foreignKeyColumnName(propertyName, propertyEntityName, propertyTableName, referencedColumnName) + "_id";
    }
}
