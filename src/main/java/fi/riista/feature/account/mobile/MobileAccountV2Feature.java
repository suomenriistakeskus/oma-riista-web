package fi.riista.feature.account.mobile;

import fi.riista.feature.account.AccountShootingTestDTO;
import fi.riista.feature.account.AccountShootingTestFeature;
import fi.riista.feature.account.certificate.HuntingCardQRCodeGenerator;
import fi.riista.feature.account.certificate.HuntingCardQRCodeKeyHolder;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.Locales;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.SortedSet;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static java.util.Collections.singleton;

@Component
public class MobileAccountV2Feature extends MobileAccountFeature {

    @Resource
    private AccountShootingTestFeature accountShootingTestFeature;

    @Resource
    private HuntingCardQRCodeKeyHolder keyHolder;

    @Transactional(readOnly = true)
    @Override
    public MobileAccountV2DTO getMobileAccount() {
        final Person person = activeUserService.requireActivePerson();
        final String username = activeUserService.getActiveUsernameOrNull();

        final SortedSet<Integer> harvestYears = getBeginningCalendarYearsOfHuntingYearsContainingHarvests(person);
        final SortedSet<Integer> observationYears =
                getBeginningCalendarYearsOfHuntingYearsContainingObservations(person);
        final List<Occupation> occupations =
                occupationRepository.findActiveByPersonAndOrganisationTypes(person, singleton(OrganisationType.RHY));

        final boolean shootingTestsEnabled = person.isShootingTestsEnabled()
                && occupations.stream().anyMatch(occ -> occ.getOccupationType() == AMPUMAKOKEEN_VASTAANOTTAJA);

        final String qrCode = person.canPrintCertificate()
                ? HuntingCardQRCodeGenerator.forPerson(person).build(keyHolder.getPrivateKey(), Locales.FI_LANG)
                : null;

        final List<AccountShootingTestDTO> shootingTests = accountShootingTestFeature.listMyShootingTests(person.getId());

        return MobileAccountV2DTO.create(
                username,
                person,
                person.getAddress(),
                person.getRhyMembership(),
                harvestYears,
                observationYears,
                mobileOccupationDTOFactory.create(occupations),
                shootingTestsEnabled,
                qrCode,
                shootingTests);
    }
}
