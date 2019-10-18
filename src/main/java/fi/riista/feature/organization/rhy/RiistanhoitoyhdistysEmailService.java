package fi.riista.feature.organization.rhy;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.QPerson;
import io.vavr.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.util.Collect.tuplesToMap;
import static fi.riista.util.EmailSanitizer.sanitize;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

@Service
public class RiistanhoitoyhdistysEmailService {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Set<String> resolveEmails(@Nonnull final Organisation rhy) {
        requireNonNull(rhy);

        if (rhy.getOrganisationType() != OrganisationType.RHY) {
            return emptySet();
        }

        return sanitize(rhy.getEmail())
                .map(Collections::singleton)
                .orElseGet(() -> sanitize(findFallbackCoordinatorEmails(rhy)));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Long, Set<String>> resolveEmails(@Nonnull final Set<Long> rhyIds) {
        requireNonNull(rhyIds);

        if (rhyIds.isEmpty()) {
            return emptyMap();
        }

        final Map<Long, String> sanitizedRhyEmailIndex = findSanitizedRhyEmails(rhyIds);
        final Set<Long> fallbackRhyIds = Sets.difference(rhyIds, sanitizedRhyEmailIndex.keySet());
        final Map<Long, Set<String>> coordinatorEmailIndex = findFallbackCoordinatorEmails(fallbackRhyIds);

        return Maps.toMap(rhyIds, rhyId -> {
            final String rhyEmail = sanitizedRhyEmailIndex.get(rhyId);

            if (rhyEmail != null) {
                return singleton(rhyEmail);
            }

            return sanitize(coordinatorEmailIndex.get(rhyId));
        });
    }

    private Map<Long, String> findSanitizedRhyEmails(final Collection<Long> rhyIds) {
        if (rhyIds.isEmpty()) {
            return emptyMap();
        }

        final QOrganisation RHY = QOrganisation.organisation;

        return jpqlQueryFactory
                .select(RHY.id, RHY.email)
                .from(RHY)
                .where(RHY.organisationType.eq(OrganisationType.RHY))
                .where(RHY.id.in(rhyIds))
                .where(RHY.email.isNotNull())
                .fetch()
                .stream()
                .map(t -> {
                    final Long rhyId = t.get(RHY.id);
                    final String rhyEmail = t.get(RHY.email);

                    return sanitize(rhyEmail)
                            .map(sanitizedEmail -> Tuple.of(rhyId, sanitizedEmail))
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(tuplesToMap());
    }

    private Set<String> findFallbackCoordinatorEmails(final Organisation rhy) {
        final Long rhyId = rhy.getId();
        final Set<String> coordinatorEmails = findFallbackCoordinatorEmails(singleton(rhyId)).get(rhyId);
        return coordinatorEmails != null ? coordinatorEmails : emptySet();
    }

    private Map<Long, Set<String>> findFallbackCoordinatorEmails(final Collection<Long> rhyIds) {
        if (rhyIds.isEmpty()) {
            return emptyMap();
        }

        final QOccupation OCCUPATION = QOccupation.occupation;
        final QOrganisation RHY = QOrganisation.organisation;
        final QPerson PERSON = QPerson.person;

        return jpqlQueryFactory
                .select(RHY.id, PERSON.email)
                .from(OCCUPATION)
                .join(OCCUPATION.organisation, RHY)
                .join(OCCUPATION.person, PERSON)
                .where(PERSON.email.isNotNull())
                .where(OCCUPATION.occupationType.eq(TOIMINNANOHJAAJA))
                .where(OCCUPATION.validAndNotDeleted())
                .where(RHY.organisationType.eq(OrganisationType.RHY))
                .where(RHY.id.in(rhyIds))
                .transform(GroupBy.groupBy(RHY.id).as(GroupBy.set(PERSON.email)));
    }
}
