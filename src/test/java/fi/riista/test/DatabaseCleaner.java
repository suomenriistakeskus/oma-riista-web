package fi.riista.test;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;

@Component
public class DatabaseCleaner {

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearManagedEntityTablesFromH2Database() {

        // Disable foreign key checks temporarily to allow clearing tables in arbitrary order.
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        // Here we want to clear only those tables that are mapped to JPA entities. Because
        // database contains also tables used to store Java enumeration names (to ensure data
        // integrity via foreign key constraints) and possibly some other static data we cannot
        // delete all tables.

        em.getMetamodel().getEntities().forEach(entityType -> {
            em.createQuery(criteriaDelete(entityType.getJavaType(), em.getCriteriaBuilder())).executeUpdate();
        });

        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    private static <T> CriteriaDelete<T> criteriaDelete(final Class<T> entityJavaType, final CriteriaBuilder cb) {
        final CriteriaDelete<T> deleteCrit = cb.createCriteriaDelete(entityJavaType);
        deleteCrit.from(entityJavaType);
        return deleteCrit;
    }

}
