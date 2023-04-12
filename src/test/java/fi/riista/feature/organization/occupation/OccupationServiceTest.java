package fi.riista.feature.organization.occupation;

import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import java.util.Collections;

import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Theories.class)
public class OccupationServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private OccupationService occupationService;

    @Resource
    private OccupationRepository occupationRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testUpdateContactInfoVisibility() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person person = model().newPerson();
            final Occupation occupation = model().newOccupation(rhy, person, METSASTYKSENVALVOJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                final OccupationContactInfoVisibilityDTO visibility =
                        new OccupationContactInfoVisibilityDTO(
                                occupation.getId(),
                                occupation.isNameVisibility(),
                                occupation.isPhoneNumberVisibility(),
                                false);
                occupationService.updateContactInfoVisibility(Collections.singletonList(visibility));

                runInTransaction(() -> {
                    final Occupation updated = occupationRepository.getOne(occupation.getId());
                    assertThat(updated, is(notNullValue()));

                    assertThat(updated.isNameVisibility(), is(equalTo(occupation.isNameVisibility())));
                    assertThat(updated.isPhoneNumberVisibility(), is(equalTo(occupation.isPhoneNumberVisibility())));
                    assertThat(updated.isEmailVisibility(), is(equalTo(false)));
                });
            });
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateContactInfoVisibility_illegalSetting() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person person = model().newPerson();
            final Occupation occupation = model().newOccupation(rhy, person, METSASTYKSENVALVOJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                final OccupationContactInfoVisibilityDTO visibility =
                        new OccupationContactInfoVisibilityDTO(
                                occupation.getId(),
                                false,
                                occupation.isPhoneNumberVisibility(),
                                false);
                occupationService.updateContactInfoVisibility(Collections.singletonList(visibility));
            });
        });
    }

}
