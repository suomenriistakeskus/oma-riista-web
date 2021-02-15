package fi.riista.integration.srva.callring;

import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.SrvaRotation;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static fi.riista.util.DateUtil.today;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

public class SrvaCallRingConfigurationServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private SrvaCallRingConfigurationService srvaCallRingConfigurationService;

    private Occupation createSrvaOccupation(final Riistanhoitoyhdistys rhy, final Integer callOrder) {
        final Occupation occupation = model().newOccupation(rhy, model().newPerson(), OccupationType.SRVA_YHTEYSHENKILO);
        occupation.setCallOrder(callOrder);
        return occupation;
    }

    private SrvaCallRingConfiguration getConfiguration(final Riistanhoitoyhdistys rhy) {
        final List<SrvaCallRingConfiguration> callRingConfigurations =
                callInTransaction(srvaCallRingConfigurationService::generateConfigurationForEveryRhy);

        final Map<String, SrvaCallRingConfiguration> rhyToConfiguration =
                F.index(callRingConfigurations, SrvaCallRingConfiguration::getRhyOfficialCode);

        assertThat(rhyToConfiguration, hasKey(rhy.getOfficialCode()));

        return rhyToConfiguration.get(rhy.getOfficialCode());
    }

    private void assertRhyCallRing(final Riistanhoitoyhdistys rhy, final String... phoneNumbers) {
        final SrvaCallRingConfiguration rhyConfiguration = getConfiguration(rhy);

        final List<Matcher<? super String>> matcherList = Arrays.stream(phoneNumbers)
                .map(p -> p.substring(1))
                .map(Matchers::equalTo)
                .collect(toList());

        final List<Matcher<? super String>> repeatedMatcherList = new LinkedList<>();
        repeatedMatcherList.addAll(matcherList);
        repeatedMatcherList.addAll(matcherList);

        assertThat(rhyConfiguration.getFormattedPhoneNumbers(), contains(repeatedMatcherList));
    }

    private void assertNotificationEmails(final Riistanhoitoyhdistys rhy, final String... emails) {
        final SrvaCallRingConfiguration rhyConfiguration = getConfiguration(rhy);

        final List<Matcher<? super String>> matcherList = Arrays.stream(emails)
                .map(Matchers::equalTo)
                .collect(toList());

        assertThat(rhyConfiguration.getNotificationEmails(), contains(matcherList));
    }

    @Test
    public void testOccupationOrder() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        // Randomize insertion order
        final Occupation srva1 = createSrvaOccupation(rhy, 1);
        final Occupation srva3 = createSrvaOccupation(rhy, 3);
        final Occupation srva4 = createSrvaOccupation(rhy, null);
        final Occupation srva2 = createSrvaOccupation(rhy, 2);

        persistInNewTransaction();

        assertRhyCallRing(rhy,
                srva1.getPerson().getPhoneNumber(),
                srva2.getPerson().getPhoneNumber(),
                srva3.getPerson().getPhoneNumber(),
                srva4.getPerson().getPhoneNumber());
    }

    @Test
    public void testOccupationOrder_rotationDays() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setSrvaRotation(SrvaRotation.DAILY);
        rhy.setRotationStart(today().minusDays(1));

        // Randomize insertion order
        final Occupation srva1 = createSrvaOccupation(rhy, 1);
        final Occupation srva3 = createSrvaOccupation(rhy, 3);
        final Occupation srva4 = createSrvaOccupation(rhy, null);
        final Occupation srva2 = createSrvaOccupation(rhy, 2);

        persistInNewTransaction();

        // First one should be moved to last
        assertRhyCallRing(rhy,
                srva2.getPerson().getPhoneNumber(),
                srva3.getPerson().getPhoneNumber(),
                srva4.getPerson().getPhoneNumber(),
                srva1.getPerson().getPhoneNumber());
    }

    @Test
    public void testOccupationOrder_rotationWeeks() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setSrvaRotation(SrvaRotation.WEEKLY);
        rhy.setRotationStart(today().minusWeeks(2));

        // Randomize insertion order
        final Occupation srva1 = createSrvaOccupation(rhy, 1);
        final Occupation srva3 = createSrvaOccupation(rhy, 3);
        final Occupation srva4 = createSrvaOccupation(rhy, null);
        final Occupation srva2 = createSrvaOccupation(rhy, 2);

        persistInNewTransaction();

        // First one should be moved to last two times
        assertRhyCallRing(rhy,
                srva3.getPerson().getPhoneNumber(),
                srva4.getPerson().getPhoneNumber(),
                srva1.getPerson().getPhoneNumber(),
                srva2.getPerson().getPhoneNumber());
    }

    @Test
    public void testOccupationOrder_rotationMonths() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setSrvaRotation(SrvaRotation.MONTHLY);
        rhy.setRotationStart(today().minusMonths(3));

        // Randomize insertion order
        final Occupation srva1 = createSrvaOccupation(rhy, 1);
        final Occupation srva3 = createSrvaOccupation(rhy, 3);
        final Occupation srva4 = createSrvaOccupation(rhy, null);
        final Occupation srva2 = createSrvaOccupation(rhy, 2);

        persistInNewTransaction();

        // First one should be moved to last three times
        assertRhyCallRing(rhy,
                srva4.getPerson().getPhoneNumber(),
                srva1.getPerson().getPhoneNumber(),
                srva2.getPerson().getPhoneNumber(),
                srva3.getPerson().getPhoneNumber());
    }

    @Test
    public void testOccupationOrder_rotationStartDateInFuture() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setSrvaRotation(SrvaRotation.DAILY);
        rhy.setRotationStart(today().plusDays(3));

        // Randomize insertion order
        final Occupation srva1 = createSrvaOccupation(rhy, 1);
        final Occupation srva3 = createSrvaOccupation(rhy, 3);
        final Occupation srva4 = createSrvaOccupation(rhy, null);
        final Occupation srva2 = createSrvaOccupation(rhy, 2);

        persistInNewTransaction();

        // No rotation until start
        assertRhyCallRing(rhy,
                srva1.getPerson().getPhoneNumber(),
                srva2.getPerson().getPhoneNumber(),
                srva3.getPerson().getPhoneNumber(),
                srva4.getPerson().getPhoneNumber());
    }
    @Test
    public void testOccupationOrder_fullRound() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setSrvaRotation(SrvaRotation.DAILY);
        rhy.setRotationStart(today().plusDays(4));

        // Randomize insertion order
        final Occupation srva1 = createSrvaOccupation(rhy, 1);
        final Occupation srva3 = createSrvaOccupation(rhy, 3);
        final Occupation srva4 = createSrvaOccupation(rhy, null);
        final Occupation srva2 = createSrvaOccupation(rhy, 2);

        persistInNewTransaction();

        assertRhyCallRing(rhy,
                srva1.getPerson().getPhoneNumber(),
                srva2.getPerson().getPhoneNumber(),
                srva3.getPerson().getPhoneNumber(),
                srva4.getPerson().getPhoneNumber());
    }

    @Test
    public void testMultipleRhyAndRka() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rka1_rhy1 = model().newRiistanhoitoyhdistys(rka1);
        final Riistanhoitoyhdistys rka1_rhy2 = model().newRiistanhoitoyhdistys(rka1);
        final Riistanhoitoyhdistys rka2_rhy1 = model().newRiistanhoitoyhdistys(rka2);

        // RKA 1 - RHY 1
        final Occupation rka1_rhy1_srva1 = createSrvaOccupation(rka1_rhy1, 1);
        final Occupation rka1_rhy1_srva2 = createSrvaOccupation(rka1_rhy1, 2);

        // RKA 1 - RHY 2
        final Occupation rka1_rhy2_srva1 = createSrvaOccupation(rka1_rhy2, 1);
        final Occupation rka1_rhy2_srva2 = createSrvaOccupation(rka1_rhy2, 2);

        // RKA 2 - RHY 1
        final Occupation rka2_rhy1_srva1 = createSrvaOccupation(rka2_rhy1, 1);
        final Occupation rka2_rhy1_srva2 = createSrvaOccupation(rka2_rhy1, 2);

        persistInNewTransaction();

        assertRhyCallRing(rka1_rhy1,
                rka1_rhy1_srva1.getPerson().getPhoneNumber(),
                rka1_rhy1_srva2.getPerson().getPhoneNumber());

        assertRhyCallRing(rka1_rhy2,
                rka1_rhy2_srva1.getPerson().getPhoneNumber(),
                rka1_rhy2_srva2.getPerson().getPhoneNumber());

        assertRhyCallRing(rka2_rhy1,
                rka2_rhy1_srva1.getPerson().getPhoneNumber(),
                rka2_rhy1_srva2.getPerson().getPhoneNumber());
    }

    @Test
    public void testInvalidPhoneNumber() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final Occupation srva1 = createSrvaOccupation(rhy, 1);
        final Occupation srva2 = createSrvaOccupation(rhy, 2);
        srva1.getPerson().setPhoneNumber("junk");

        persistInNewTransaction();

        assertRhyCallRing(rhy,
                srva2.getPerson().getPhoneNumber());
    }

    @Test
    public void testOnlySrvaOccupations() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final Occupation srva = createSrvaOccupation(rhy, 1);
        model().newOccupation(rhy, model().newPerson(), OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy, model().newPerson(), OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA);
        model().newOccupation(rhy, model().newPerson(), OccupationType.METSASTAJATUTKINNON_VASTAANOTTAJA);

        persistInNewTransaction();

        assertRhyCallRing(rhy,
                srva.getPerson().getPhoneNumber());
    }

    @Test
    public void testDuplicateOccupations() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person samePerson = model().newPerson();
        final Person otherPerson = model().newPerson();

        final Occupation srva1 = model().newOccupation(rhy, samePerson, OccupationType.SRVA_YHTEYSHENKILO);
        srva1.setCallOrder(1);
        final Occupation srva2 = model().newOccupation(rhy, samePerson, OccupationType.SRVA_YHTEYSHENKILO);
        srva2.setCallOrder(2);

        final Occupation srva3 = model().newOccupation(rhy, otherPerson, OccupationType.SRVA_YHTEYSHENKILO);
        srva3.setCallOrder(3);
        final Occupation srva4 = model().newOccupation(rhy, otherPerson, OccupationType.SRVA_YHTEYSHENKILO);
        srva4.setCallOrder(3);

        final Occupation srva5 = model().newOccupation(rhy, model().newPerson(), OccupationType.SRVA_YHTEYSHENKILO);
        srva5.setCallOrder(3);
        final Occupation srva6 = model().newOccupation(rhy, model().newPerson(), OccupationType.SRVA_YHTEYSHENKILO);
        srva6.setCallOrder(3);

        persistInNewTransaction();

        assertRhyCallRing(rhy,
                srva1.getPerson().getPhoneNumber(),
                srva3.getPerson().getPhoneNumber(),
                srva5.getPerson().getPhoneNumber(),
                srva6.getPerson().getPhoneNumber());
    }

    @Test
    public void testFallbackToRhyContactPersonEmail() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setEmail(null);

        final Person contactPerson = model().newPerson();
        model().newOccupation(rhy, contactPerson, OccupationType.TOIMINNANOHJAAJA);
        contactPerson.setEmail("contact@invalid");

        createSrvaOccupation(rhy, 1);

        persistInNewTransaction();

        assertNotificationEmails(rhy, "contact@invalid");
    }

    @Test
    public void testRhyEmailAsNotificationReceiver() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setEmail("rhy@invalid");

        final Person contactPerson = model().newPerson();
        model().newOccupation(rhy, contactPerson, OccupationType.TOIMINNANOHJAAJA);
        contactPerson.setEmail("contact@invalid");

        createSrvaOccupation(rhy, 1);

        persistInNewTransaction();

        assertNotificationEmails(rhy, "rhy@invalid");
    }

    @Test
    public void testConfiguresOnlyActiveRhys() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Riistanhoitoyhdistys inactiveRhy = model().newRiistanhoitoyhdistys();
        inactiveRhy.setActive(false);
        persistInNewTransaction();

        final List<SrvaCallRingConfiguration> callRingConfigurations =
                callInTransaction(srvaCallRingConfigurationService::generateConfigurationForEveryRhy);
        assertThat(callRingConfigurations, hasSize(1));
        final SrvaCallRingConfiguration configuration = callRingConfigurations.get(0);
        assertEquals(rhy.getOfficialCode(), configuration.getRhyOfficialCode());
        assertThat(configuration.getFormattedPhoneNumbers(), hasSize(0));
    }
}
