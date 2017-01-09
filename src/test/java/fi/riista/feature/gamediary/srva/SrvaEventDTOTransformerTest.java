package fi.riista.feature.gamediary.srva;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.util.F;
import fi.riista.util.Functions;
import fi.riista.util.jpa.HibernateStatisticsAssertions;
import javaslang.Tuple2;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static fi.riista.util.TestUtils.createList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SrvaEventDTOTransformerTest extends AbstractSrvaEventDTOTransformerTest {

    @Resource
    private SrvaEventDTOTransformer srvaEventDTOTransformer;

    @Test
    @HibernateStatisticsAssertions(maxQueries = 8)
    public void testWithSpecimens() {
        withPerson(person -> {

            final int numSpecimens = 10;
            final List<Tuple2<SrvaEvent, List<SrvaSpecimen>>> pairs =
                    createList(5, () -> newSrvaEventWithSpecimens(numSpecimens, person));

            // Generate extra srva event that is not included in input and thus should not affect output either.
            newSrvaEventWithSpecimens(5, person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final List<SrvaEventDTO> dtos = srvaEventDTOTransformer.apply(F.nonNullKeys(pairs));

                assertNotNull(dtos);
                assertEquals(pairs.size(), dtos.size());

                for (int i = 0; i < pairs.size(); i++) {
                    final SrvaEvent srvaEvent = pairs.get(i)._1();
                    final SrvaEventDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertTrue(dto.getImageIds().isEmpty());
                    assertCanEdit(dto);
                    assertCommonFields(srvaEvent, dto);

                    assertSpecimens(dto.getSpecimens(), pairs.get(i)._2());
                }
            });
        });
    }

    @Test
    @HibernateStatisticsAssertions(maxQueries = 8)
    public void testWithMethods() {
        withPerson(person -> {

            final int numMethods = 3;
            final List<Tuple2<SrvaEvent, List<SrvaMethod>>> pairs =
                    createList(5, () -> newSrvaEventWithMethods(numMethods, person));

            // Generate extra srva event that is not included in input and thus should not affect output either.
            newSrvaEventWithMethods(5, person);

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<SrvaEventDTO> dtos = srvaEventDTOTransformer.apply(F.nonNullKeys(pairs));

                assertNotNull(dtos);
                assertEquals(pairs.size(), dtos.size());

                for (int i = 0; i < pairs.size(); i++) {
                    final SrvaEvent srvaEvent = pairs.get(i)._1();
                    final SrvaEventDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertTrue(dto.getImageIds().isEmpty());
                    assertCanEdit(dto);
                    assertCommonFields(srvaEvent, dto);

                    assertMethods(dto.getMethods(), pairs.get(i)._2());
                }
            });
        });
    }

    @Test
    @HibernateStatisticsAssertions(maxQueries = 8)
    public void testWithImages() {
        withPerson(person -> {

            final List<Tuple2<SrvaEvent, List<GameDiaryImage>>> pairs =
                    createList(5, () -> newSrvaEventWithImages(5, person));

            // Generate extra srva event that are not included in input and thus should not affect output either.
            newSrvaEventWithImages(5, person);

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<SrvaEventDTO> dtos = srvaEventDTOTransformer.apply(F.nonNullKeys(pairs));

                assertNotNull(dtos);
                assertEquals(pairs.size(), dtos.size());

                for (int i = 0; i < pairs.size(); i++) {
                    final SrvaEventDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertCommonFields(pairs.get(i)._1, dto);

                    final Set<UUID> uuids =
                            F.mapNonNullsToSet(pairs.get(i)._2, Functions.idOf(GameDiaryImage::getFileMetadata));

                    assertEquals(uuids, new HashSet<>(dto.getImageIds()));

                    assertCanEdit(dto);
                }
            });
        });
    }

    @Test
    public void testOtherSpecies() {
        withPerson(person -> {
            final SrvaEvent srvaEvent = newSrvaEvent();
            srvaEvent.setSpecies(null);
            srvaEvent.setOtherSpeciesDescription("otherspecies");

            final List<SrvaEvent> events = Arrays.asList(newSrvaEvent(), srvaEvent);

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<SrvaEventDTO> dtos = srvaEventDTOTransformer.apply(events);
                assertEquals(2, dtos.size());
                assertEquals(1, dtos.stream().filter(srvaEventDTO ->
                        srvaEventDTO.getGameSpeciesCode() == null).count());
            });
        });
    }

    private static void assertCanEdit(SrvaEventDTO dto) {
        assertEquals(dto.getState() != SrvaEventStateEnum.APPROVED, dto.isCanEdit());
    }

    @Test
    public void testCanEdit_author() {
        withPerson(author -> {
            final SystemUser user = createUser(author);
            testCanEdit(author, true, SrvaEventStateEnum.UNFINISHED, user);
            testCanEdit(author, true, SrvaEventStateEnum.REJECTED, user);
            testCanEdit(author, false, SrvaEventStateEnum.APPROVED, user);
        });
    }


    @Test
    public void testCanEdit_srvaContact() {
        withPerson(srvaContact -> withPerson(author -> {
            model().newOccupation(rhy, srvaContact, OccupationType.SRVA_YHTEYSHENKILO);
            final SystemUser user = createUser(srvaContact);
            testCanEdit(author, true, SrvaEventStateEnum.UNFINISHED, user);
            testCanEdit(author, true, SrvaEventStateEnum.REJECTED, user);
            testCanEdit(author, false, SrvaEventStateEnum.APPROVED, user);
        }));
    }

    @Test
    public void testCanEdit_moderator() {
        withPerson(author -> {
            final SystemUser user = createNewModerator();
            testCanEdit(author, true, SrvaEventStateEnum.UNFINISHED, user);
            testCanEdit(author, true, SrvaEventStateEnum.REJECTED, user);
            testCanEdit(author, false, SrvaEventStateEnum.APPROVED, user);
        });

    }

    @Test
    public void testCanEdit_otherPerson() {
        withPerson(other -> withPerson(author -> {
            final SystemUser user = createUser(other);
            testCanEdit(author, false, SrvaEventStateEnum.UNFINISHED, user);
            testCanEdit(author, false, SrvaEventStateEnum.REJECTED, user);
            testCanEdit(author, false, SrvaEventStateEnum.APPROVED, user);
        }));
    }

    private void testCanEdit(Person author, boolean canEdit, SrvaEventStateEnum state, SystemUser callingUser) {
        SystemUser approver = state != SrvaEventStateEnum.UNFINISHED ? createNewModerator() : null;
        final SrvaEvent srvaEvent = newSrvaEvent(author);
        srvaEvent.setState(state);
        srvaEvent.setApproverAsUser(approver);

        onSavedAndAuthenticated(callingUser, () -> {
            final List<SrvaEventDTO> dtos = srvaEventDTOTransformer.apply(Arrays.asList(srvaEvent));
            assertEquals(1, dtos.size());
            assertEquals(canEdit, dtos.get(0).isCanEdit());
        });
    }

}
