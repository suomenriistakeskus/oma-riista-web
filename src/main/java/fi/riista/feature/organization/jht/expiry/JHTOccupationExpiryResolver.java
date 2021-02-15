package fi.riista.feature.organization.jht.expiry;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysEmailService;
import fi.riista.util.F;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JHTOccupationExpiryResolver {

    private static final ImmutableSet<OccupationType> OCCUPATION_TYPES = Sets.immutableEnumSet(
            OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA,
            OccupationType.METSASTAJATUTKINNON_VASTAANOTTAJA,
            OccupationType.METSASTYKSENVALVOJA,
            OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA);

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private RiistanhoitoyhdistysEmailService riistanhoitoyhdistysEmailService;

    @Resource
    private EnumLocaliser enumLocaliser;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public List<JHTOccupationExpiryDTO> resolve(final LocalDate expiryDate) {
        final QOccupation OCCUPATION = QOccupation.occupation;
        final QOrganisation RHY = QOrganisation.organisation;
        final QPerson PERSON = QPerson.person;

        return jpqlQueryFactory
                .select(OCCUPATION.occupationType,
                        OCCUPATION.endDate,
                        PERSON.firstName,
                        PERSON.lastName,
                        PERSON.email,
                        PERSON.languageCode,
                        RHY.id,
                        RHY.nameLocalisation())
                .from(OCCUPATION)
                .join(OCCUPATION.organisation, RHY)
                .join(OCCUPATION.person, PERSON)
                .where(OCCUPATION.occupationType.in(OCCUPATION_TYPES))
                .where(OCCUPATION.endDate.eq(expiryDate))
                .where(PERSON.email.isNotNull())
                .where(RHY.organisationType.eq(OrganisationType.RHY))
                .fetch().stream()
                .map(tuple -> {
                    final String languageCode = tuple.get(PERSON.languageCode);
                    final String personEmail = tuple.get(PERSON.email);
                    final Long rhyId = tuple.get(RHY.id);
                    final LocalisedString rhyName = tuple.get(RHY.nameLocalisation());
                    final OccupationType occupationType = tuple.get(OCCUPATION.occupationType);
                    final LocalDate occupationEndDate = tuple.get(OCCUPATION.endDate);
                    final String firstName = tuple.get(PERSON.firstName);
                    final String lastName = tuple.get(PERSON.lastName);
                    final String personName = String.format("%s %s", firstName, lastName);

                    final Locale locale = Locales.getLocaleByLanguageCode(languageCode);
                    final LocalisedString occupationName = enumLocaliser.getLocalisedString(occupationType);

                    return new JHTOccupationExpiryDTO(locale, occupationEndDate, personName, occupationName, personEmail, rhyId, rhyName);
                })
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Map<Long, Set<String>> resolveRhyEmails(final List<JHTOccupationExpiryDTO> dtoList) {
        return riistanhoitoyhdistysEmailService.resolveEmails(
                F.mapNonNullsToSet(dtoList, JHTOccupationExpiryDTO::getRhyId));
    }

}
