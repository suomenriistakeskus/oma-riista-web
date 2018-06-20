package fi.riista.feature.gamediary;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static fi.riista.util.EqualityHelper.equalIdAndContent;
import static fi.riista.util.EqualityHelper.equalNotNull;
import static fi.riista.util.Filters.hasAnyIdOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class GameDiaryEntryFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    protected PersonRepository personRepo;

    protected static <T extends GameDiaryEntry> Consumer<T> assertAcceptedToHuntingDay(
            final Person acceptor, final GroupHuntingDay huntingDay) {

        return e -> {
            assertEquals(huntingDay.getId(), e.getHuntingDayOfGroup().getId());
            assertEquals(acceptor, e.getApproverToHuntingDay());
            assertNotNull(e.getPointOfTimeApprovedToHuntingDay());
        };
    }

    protected static void assertCommonExpectations(final GameDiaryEntry entry,
                                                   final HuntingDiaryEntryDTO expectedValues) {
        assertNotNull(entry);

        assertEquals(GeoLocation.Source.MANUAL, entry.getGeoLocation().getSource());
        assertNull(entry.getMobileClientRefId());

        assertEquals(expectedValues.getGameSpeciesCode(), entry.getSpecies().getOfficialCode());
        assertEquals(DateUtil.toDateNullSafe(expectedValues.getPointOfTime()), entry.getPointOfTime());
        assertEquals(expectedValues.getGeoLocation(), entry.getGeoLocation());
        assertEquals(expectedValues.getDescription(), entry.getDescription());
    }

    protected void validateAuthorAndActor(final GameDiaryEntry diaryEntry,
                                          final Long expectedAuthorId,
                                          final Long expectedActorId) {

        final Person author = personRepo.getOne(expectedAuthorId);
        final Person actor = expectedActorId != null ? personRepo.getOne(expectedActorId) : author;

        assertEquals("wrong author", author, diaryEntry.getAuthor());
        assertEquals("wrong actor", actor, diaryEntry.getActor());
    }

    protected static <ENTITY extends HasID<Long>, DTO extends HasID<Long>> void assertSpecimens(
            final List<ENTITY> specimens,
            final List<DTO> expectedSpecimens,
            final BiFunction<ENTITY, DTO, Boolean> compareFn) {

        assertEquals(expectedSpecimens.size(), specimens.size());

        final Map<Boolean, List<DTO>> dtoPartitionByIdExistence = F.partition(expectedSpecimens, F::hasId);
        final List<DTO> expectedUpdatedSpecimens = dtoPartitionByIdExistence.get(true);
        final Map<Boolean, List<ENTITY>> entityPartition = F.partition(specimens, hasAnyIdOf(expectedUpdatedSpecimens));

        assertTrue(equalIdAndContent(entityPartition.get(true), expectedUpdatedSpecimens, compareFn));
        assertTrue(equalNotNull(entityPartition.get(false), dtoPartitionByIdExistence.get(false), compareFn));
    }

}
