package fi.riista.feature.gamediary.search;

import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.Interval;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.EnumSet;

import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;

public class GameDiarySearchFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private GameDiarySearchFeature gameDiarySearchFeature;

    @Test
    public void testListDiaryEntriesForActiveUser() {
        final Interval interval = DateUtil.createDateInterval(today().minusDays(1), today().plusDays(1));
        final GameSpecies species = model().newGameSpecies();

        withPerson(me -> withPerson(other -> {
            final Harvest myHarvest = model().newHarvest(species, me, me);
            myHarvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            myHarvest.setHarvestReportDate(DateUtil.now());
            myHarvest.setHarvestReportAuthor(me);

            final Harvest todoHarvest = model().newHarvest(species, me, me);
            todoHarvest.setHarvestReportRequired(true);

            final Harvest myAuthoredHarvest = model().newHarvest(species, me, other);
            model().newHarvest(species, other, other);

            final Observation myObservation = model().newObservation(species, me, me);
            final Observation myAuthoredObservation = model().newObservation(species, me, other);
            model().newObservation(species, other, other);

            final SrvaEvent mySrvaEvent = model().newSrvaEvent(me);

            // Create entries not to be included in search results
            model().newHarvest();
            model().newObservation();
            model().newSrvaEvent();

            onSavedAndAuthenticated(createUser(me), () -> {
                assertEquals(
                        F.getUniqueIds(myHarvest, todoHarvest, myObservation, mySrvaEvent),
                        F.getUniqueIds(gameDiarySearchFeature.listDiaryEntriesForActiveUser(new GameDiarySearchDTO(
                                interval, EnumSet.allOf(GameDiaryEntryType.class), false, false, false))));

                assertEquals(
                        F.getUniqueIds(myAuthoredHarvest, myAuthoredObservation),
                        F.getUniqueIds(gameDiarySearchFeature.listDiaryEntriesForActiveUser(new GameDiarySearchDTO(
                                interval, EnumSet.allOf(GameDiaryEntryType.class), false, false, true))));

                assertEquals(
                        F.getUniqueIds(myHarvest),
                        F.getUniqueIds(gameDiarySearchFeature.listDiaryEntriesForActiveUser(new GameDiarySearchDTO(
                                interval, EnumSet.allOf(GameDiaryEntryType.class), true, false, false))));

                assertEquals(
                        F.getUniqueIds(todoHarvest),
                        F.getUniqueIds(gameDiarySearchFeature.listDiaryEntriesForActiveUser(new GameDiarySearchDTO(
                                interval, EnumSet.allOf(GameDiaryEntryType.class), false, true, false))));
            });
        }));
    }

}
