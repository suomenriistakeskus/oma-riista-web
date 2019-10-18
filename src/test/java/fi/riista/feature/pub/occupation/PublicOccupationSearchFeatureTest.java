package fi.riista.feature.pub.occupation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.AlueellinenRiistaneuvosto;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PublicOccupationSearchFeatureTest extends EmbeddedDatabaseTest {

    private final ImmutableList<LocalisedString> rhyNames = ImmutableList.of(
            LocalisedString.of("fi_aa", "sv_dd"),
            LocalisedString.of("fi_bb", "sv_cc"),
            LocalisedString.of("fi_cc", "sv_bb"),
            LocalisedString.of("fi_dd", "sv_aa"));

    @Resource
    private PublicOccupationSearchFeature occupationSearchFeature;

    @Nonnull
    private static CustomTypeSafeMatcher<PublicOccupationTypeDTO> equalToOccupationType(final OrganisationType organisationType,
                                                                                        final OccupationType occupationType) {
        return new CustomTypeSafeMatcher<PublicOccupationTypeDTO>(
                String.format("occupationType=%s organisationType=%s", occupationType, organisationType)) {
            @Override
            protected boolean matchesSafely(final PublicOccupationTypeDTO o) {
                return o.getOccupationType() == occupationType && o.getOrganisationType() == organisationType;
            }
        };
    }

    @Nonnull
    private static CustomTypeSafeMatcher<AddressDTO> equalToAddress(final Address address) {
        return new CustomTypeSafeMatcher<AddressDTO>("Address does not match") {
            @Override
            protected boolean matchesSafely(final AddressDTO o) {
                return Objects.equals(o.getStreetAddress(), address.getStreetAddress()) &&
                        Objects.equals(o.getCity(), address.getCity()) &&
                        Objects.equals(o.getPostalCode(), address.getPostalCode());
            }
        };
    }

    @After
    public void tearDown() {
        LocaleContextHolder.setLocale(null);
    }

    @Test
    public void testSuborganisationsInAlphabeticalOrder_finnish() {
        LocaleContextHolder.setLocale(Locales.FI);
        final List<String> orderedRhyNamesFinnish = Lists.newArrayList(
                rhyNames.get(0).getFinnish(),
                rhyNames.get(1).getFinnish(),
                rhyNames.get(2).getFinnish(),
                rhyNames.get(3).getFinnish()
        );

        assertReturnsSuborganisationsSortedByName(orderedRhyNamesFinnish);
    }

    @Test
    public void testSuborganisationsInAlphabeticalOrder_swedish() {
        LocaleContextHolder.setLocale(Locales.SV);
        final List<String> orderedRhyNamesSwedish = Lists.newArrayList(
                rhyNames.get(3).getSwedish(),
                rhyNames.get(2).getSwedish(),
                rhyNames.get(1).getSwedish(),
                rhyNames.get(0).getSwedish()
        );

        assertReturnsSuborganisationsSortedByName(orderedRhyNamesSwedish);
    }

    private void assertReturnsSuborganisationsSortedByName(List<String> orderedNamesList) {
        RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        for (LocalisedString str : rhyNames) {
            Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);
            rhy.setNameFinnish(str.getFinnish());
            rhy.setNameSwedish(str.getSwedish());
        }

        persistInNewTransaction();

        PublicOrganisationDTO result = occupationSearchFeature
                .getByTypeAndOfficialCode(OrganisationType.RKA, rka.getOfficialCode());


        final List<String> subOrganisations = result.getSubOrganisations().stream()
                .map(PublicOrganisationDTO::getName)
                .collect(toList());

        assertThat(subOrganisations, equalTo(orderedNamesList));

    }

    @Test
    public void testOccupationTypes() {
        final List<PublicOccupationTypeDTO> result = occupationSearchFeature.getAllOccupationTypes();

        assertThat(result, contains(Arrays.asList(
                equalToOccupationType(OrganisationType.RK, OccupationType.PUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.RK, OccupationType.VARAPUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.RK, OccupationType.HALLITUKSEN_JASEN),
                equalToOccupationType(OrganisationType.RK, OccupationType.HALLITUKSEN_VARAJASEN),
                equalToOccupationType(OrganisationType.VRN, OccupationType.PUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.VRN, OccupationType.VARAPUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.VRN, OccupationType.JASEN),
                equalToOccupationType(OrganisationType.VRN, OccupationType.VARAJASEN),
                equalToOccupationType(OrganisationType.ARN, OccupationType.PUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.ARN, OccupationType.VARAPUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.ARN, OccupationType.JASEN),
                equalToOccupationType(OrganisationType.ARN, OccupationType.VARAJASEN),
                equalToOccupationType(OrganisationType.RHY, OccupationType.TOIMINNANOHJAAJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.SRVA_YHTEYSHENKILO),
                equalToOccupationType(OrganisationType.RHY, OccupationType.PETOYHDYSHENKILO),
                equalToOccupationType(OrganisationType.RHY, OccupationType.METSASTYKSENVALVOJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.METSASTAJATUTKINNON_VASTAANOTTAJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.PUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.VARAPUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.HALLITUKSEN_JASEN),
                equalToOccupationType(OrganisationType.RHY, OccupationType.HALLITUKSEN_VARAJASEN),
                equalToOccupationType(OrganisationType.RHY, OccupationType.JALJESTYSKOIRAN_OHJAAJA_HIRVI),
                equalToOccupationType(OrganisationType.RHY, OccupationType.JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET),
                equalToOccupationType(OrganisationType.RHY, OccupationType.JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT)
        )));
    }

    @Test
    public void testOnlyValid() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final Occupation past = model().newOccupation(rhy, model().newPerson(), OccupationType.TOIMINNANOHJAAJA);
        past.setEndDate(DateUtil.today().minusDays(1));

        final Occupation future = model().newOccupation(rhy, model().newPerson(), OccupationType.TOIMINNANOHJAAJA);
        future.setBeginDate(DateUtil.today().plusDays(1));

        final Occupation active = model().newOccupation(rhy, model().newPerson(), OccupationType.TOIMINNANOHJAAJA);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));
        assertEquals(active.getPerson().getFullName(), result.getOccupations().get(0).getPersonName());
    }

    @Test
    public void testCoordinator() {
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys();
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys();
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        model().newOccupation(rhy1, person1, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy1, person1, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(rhy2, person2, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy2, person2, OccupationType.METSASTYKSENVALVOJA);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .build());

        assertThat(result.getOccupations(), hasSize(2));
        assertThat(result.getOrganisations(), hasSize(2));
    }

    @Test
    public void testCoordinator_UseRhyInformation() {
        // RHY has address
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setAddress(model().newAddress());
        rhy.setEmail(model().zeroPaddedNumber(3) + "@invalid");
        rhy.setPhoneNumber(model().phoneNumber());

        // Coordinator has address, email and phoneNumber
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.TOIMINNANOHJAAJA);
        person.setMrAddress(model().newAddress());
        person.setPhoneNumber(model().phoneNumber());

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));

        final PublicOrganisationDTO rhyDTO = result.getOrganisations().get(0);
        assertThat(rhyDTO.getAddress(), equalToAddress(rhy.getAddress()));
        assertEquals(rhyDTO.getPhoneNumber(), rhy.getPhoneNumber());
        assertEquals(rhyDTO.getEmail(), rhy.getEmail());
    }

    @Test
    public void testCoordinator_UseCoordinatorInformation() {
        // RHY does not have address, email or phoneNumber
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setAddress(null);
        rhy.setEmail(null);
        rhy.setPhoneNumber(null);

        // Coordinator has address
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.TOIMINNANOHJAAJA);
        person.setMrAddress(model().newAddress());
        person.setPhoneNumber(model().phoneNumber());

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));

        final PublicOrganisationDTO rhyDTO = result.getOrganisations().get(0);
        assertThat(rhyDTO.getAddress(), equalToAddress(person.getMrAddress()));
        assertEquals(rhyDTO.getPhoneNumber(), person.getPhoneNumber());
        assertEquals(rhyDTO.getEmail(), person.getEmail());
    }

    @Test
    public void testRhyGetsContactInformationFromCoordinator() {
        final RiistakeskuksenAlue riistakeskuksenAlue = model().newRiistakeskuksenAlue();
        // RHY does not have address, email or phoneNumber
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(riistakeskuksenAlue);
        rhy.setAddress(null);
        rhy.setEmail(null);
        rhy.setPhoneNumber(null);

        // Coordinator has address
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.TOIMINNANOHJAAJA);
        person.setMrAddress(model().newAddress());
        person.setPhoneNumber(model().phoneNumber());

        persistInNewTransaction();

        final PublicOrganisationDTO byTypeAndOfficialCode = occupationSearchFeature
                .getByTypeAndOfficialCode(OrganisationType.RKA, riistakeskuksenAlue.getOfficialCode());

        assertThat(byTypeAndOfficialCode.getSubOrganisations(), hasSize(1));

        final List<PublicOrganisationDTO> subOrgs = byTypeAndOfficialCode.getSubOrganisations();

        final PublicOrganisationDTO rhyDTO = subOrgs.get(0);
        assertThat(rhyDTO.getAddress(), equalToAddress(person.getMrAddress()));
        assertEquals(rhyDTO.getPhoneNumber(), person.getPhoneNumber());
        assertEquals(rhyDTO.getEmail(), person.getEmail());
    }

    @Test
    public void testRhyGetsContactInformationFromCoordinator_onlyMissingInfoFromCoordinator() {
        final RiistakeskuksenAlue riistakeskuksenAlue = model().newRiistakeskuksenAlue();
        // RHY does not have address, but has email and phoneNumber
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(riistakeskuksenAlue);
        rhy.setAddress(null);
        rhy.setEmail("email@invalid");
        rhy.setPhoneNumber("1234567");

        // Coordinator has all fields
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.TOIMINNANOHJAAJA);
        person.setMrAddress(model().newAddress());
        person.setPhoneNumber(model().phoneNumber());
        person.setEmail("person@invalid");
        person.setPhoneNumber("1111111");

        persistInNewTransaction();

        final PublicOrganisationDTO byTypeAndOfficialCode = occupationSearchFeature
                .getByTypeAndOfficialCode(OrganisationType.RKA, riistakeskuksenAlue.getOfficialCode());

        assertThat(byTypeAndOfficialCode.getSubOrganisations(), hasSize(1));

        final List<PublicOrganisationDTO> subOrgs = byTypeAndOfficialCode.getSubOrganisations();

        final PublicOrganisationDTO rhyDTO = subOrgs.get(0);
        assertThat(rhyDTO.getAddress(), equalToAddress(person.getMrAddress()));
        assertEquals(rhyDTO.getPhoneNumber(), rhy.getPhoneNumber());
        assertEquals(rhyDTO.getEmail(), rhy.getEmail());
    }

    @Test
    public void testRhyGetsContactInformationFromCoordinator_nullWhenNoCoordinatorInfo() {
        final RiistakeskuksenAlue riistakeskuksenAlue = model().newRiistakeskuksenAlue();
        // RHY does not have address, email or phoneNumber
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(riistakeskuksenAlue);
        rhy.setAddress(null);
        rhy.setEmail(null);
        rhy.setPhoneNumber(null);

        // Coordinator has address
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.TOIMINNANOHJAAJA);
        person.setMrAddress(null);
        person.setOtherAddress(null);
        person.setPhoneNumber(null);
        person.setEmail(null);

        persistInNewTransaction();

        final PublicOrganisationDTO byTypeAndOfficialCode = occupationSearchFeature
                .getByTypeAndOfficialCode(OrganisationType.RKA, riistakeskuksenAlue.getOfficialCode());

        assertThat(byTypeAndOfficialCode.getSubOrganisations(), hasSize(1));

        final List<PublicOrganisationDTO> subOrgs = byTypeAndOfficialCode.getSubOrganisations();

        final PublicOrganisationDTO rhyDTO = subOrgs.get(0);
        assertNull(rhyDTO.getAddress());
        assertNull(rhyDTO.getPhoneNumber());
        assertNull(rhyDTO.getEmail());
    }

    @Test
    public void testSrva_ByArea() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.SRVA_YHTEYSHENKILO);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.SRVA_YHTEYSHENKILO)
                        .withOrganisationType(OrganisationType.RHY)
                        .withAreaId(rka.getOfficialCode())
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));
    }

    @Test
    public void testSrva_ByRhy() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.SRVA_YHTEYSHENKILO);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.SRVA_YHTEYSHENKILO)
                        .withOrganisationType(OrganisationType.RHY)
                        .withRhyId(rhy.getOfficialCode())
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));
    }

    @Test
    public void testFindsOccupationsNotAttachedToRhy() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final AlueellinenRiistaneuvosto arn = model().newAlueellinenRiistaneuvosto(rka, "fi", "sv");

        final Person person = model().newPerson();
        model().newOccupation(arn, person, OccupationType.PUHEENJOHTAJA);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withPageSize(5)
                        .withPageNumber(0)
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));
        Assert.assertEquals(
                OccupationType.PUHEENJOHTAJA,
                result.getOccupations().iterator().next().getOccupationType().getOccupationType());
        Assert.assertEquals(OrganisationType.ARN, result.getOrganisations().iterator().next().getOrganisationType());
    }

    @Test
    public void testFindsOccupationsNotAttachedToRhy_withAreaCriteria() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final AlueellinenRiistaneuvosto arn = model().newAlueellinenRiistaneuvosto(rka, "fi", "sv");

        final Person person = model().newPerson();
        model().newOccupation(arn, person, OccupationType.PUHEENJOHTAJA);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withAreaId(rka.getOfficialCode())
                        .withPageSize(5)
                        .withPageNumber(0)
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));
        Assert.assertEquals(
                OccupationType.PUHEENJOHTAJA,
                result.getOccupations().iterator().next().getOccupationType().getOccupationType());
        Assert.assertEquals(OrganisationType.ARN, result.getOrganisations().iterator().next().getOrganisationType());
    }

    @Test
    public void testFindsOccupationsOnRkLevel() {

        final Person person = model().newPerson();
        model().newOccupation(getRiistakeskus(), person, OccupationType.PUHEENJOHTAJA);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withPageSize(5)
                        .withPageNumber(0)
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));
        Assert.assertEquals(
                OccupationType.PUHEENJOHTAJA,
                result.getOccupations().iterator().next().getOccupationType().getOccupationType());
        Assert.assertEquals(OrganisationType.RK, result.getOrganisations().iterator().next().getOrganisationType());
    }

    @Test
    public void testDoesNotReturnClubValues() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HuntingClub club = model().newHuntingClub(rhy);
        final Person person = model().newPerson();
        model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withPageSize(5)
                        .withPageNumber(0)
                        .build());

        assertThat(result.getOccupations(), hasSize(0));
        assertThat(result.getOrganisations(), hasSize(0));
    }

    @Test
    public void testMaxResults() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final int maxResults = 3;

        for (int i = 0; i < maxResults; i++) {
            model().newOccupation(rhy, model().newPerson(), OccupationType.TOIMINNANOHJAAJA);
        }

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .build(),
                maxResults);

        assertFalse(result.isTooManyResults());
        assertThat(result.getOccupations(), hasSize(maxResults));
    }

    @Test
    public void testMoreThanMaxResults() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final int maxResults = 3;

        for (int i = 0; i < maxResults + 1; i++) {
            model().newOccupation(rhy, model().newPerson(), OccupationType.TOIMINNANOHJAAJA);
        }

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .build(),
                maxResults);

        assertTrue(result.isTooManyResults());
        assertThat(result.getOccupations(), hasSize(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequestedPageSizeCannotExceedMaxResults() {

        final int maxResults = 3;


        occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .withPageSize(maxResults + 1)
                        .withPageNumber(0)
                        .build(),
                maxResults);

        Assert.fail("Should have thrown an exception");
    }

    @Test
    public void testPaging() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final int firstPageOffset = 0;
        final int secondPageOffset = 1;

        final int maxResults = 3;
        final int pageSize = maxResults;

        final int numberOfOccupations = maxResults + 1;

        for (int i = 0; i < numberOfOccupations; i++) {
            model().newOccupation(rhy, model().newPerson(), OccupationType.TOIMINNANOHJAAJA);
        }

        persistInNewTransaction();

        // Fetch first page
        final PublicOccupationsAndOrganisationsDTO firstPage = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .withPageNumber(firstPageOffset)
                        .withPageSize(pageSize)
                        .build(),
                maxResults);

        assertFalse(firstPage.isTooManyResults());
        assertFalse(firstPage.isLastPage());
        assertThat(firstPage.getOccupations(), hasSize(pageSize));


        // Fetch second page
        final PublicOccupationsAndOrganisationsDTO secondPage = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .withPageNumber(secondPageOffset)
                        .withPageSize(pageSize)
                        .build(),
                maxResults);

        assertFalse(secondPage.isTooManyResults());
        assertTrue(secondPage.isLastPage());
        assertThat(secondPage.getOccupations(), hasSize(numberOfOccupations - pageSize));
    }

    @Test
    public void testSorting_byOccupationTypeFirst() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(rhy, person, OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withRhyId(rhy.getOfficialCode())
                        .build());

        final List<PublicOccupationDTO> occupations = result.getOccupations();

        assertThat(occupations, hasSize(2));
        final OccupationType firstOccupationType = occupations.get(0).getOccupationType().getOccupationType();
        final OccupationType secondOccupationType = occupations.get(1).getOccupationType().getOccupationType();
        Assert.assertTrue(firstOccupationType.ordinal() < secondOccupationType.ordinal());
    }

    @Test
    public void testSorting_byOrganisationIdSecond() {
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys();
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys();
        final Person person = model().newPerson();
        model().newOccupation(rhy1, person, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(rhy2, person, OccupationType.SRVA_YHTEYSHENKILO);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.SRVA_YHTEYSHENKILO)
                        .withOrganisationType(OrganisationType.RHY)
                        .build());

        final List<PublicOccupationDTO> occupations = result.getOccupations();

        assertThat(occupations, hasSize(2));
        final long firstOrganisationId = occupations.get(0).getOrganisationId();
        final long secondOrganisationId = occupations.get(1).getOrganisationId();
        Assert.assertTrue(firstOrganisationId < secondOrganisationId);
    }

    @Test
    public void testSorting_byCallOrderThird() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person person = model().newPerson();
        final Person person2 = model().newPerson();
        final Occupation occ1 = model().newOccupation(rhy, person, OccupationType.SRVA_YHTEYSHENKILO);
        occ1.setCallOrder(2);
        final Occupation occ2 = model().newOccupation(rhy, person2, OccupationType.SRVA_YHTEYSHENKILO);
        occ2.setCallOrder(1);
        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withRhyId(rhy.getOfficialCode())
                        .build());

        final List<PublicOccupationDTO> occupations = result.getOccupations();

        assertThat(occupations, hasSize(2));
        final String firstName = occupations.get(0).getPersonName();
        final String secondName = occupations.get(1).getPersonName();
        assertThat(firstName, equalTo(person2.getFullName()));
        assertThat(secondName, equalTo(person.getFullName()));
    }


    @Test
    public void testSorting_byOccupationIdFourth() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person person1 = model().newPerson("Matti", "Möttönen", ssn(), hunterNumber());
        final Person person2 = model().newPerson("Anna", "Annunen", ssn(), hunterNumber());
        model().newOccupation(rhy, person1, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(rhy, person2, OccupationType.SRVA_YHTEYSHENKILO);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.SRVA_YHTEYSHENKILO)
                        .withOrganisationType(OrganisationType.RHY)
                        .build());

        final List<PublicOccupationDTO> occupations = result.getOccupations();

        assertThat(occupations, hasSize(2));
        final String firstPersonName = occupations.get(0).getPersonName();
        final String secondPersonName = occupations.get(1).getPersonName();
        Assert.assertTrue(firstPersonName.contains(person1.getFirstName()));
        Assert.assertTrue(secondPersonName.contains(person2.getFirstName()));
    }

}
