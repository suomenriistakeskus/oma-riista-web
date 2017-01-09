package fi.riista.integration.lupahallinta;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.address.QAddress;
import fi.riista.feature.organization.lupahallinta.QLHOrganisation;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.integration.lupahallinta.club.LHHuntingClubCSVRow;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HuntingClubExportToLupahallintaFeature {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUPAHALLINTA_HUNTINGCLUBS')")
    public List<LHHuntingClubCSVRow> exportToCSCV() {
        final QHuntingClub huntingClub = QHuntingClub.huntingClub;
        final QOccupation occupation = QOccupation.occupation;
        final QPerson person = QPerson.person;
        final QAddress address = QAddress.address;

        final QOrganisation clubRhy = new QOrganisation("club_rhy");
        final QOrganisation clubRka = new QOrganisation("club_rka");

        final QRiistanhoitoyhdistys personRhy = new QRiistanhoitoyhdistys("person_rhy");
        final QOrganisation personRka = new QOrganisation("person_rka");

        final QLHOrganisation lhOrg = QLHOrganisation.lHOrganisation;

        final List<Tuple> res = new JPAQuery<>(entityManager)
                .select(occupation, huntingClub, clubRhy.officialCode, clubRka.officialCode, personRhy.officialCode, personRka.officialCode)
                .from(occupation)
                .join(occupation.organisation, huntingClub._super)
                .join(huntingClub.parentOrganisation, clubRhy)
                .join(clubRhy.parentOrganisation, clubRka)
                .join(occupation.person, person).fetchJoin()
                .leftJoin(person.mrAddress, address).fetchJoin()
                .leftJoin(person.rhyMembership, personRhy)
                .leftJoin(personRhy.parentOrganisation, personRka)
                .where(occupation.occupationType.eq(OccupationType.SEURAN_YHDYSHENKILO)
                        .and(occupation.validAndNotDeleted())
                        .and(huntingClub.officialCode.in(JPAExpressions.select(lhOrg.officialCode).from(lhOrg))))
                // Ordering is relevant:
                // same club occupations after eachothers,
                // then the smallest call order first nulls last,
                // then by person name.
                .orderBy(huntingClub.id.asc(),
                        occupation.callOrder.asc().nullsLast(),
                        person.lastName.asc(),
                        person.byName.asc()
                )
                .fetch();

        return res.stream()
                .map(t -> LHHuntingClubCSVRow.create(
                        t.get(huntingClub), t.get(clubRhy.officialCode), t.get(clubRka.officialCode),
                        t.get(occupation).getPerson(), t.get(personRhy.officialCode), t.get(personRka.officialCode)))
                .collect(Collectors.toList());
    }
}
