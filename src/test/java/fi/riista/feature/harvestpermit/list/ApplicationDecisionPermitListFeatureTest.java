package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.DocumentNumberUtil;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MOOSELIKE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApplicationDecisionPermitListFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private ApplicationDecisionPermitListFeature applicationDecisionPermitListFeature;

    @Test
    public void testSmoke() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermitArea area = model().newHarvestPermitArea();
        final Person person = model().newPerson();

        final HarvestPermitApplication application1 = model().newHarvestPermitApplication(rhy, area, MOOSELIKE);
        application1.setApplicationNumber(20001);
        application1.setContactPerson(person);

        final HarvestPermitApplication application2 = model().newHarvestPermitApplication(rhy, area, MOOSELIKE);
        application2.setApplicationNumber(20002);
        application2.setContactPerson(person);
        // this decision won't be listed because not published
        final PermitDecision decision2 = model().newPermitDecision(application2);
        decision2.setStatusDraft();

        final HarvestPermitApplication application3 = model().newHarvestPermitApplication(rhy, area, MOOSELIKE);
        application3.setApplicationNumber(20003);
        application3.setContactPerson(person);
        final PermitDecision decision3 = model().newPermitDecision(application3);

        final HarvestPermitApplication application4 = model().newHarvestPermitApplication(rhy, area, MOOSELIKE);
        application4.setApplicationNumber(20004);
        application4.setContactPerson(person);
        final PermitDecision decision4 = model().newPermitDecision(application4);
        final String permitNumber4 = DocumentNumberUtil.createDocumentNumber(decision4.getDecisionYear(), 1, decision4.getDecisionNumber());
        final HarvestPermit permit4 = model().newHarvestPermit(rhy, permitNumber4);
        model().newHarvestPermitContactPerson(permit4, person);
        permit4.setPermitDecision(decision4);

        // this application won't be listed because not contact person
        final HarvestPermitApplication application5 = model().newHarvestPermitApplication(rhy, area, MOOSELIKE);
        application5.setApplicationNumber(20005);
        // this decision won't be listed because not contact person
        final PermitDecision decision5 = model().newPermitDecision(application5);
        final String permitNumber5 = DocumentNumberUtil.createDocumentNumber(decision5.getDecisionYear(), 1, decision5.getDecisionNumber());
        final HarvestPermit permit5 = model().newHarvestPermit(rhy, permitNumber5);
        model().newHarvestPermitContactPerson(permit5, person);
        permit5.setPermitDecision(decision5);

        final String permitNumber6 = DocumentNumberUtil.createDocumentNumber(DateUtil.currentYear(), 1, 30000);
        final HarvestPermit permit6 = model().newHarvestPermit(rhy, permitNumber6);
        permit6.setOriginalContactPerson(person);

        // this permit won't be listed because not contact person
        model().newHarvestPermit(rhy);

        // this application won't be listed, because not contact person
        model().newHarvestPermitApplication(rhy, area, MOOSELIKE);

        onSavedAndAuthenticated(createUser(person), () -> {
            final List<ApplicationDecisionPermitListDTO> res =
                    applicationDecisionPermitListFeature.listApplicationsAndDecisionsForPerson(person.getId());

            assertEquals(6, res.size());

            assertThat(res, contains(Arrays.asList(
                    dtoIsEqual(null, null, permit6),
                    dtoIsEqual(null, null, permit5),
                    dtoIsEqual(application4, decision4, permit4),
                    dtoIsEqual(application3, decision3, null),
                    dtoIsEqual(application2, null, null),
                    dtoIsEqual(application1, null, null))));
        });
    }

    @Test
    public void testMultiplePermits() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermitArea area = model().newHarvestPermitArea();
        final Person person = model().newPerson();

        final HarvestPermitApplication application = model().newHarvestPermitApplication(rhy, area, MOOSELIKE);
        application.setContactPerson(person);

        final PermitDecision decision = model().newPermitDecision(application);

        final HarvestPermit permit1 = model().newHarvestPermit(rhy);
        model().newHarvestPermitContactPerson(permit1, person);
        permit1.setPermitDecision(decision);

        final HarvestPermit permit2 = model().newHarvestPermit(rhy);
        model().newHarvestPermitContactPerson(permit2, person);
        permit2.setPermitDecision(decision);

        onSavedAndAuthenticated(createUser(person), () -> {
            final List<ApplicationDecisionPermitListDTO> res =
                    applicationDecisionPermitListFeature.listApplicationsAndDecisionsForPerson(person.getId());

            assertEquals(1, res.size());

            final ApplicationDecisionPermitListDTO dto = res.get(0);

            assertNotNull(dto.getApplication());
            assertNotNull(dto.getDecision());
            assertNotNull(dto.getPermits());

            assertEquals(application.getId(), dto.getApplication().getId());
            assertEquals(decision.getId(), dto.getDecision().getId());

            final List<ListHarvestPermitDTO> permitDTOs = dto.getPermits();

            assertThat(permitDTOs, hasSize(2));
            assertEquals(F.getUniqueIds(permit1, permit2), F.mapNonNullsToSet(permitDTOs, ListHarvestPermitDTO::getId));
        });
    }

    private static TypeSafeMatcher<ApplicationDecisionPermitListDTO> dtoIsEqual(final HarvestPermitApplication application,
                                                                                final PermitDecision decision,
                                                                                final HarvestPermit permit) {
        return new CustomTypeSafeMatcher<ApplicationDecisionPermitListDTO>("") {
            @Override
            protected boolean matchesSafely(final ApplicationDecisionPermitListDTO dto) {
                return Objects.equals(F.getId(dto.getApplication()), F.getId(application))
                        && Objects.equals(F.getId(dto.getDecision()), F.getId(decision))
                        && Objects.equals(F.getUniqueIds(dto.getPermits()), F.getUniqueIds(Collections.singleton(permit)));
            }
        };
    }
}
