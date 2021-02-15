package fi.riista.feature.harvestpermit.contactsearch;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitContactPerson;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Service
public class HarvestPermitContactSearchFeature {

    @Resource
    private JPAQueryFactory jpaQueryFactory;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<PermitContactSearchResultDTO> searchPermitContacts(final List<PermitContactSearchConditionDTO> searchParams, final Locale locale) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QHarvestPermitContactPerson CONTACT = QHarvestPermitContactPerson.harvestPermitContactPerson;
        final QPerson PERSON = QPerson.person;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QOrganisation RKA = RHY.parentOrganisation;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;

        final BooleanExpression expression = searchParams.stream()
                .map(param -> {
                    final BooleanExpression categoryExpression = Optional.ofNullable(param.getHarvestPermitCategory())
                            .map(APPLICATION.harvestPermitCategory::eq).orElse(null);

                    final BooleanExpression yearExpression = Optional.ofNullable(param.getHuntingYear())
                            .map(PERMIT.permitYear::eq).orElse(null);

                    return categoryExpression != null ? categoryExpression.and(yearExpression) :
                            yearExpression;
                })
                .filter(expr -> expr != null)
                .reduce(BooleanExpression::or)
                .orElse(null);

        final List<PermitContactSearchResultDTO> originalContacts = jpaQueryFactory
                .select(
                        APPLICATION.harvestPermitCategory,
                        PERMIT,
                        PERSON.firstName,
                        PERSON.lastName,
                        PERSON.email,
                        RHY,
                        RKA)
                .from(PERMIT)
                .join(PERMIT.originalContactPerson, PERSON)
                .join(PERMIT.rhy, RHY)
                .join(PERMIT.permitDecision, DECISION)
                .join(DECISION.application, APPLICATION)
                .where(expression)
                .fetch()
                .stream()
                .map(t -> {
                    final HarvestPermit permit = t.get(PERMIT);
                    final boolean hasPermission = activeUserService.checkHasPermission(permit, EntityPermission.READ);

                    return hasPermission ?
                            new PermitContactSearchResultDTO(
                                    t.get(APPLICATION.harvestPermitCategory),
                                    permit.getPermitYear(),
                                    t.get(RKA).getNameLocalisation(),
                                    t.get(RHY).getNameLocalisation(),
                                    t.get(PERSON.firstName),
                                    t.get(PERSON.lastName),
                                    t.get(PERSON.email)) :
                            null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        final List<PermitContactSearchResultDTO> additionalContacts = jpaQueryFactory
                .select(
                        APPLICATION.harvestPermitCategory,
                        PERMIT.permitYear,
                        PERSON.firstName,
                        PERSON.lastName,
                        PERSON.email,
                        RHY,
                        RKA)
                .from(CONTACT)
                .join(CONTACT.contactPerson, PERSON)
                .join(CONTACT.harvestPermit, PERMIT)
                .join(PERMIT.rhy, RHY)
                .join(PERMIT.permitDecision, DECISION)
                .join(DECISION.application, APPLICATION)
                .where(expression)
                .fetch()
                .stream()
                .map(t -> new PermitContactSearchResultDTO(
                        t.get(APPLICATION.harvestPermitCategory),
                        t.get(PERMIT.permitYear),
                        t.get(RKA).getNameLocalisation(),
                        t.get(RHY).getNameLocalisation(),
                        t.get(PERSON.firstName),
                        t.get(PERSON.lastName),
                        t.get(PERSON.email))
                )
                .collect(Collectors.toList());

        return F.concat(originalContacts, additionalContacts)
                .stream().distinct()
                .sorted(permitContactComparator(locale))
                .collect(Collectors.toList());
    }

    private Comparator<PermitContactSearchResultDTO> permitContactComparator(final Locale locale) {
        return comparing(PermitContactSearchResultDTO::getHarvestPermitCategory)
                .thenComparing(PermitContactSearchResultDTO::getHuntingYear)
                .thenComparing(result -> result.getRka().getAnyTranslation(locale))
                .thenComparing(result -> result.getRhy().getAnyTranslation(locale))
                .thenComparing(PermitContactSearchResultDTO::getName);
    }
}
