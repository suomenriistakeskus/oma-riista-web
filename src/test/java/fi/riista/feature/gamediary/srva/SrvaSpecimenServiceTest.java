package fi.riista.feature.gamediary.srva;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenService;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.srva.SrvaEventDTO;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenDTO;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenRepository;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.test.TestUtils.createList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SrvaSpecimenServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private SrvaSpecimenService srvaSpecimenService;

    @Resource
    private SrvaSpecimenRepository srvaSpecimenRepository;

    @Test
    @Transactional
    public void testSaveSpecimens() {
        final SrvaEvent srvaEventEntity = model().newSrvaEvent();
        persistInCurrentlyOpenTransaction();

        final SrvaEventDTO dtoWithSpecimens = createDtoWithSpecimens(nextPositiveIntAtMost(100));

        srvaSpecimenService.saveSpecimens(srvaEventEntity, dtoWithSpecimens);

        assertEqualSpecimenFields(dtoWithSpecimens.getSpecimens(), findSpecimens(srvaEventEntity));
    }

    @Test
    @Transactional
    public void testUpdateSpecimens_numberOfSpecimenIncreased() {
        final SrvaEvent srvaEventEntity = model().newSrvaEvent();
        persistInCurrentlyOpenTransaction();

        srvaSpecimenService.saveSpecimens(srvaEventEntity, createDtoWithSpecimens(10));

        assertTrue(srvaSpecimenService.updateSpecimens(srvaEventEntity, createDtoWithSpecimens(11)));
    }

    @Test
    @Transactional
    public void testUpdateSpecimens_numberOfSpecimenDecreased() {
        final SrvaEvent srvaEventEntity = model().newSrvaEvent();
        persistInCurrentlyOpenTransaction();

        srvaSpecimenService.saveSpecimens(srvaEventEntity, createDtoWithSpecimens(10));

        assertTrue(srvaSpecimenService.updateSpecimens(srvaEventEntity, createDtoWithSpecimens(9)));
    }

    @Test
    @Transactional
    public void testUpdateSpecimens_noChanges() {
        final SrvaEvent srvaEventEntity = model().newSrvaEvent();
        persistInCurrentlyOpenTransaction();

        final SrvaEventDTO dtoWithSpecimens = createDtoWithSpecimens(2);
        srvaSpecimenService.saveSpecimens(srvaEventEntity, dtoWithSpecimens);

        assertFalse(srvaSpecimenService.updateSpecimens(srvaEventEntity, dtoWithSpecimens));
    }

    @Test
    @Transactional
    public void testUpdateSpecimens_businessFieldsUpdated() {
        final SrvaEvent srvaEventEntity = model().newSrvaEvent();
        persistInCurrentlyOpenTransaction();

        final SrvaEventDTO dtoWithSpecimens = createDtoWithSpecimens(3);
        srvaSpecimenService.saveSpecimens(srvaEventEntity, dtoWithSpecimens);

        assertFalse(srvaSpecimenService.updateSpecimens(srvaEventEntity, dtoWithSpecimens));

        final SrvaSpecimenDTO specimenDTO = dtoWithSpecimens.getSpecimens().get(0);
        specimenDTO.setAge(someOtherThan(specimenDTO.getAge(), GameAge.class));

        assertTrue(srvaSpecimenService.updateSpecimens(srvaEventEntity, dtoWithSpecimens));

        assertEqualSpecimenFields(dtoWithSpecimens.getSpecimens(), findSpecimens(srvaEventEntity));
    }

    private SrvaEventDTO createDtoWithSpecimens(final int numberOfSpecimens) {
        final SrvaEventDTO dto = new SrvaEventDTO();

        dto.setSpecimens(createList(numberOfSpecimens, () -> {
            final SrvaSpecimenDTO specimenDTO = new SrvaSpecimenDTO();
            specimenDTO.setAge(some(GameAge.class));
            specimenDTO.setGender(some(GameGender.class));
            return specimenDTO;
        }));
        dto.setTotalSpecimenAmount(numberOfSpecimens);

        return dto;
    }

    private List<SrvaSpecimen> findSpecimens(final SrvaEvent srvaEvent) {
        return srvaSpecimenRepository.findByEventOrderById(srvaEvent);
    }

    private static void assertEqualSpecimenFields(
            final List<SrvaSpecimenDTO> specimenDTOs, final List<SrvaSpecimen> savedEntities) {

        assertEquals(specimenDTOs.size(), savedEntities.size());

        for (int i = 0; i < savedEntities.size(); i++) {
            assertEquals(savedEntities.get(i).getAge(), specimenDTOs.get(i).getAge());
            assertEquals(savedEntities.get(i).getGender(), specimenDTOs.get(i).getGender());
        }
    }

    @Test(expected = NullPointerException.class)
    @Transactional
    public void testSaveSpecimens_nullEntity() {

        srvaSpecimenService.saveSpecimens(null, new SrvaEventDTO());
    }

    @Test(expected = NullPointerException.class)
    @Transactional
    public void testSaveSpecimens_nullDTO() {
        final SrvaEvent srvaEventEntity = model().newSrvaEvent();
        persistInCurrentlyOpenTransaction();

        srvaSpecimenService.saveSpecimens(srvaEventEntity, null);
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void testSaveSpecimens_transientEntity() {
        srvaSpecimenService.saveSpecimens(new SrvaEvent(), new SrvaEventDTO());
    }

    @Test(expected = NullPointerException.class)
    @Transactional
    public void testUpdateSpecimens_nullEntity() {
        srvaSpecimenService.updateSpecimens(null, new SrvaEventDTO());
    }

    @Test(expected = NullPointerException.class)
    @Transactional
    public void testUpdateSpecimens_nullDTO() {
        final SrvaEvent srvaEventEntity = model().newSrvaEvent();
        persistInCurrentlyOpenTransaction();

        srvaSpecimenService.updateSpecimens(srvaEventEntity, null);
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void testUpdateSpecimens_transientEntity() {
        srvaSpecimenService.updateSpecimens(new SrvaEvent(), new SrvaEventDTO());
    }
}
