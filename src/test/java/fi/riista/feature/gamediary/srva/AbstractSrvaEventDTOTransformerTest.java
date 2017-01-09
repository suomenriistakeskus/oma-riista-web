package fi.riista.feature.gamediary.srva;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.gamediary.srva.SrvaEventDTOBase;
import fi.riista.feature.gamediary.srva.method.SrvaMethodDTO;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenDTO;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.util.DateUtil;
import javaslang.Tuple;
import javaslang.Tuple2;

import org.junit.Before;
import java.util.List;

import static fi.riista.util.TestUtils.createList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractSrvaEventDTOTransformerTest extends EmbeddedDatabaseTest {

    protected Riistanhoitoyhdistys rhy;

    @Before
    public void initRhy() {
        this.rhy = model().newRiistanhoitoyhdistys();
    }

    protected static void assertSpecimens(final List<SrvaSpecimenDTO> specimenDTOs, final List<SrvaSpecimen> specimens) {
        for (int i = 0; i < specimenDTOs.size(); i++) {
            final SrvaSpecimen srvaSpecimen = specimens.get(i);
            final SrvaSpecimenDTO specimenDTO = specimenDTOs.get(i);
            assertEquals(srvaSpecimen.getAge(), specimenDTO.getAge());
            assertEquals(srvaSpecimen.getGender(), specimenDTO.getGender());
        }
    }

    protected static void assertMethods(final List<SrvaMethodDTO> methodDTOs, final List<SrvaMethod> methods) {
        for (int i = 0; i < methodDTOs.size(); i++) {
            final SrvaMethod srvaMethod = methods.get(i);
            final SrvaMethodDTO methodDTO = methodDTOs.get(i);
            assertEquals(srvaMethod.getName(), methodDTO.getName());
            assertEquals(srvaMethod.isChecked(), methodDTO.isChecked());
        }
    }

    protected static void assertCommonFields(final SrvaEvent srvaEvent, final SrvaEventDTOBase dto) {
        assertNotNull(srvaEvent.getId());
        assertNotNull(srvaEvent.getConsistencyVersion());
        assertNotNull(srvaEvent.getEventName());
        assertNotNull(srvaEvent.getEventType());
        assertNotNull(srvaEvent.getTotalSpecimenAmount());
        assertNotNull(srvaEvent.getPointOfTime());
        assertNotNull(srvaEvent.getGeoLocation());
        assertNotNull(srvaEvent.getSpecies());

        assertEquals(GameDiaryEntryType.SRVA, dto.getType());
        assertEquals(srvaEvent.getId(), dto.getId());
        assertEquals(srvaEvent.getConsistencyVersion(), dto.getRev());
        assertEquals(srvaEvent.getEventName(), dto.getEventName());
        assertEquals(srvaEvent.getEventType(), dto.getEventType());
        assertEquals(srvaEvent.getTotalSpecimenAmount(), dto.getTotalSpecimenAmount());
        assertEquals(Integer.valueOf(srvaEvent.getSpecies().getOfficialCode()), dto.getGameSpeciesCode());
        assertEquals(srvaEvent.getGeoLocation(), dto.getGeoLocation());
        assertEquals(DateUtil.toLocalDateTimeNullSafe(srvaEvent.getPointOfTime()), dto.getPointOfTime());
        assertEquals(srvaEvent.getDescription(), dto.getDescription());
        assertEquals(srvaEvent.getPersonCount(), dto.getPersonCount());
        assertEquals(srvaEvent.getTimeSpent(), dto.getTimeSpent());
        assertEquals(srvaEvent.getAuthor().getId(), dto.getAuthorInfo().getId());
        assertEquals(srvaEvent.getEventResult(), dto.getEventResult());
        assertEquals(srvaEvent.getOtherMethodDescription(), dto.getOtherMethodDescription());
        assertEquals(srvaEvent.getOtherTypeDescription(), dto.getOtherTypeDescription());
        assertEquals(srvaEvent.getState(), dto.getState());
    }

    protected SrvaEvent newSrvaEvent() {
        return model().newSrvaEvent(this.rhy);
    }

    protected SrvaEvent newSrvaEvent(final Person author) {
        return model().newSrvaEvent(author, this.rhy);
    }

    protected Tuple2<SrvaEvent, List<SrvaSpecimen>> newSrvaEventWithSpecimens(final int numSpecimens, final Person author) {
        final SrvaEvent srvaEvent = newSrvaEvent(author);

        // With one undefined specimen
        srvaEvent.setTotalSpecimenAmount(numSpecimens + 1);

        return Tuple.of(srvaEvent, createList(numSpecimens, () -> model().newSrvaSpecimen(srvaEvent)));
    }

    protected Tuple2<SrvaEvent, List<SrvaMethod>> newSrvaEventWithMethods(final int numMethods, final Person author) {
        final SrvaEvent srvaEvent = newSrvaEvent(author);

        return Tuple.of(srvaEvent, createList(numMethods, () -> model().newSrvaMethod(srvaEvent)));
    }

    protected Tuple2<SrvaEvent, List<GameDiaryImage>> newSrvaEventWithImages(final int numSpecimens, final Person author) {
        final SrvaEvent srvaEvent = newSrvaEvent(author);

        return Tuple.of(srvaEvent, createList(numSpecimens, () -> model().newGameDiaryImage(srvaEvent)));
    }
}
