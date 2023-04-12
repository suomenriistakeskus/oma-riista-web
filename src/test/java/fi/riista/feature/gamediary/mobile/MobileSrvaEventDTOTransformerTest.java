package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.mobile.srva.MobileSrvaEventDTO;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.mobile.srva.MobileSrvaEventDTOTransformer;
import fi.riista.feature.gamediary.srva.AbstractSrvaEventDTOTransformerTest;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.feature.gamediary.srva.SrvaEventResultDetailsEnum;
import fi.riista.feature.gamediary.srva.SrvaEventSpecVersion;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.gamediary.srva.SrvaEventTypeDetailsEnum;
import fi.riista.feature.gamediary.srva.SrvaEventTypeEnum;
import fi.riista.feature.gamediary.srva.SrvaResultEnum;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.test.rules.HibernateStatisticsAssertions;
import fi.riista.util.F;
import io.vavr.Tuple2;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.riista.feature.gamediary.image.GameDiaryImage.getUniqueImageIds;
import static fi.riista.test.TestUtils.createList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class MobileSrvaEventDTOTransformerTest extends AbstractSrvaEventDTOTransformerTest {

    @Resource
    private MobileSrvaEventDTOTransformer mobileSrvaEventDTOTransformer;

    @Test
    @HibernateStatisticsAssertions(maxQueries = 6)
    public void testWithSpecimens() {
        SrvaEventSpecVersion specVersion = SrvaEventSpecVersion._1;

        withPerson(person -> {
            final List<Tuple2<SrvaEvent, List<SrvaSpecimen>>> pairs =
                    createList(5, () -> newSrvaEventWithSpecimens(10, person));

            // Generate extra srva event that is not included in input and thus should not affect output either.
            newSrvaEventWithSpecimens(5, person);

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<MobileSrvaEventDTO> dtos = mobileSrvaEventDTOTransformer.apply(F.nonNullKeys(pairs), specVersion);

                assertNotNull(dtos);
                assertEquals(pairs.size(), dtos.size());

                for (int i = 0; i < pairs.size(); i++) {
                    final SrvaEvent srvaEvent = pairs.get(i)._1();
                    final MobileSrvaEventDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertTrue(dto.getImageIds().isEmpty());
                    assertCanEdit(dto);
                    assertCommonFields(srvaEvent, dto);
                    assertSpecimens(dto.getSpecimens(), pairs.get(i)._2());
                    assertEquals(specVersion, dto.getSrvaEventSpecVersion());
                }
            });
        });
    }

    @Test
    @HibernateStatisticsAssertions(maxQueries = 6)
    public void testWithMethods() {
        SrvaEventSpecVersion specVersion = SrvaEventSpecVersion._1;

        withPerson(person -> {
            final List<Tuple2<SrvaEvent, List<SrvaMethod>>> pairs =
                    createList(5, () -> newSrvaEventWithMethods(3, person));

            // Generate extra srva event that is not included in input and thus should not affect output either.
            newSrvaEventWithMethods(5, person);

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<MobileSrvaEventDTO> dtos = mobileSrvaEventDTOTransformer.apply(F.nonNullKeys(pairs), specVersion);

                assertNotNull(dtos);
                assertEquals(pairs.size(), dtos.size());

                for (int i = 0; i < pairs.size(); i++) {
                    final SrvaEvent srvaEvent = pairs.get(i)._1();
                    final MobileSrvaEventDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertTrue(dto.getImageIds().isEmpty());
                    assertCanEdit(dto);
                    assertCommonFields(srvaEvent, dto);
                    assertMethods(dto.getMethods(), pairs.get(i)._2());
                    assertEquals(specVersion, dto.getSrvaEventSpecVersion());
                }
            });
        });
    }

    @Test
    @HibernateStatisticsAssertions(maxQueries = 6)
    public void testWithImages() {
        SrvaEventSpecVersion specVersion = SrvaEventSpecVersion._1;

        withPerson(person -> {
            final List<Tuple2<SrvaEvent, List<GameDiaryImage>>> pairs =
                    createList(5, () -> newSrvaEventWithImages(5, person));

            // Generate extra srva event that are not included in input and thus should not affect output either.
            newSrvaEventWithImages(5, person);

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<MobileSrvaEventDTO> dtos = mobileSrvaEventDTOTransformer.apply(F.nonNullKeys(pairs), specVersion);

                assertNotNull(dtos);
                assertEquals(pairs.size(), dtos.size());

                for (int i = 0; i < pairs.size(); i++) {
                    final MobileSrvaEventDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertCommonFields(pairs.get(i)._1, dto);

                    assertThat(dto.getImageIds(), containsInAnyOrder(getUniqueImageIds(pairs.get(i)._2).toArray()));

                    assertCanEdit(dto);
                    assertEquals(specVersion, dto.getSrvaEventSpecVersion());
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
                final List<MobileSrvaEventDTO> dtos = mobileSrvaEventDTOTransformer.apply(events, SrvaEventSpecVersion._1);
                assertEquals(2, dtos.size());
                assertEquals(1, dtos.stream().filter(mobileSrvaEventDTO ->
                        mobileSrvaEventDTO.getGameSpeciesCode() == null).count());
            });
        });
    }

    @Test
    public void testSpecV2Fields() {
        withPerson(person -> {
            final SrvaEvent srvaEvent = newSrvaEvent();
            srvaEvent.setSpecies(null);
            srvaEvent.setOtherSpeciesDescription("otherspecies");
            srvaEvent.setEventName(SrvaEventNameEnum.DEPORTATION);
            srvaEvent.setEventType(SrvaEventTypeEnum.ANIMAL_NEAR_HOUSES_AREA);
            srvaEvent.setDeportationOrderNumber("123456");
            srvaEvent.setEventTypeDetail(SrvaEventTypeDetailsEnum.OTHER);
            srvaEvent.setOtherEventTypeDetailDescription("otherDetail");
            srvaEvent.setEventResult(SrvaResultEnum.ANIMAL_DEPORTED);
            srvaEvent.setEventResultDetail(SrvaEventResultDetailsEnum.ANIMAL_CONTACTED_AND_DEPORTED);

            final List<SrvaEvent> events = Arrays.asList(srvaEvent);

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<MobileSrvaEventDTO> dtos = mobileSrvaEventDTOTransformer.apply(events, SrvaEventSpecVersion._2);
                assertEquals(1, dtos.size());
                final MobileSrvaEventDTO dto = dtos.get(0);
                assertEquals(SrvaEventNameEnum.DEPORTATION, dto.getEventName());
                assertEquals(SrvaEventTypeEnum.ANIMAL_NEAR_HOUSES_AREA, dto.getEventType());
                assertEquals("123456", dto.getDeportationOrderNumber());
                assertEquals(SrvaEventTypeDetailsEnum.OTHER, dto.getEventTypeDetail());
                assertEquals("otherDetail", dto.getOtherEventTypeDetailDescription());
                assertEquals(SrvaResultEnum.ANIMAL_DEPORTED, dto.getEventResult());
                assertEquals(SrvaEventResultDetailsEnum.ANIMAL_CONTACTED_AND_DEPORTED, dto.getEventResultDetail());
            });
        });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testApplyList_noSpecVersion() {
        mobileSrvaEventDTOTransformer.apply(Collections.emptyList());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testApply_noSpecVersion() {
        mobileSrvaEventDTOTransformer.apply(new SrvaEvent());
    }

    @Test
    public void testApply_emptyList() {
        assertNotNull(mobileSrvaEventDTOTransformer.apply(Collections.emptyList(), SrvaEventSpecVersion._1));
    }

    private static void assertCanEdit(MobileSrvaEventDTO dto) {
        assertEquals(dto.getState() != SrvaEventStateEnum.APPROVED, dto.isCanEdit());
    }
}
