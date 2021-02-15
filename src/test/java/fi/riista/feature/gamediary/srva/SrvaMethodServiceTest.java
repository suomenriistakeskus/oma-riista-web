package fi.riista.feature.gamediary.srva;

import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.method.SrvaMethodDTO;
import fi.riista.feature.gamediary.srva.method.SrvaMethodEnum;
import fi.riista.feature.gamediary.srva.method.SrvaMethodRepository;
import fi.riista.feature.gamediary.srva.method.SrvaMethodService;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.test.TestUtils.createList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SrvaMethodServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private SrvaMethodService srvaMethodService;

    @Resource
    private SrvaMethodRepository srvaMethodRepository;

    @Test
    @Transactional
    public void testSaveMethods() {
        final SrvaEvent srvaEventEntity = model().newSrvaEvent();
        persistInCurrentlyOpenTransaction();

        final SrvaEventDTO dtoWithMethods = createDtoWithMethods(nextPositiveIntAtMost(5));

        srvaMethodService.saveMethods(srvaEventEntity, dtoWithMethods);

        final List<SrvaMethod> methodEntities = findMethods(srvaEventEntity);

        assertEqualMethodFields(dtoWithMethods.getMethods(), methodEntities);
    }

    @Test
    @Transactional
    public void testUpdateMethods() {
        final SrvaEvent srvaEventEntity = model().newSrvaEvent();
        persistInCurrentlyOpenTransaction();

        srvaMethodService.saveMethods(srvaEventEntity, createDtoWithMethods(nextPositiveIntAtMost(5)));

        final SrvaEventDTO dtoWithMethods = createDtoWithMethods(nextPositiveIntAtMost(5));

        // Number of methods updated
        assertTrue(srvaMethodService.updateMethods(srvaEventEntity, dtoWithMethods));

        final SrvaMethodDTO methodDTO = dtoWithMethods.getMethods().get(0);
        methodDTO.setName(someOtherThan(methodDTO.getName(), SrvaMethodEnum.class));

        // method business field updated
        assertTrue(srvaMethodService.updateMethods(srvaEventEntity, dtoWithMethods));

        final List<SrvaMethod> methodEntities = findMethods(srvaEventEntity);
        assertEqualMethodFields(dtoWithMethods.getMethods(), methodEntities);

        // if Methods are not changed this should return false
        assertFalse(srvaMethodService.updateMethods(srvaEventEntity, dtoWithMethods));
    }

    private SrvaEventDTO createDtoWithMethods(final int numberOfMethods) {
        final SrvaEventDTO dto = new SrvaEventDTO();
        dto.setMethods(createList(numberOfMethods, () -> new SrvaMethodDTO(some(SrvaMethodEnum.class), true)));
        return dto;
    }

    private List<SrvaMethod> findMethods(final SrvaEvent srvaEvent) {
        return srvaMethodRepository.findByEvent(srvaEvent);
    }

    private static void assertEqualMethodFields(
            final List<SrvaMethodDTO> methodDTOs, final List<SrvaMethod> savedEntities) {

        assertEquals(methodDTOs.size(), savedEntities.size());

        for (int i = 0; i < savedEntities.size(); i++) {
            assertEquals(savedEntities.get(i).getName(), methodDTOs.get(i).getName());
            assertEquals(savedEntities.get(i).isChecked(), methodDTOs.get(i).isChecked());
        }
    }

    @Test(expected = NullPointerException.class)
    @Transactional
    public void testSaveMethods_nullEntity() {
        srvaMethodService.saveMethods(null, new SrvaEventDTO());
    }

    @Test(expected = NullPointerException.class)
    @Transactional
    public void testSaveMethods_nullDTO() {
        final SrvaEvent srvaEventEntity = model().newSrvaEvent();
        persistInCurrentlyOpenTransaction();

        srvaMethodService.saveMethods(srvaEventEntity, null);
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void testSaveMethods_transientEntity() {
        srvaMethodService.saveMethods(new SrvaEvent(), new SrvaEventDTO());
    }

    @Test(expected = NullPointerException.class)
    @Transactional
    public void testUpdateMethods_nullEntity() {
        srvaMethodService.updateMethods(null, new SrvaEventDTO());
    }

    @Test(expected = NullPointerException.class)
    @Transactional
    public void testUpdateMethods_nullDTO() {
        final SrvaEvent srvaEventEntity = model().newSrvaEvent();
        persistInCurrentlyOpenTransaction();

        srvaMethodService.updateMethods(srvaEventEntity, null);
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void testUpdateMethods_transientEntity() {
        srvaMethodService.updateMethods(new SrvaEvent(), new SrvaEventDTO());
    }
}
