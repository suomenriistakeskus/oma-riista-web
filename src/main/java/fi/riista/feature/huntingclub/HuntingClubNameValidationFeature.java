package fi.riista.feature.huntingclub;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.sql.SQOrganisation;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
public class HuntingClubNameValidationFeature {

    @Resource
    private SQLTemplates sqlTemplates;

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public boolean isClubNameDuplicate(Long clubId, String name) {
        final SQOrganisation club = SQOrganisation.organisation;

        final StringTemplate candidate = replaceAll(name.toLowerCase(), "\\W", "");
        final StringTemplate nameFi = replaceAll(club.nameFinnish.lower(), "\\W", "");
        final StringTemplate nameSv = replaceAll(club.nameSwedish.lower(), "\\W", "");
        final Boolean b = createNativeQuery()
                .select(Expressions.constant(true))
                .from(club)
                .where(club.organisationType.eq(OrganisationType.CLUB.name()),
                        clubId != null ? club.organisationId.ne(clubId) : null,
                        candidate.eq(nameFi).or(candidate.eq(nameSv)))
                .fetchOne();
        return Boolean.TRUE.equals(b);
    }

    private JPASQLQuery<Object> createNativeQuery() {
        return new JPASQLQuery<>(em, sqlTemplates);
    }

    private static StringTemplate replaceAll(String string, String pattern, String replacement) {
        return _replaceAll(string, pattern, replacement);
    }

    private static StringTemplate replaceAll(StringExpression string, String pattern, String replacement) {
        return _replaceAll(string, pattern, replacement);
    }

    private static StringTemplate _replaceAll(Object string, String pattern, String replacement) {
        final String flags = "g";// This is postgresql spesific flag. H2 also has regexp_replace, but it needs other flags or none.
        return Expressions.stringTemplate("regexp_replace({0}, {1}, {2}, {3})", string, pattern, replacement, flags);
    }
}
