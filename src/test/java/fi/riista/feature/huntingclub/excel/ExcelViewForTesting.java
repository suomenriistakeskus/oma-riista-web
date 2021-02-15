package fi.riista.feature.huntingclub.excel;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayDTO;
import fi.riista.feature.huntingclub.hunting.excel.ClubHuntingDataExcelDTO;
import fi.riista.feature.huntingclub.hunting.excel.ClubHuntingDataExcelView;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.util.LocalisedString;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelViewForTesting extends ClubHuntingDataExcelView {

    public ExcelViewForTesting(final EnumLocaliser localiser, boolean includeDeerPilotFields) {
        super(localiser,
              mooselikeSpeciesIndex(),
              LocalisedString.of("Seura", "Klubben"),
              excelData(),
              includeDeerPilotFields);
    }

    public void build(final Workbook workbook) {
        final HttpServletResponse response = new MockHttpServletResponse();
        buildExcelDocument(null, workbook, null, response);
    }

    private static Map<Integer, LocalisedString> mooselikeSpeciesIndex() {
        final Map<Integer, LocalisedString> speciesIndex = new HashMap<>();
        speciesIndex.put(GameSpecies.OFFICIAL_CODE_FALLOW_DEER, LocalisedString.of("kuusipeura", "dovhjort"));
        speciesIndex.put(GameSpecies.OFFICIAL_CODE_MOOSE, LocalisedString.of("hirvi", "älg"));
        speciesIndex.put(GameSpecies.OFFICIAL_CODE_ROE_DEER, LocalisedString.of("metsäkauris", "rådjur"));
        speciesIndex.put(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER,LocalisedString.of("valkohäntäpeura", "vitsvanshjort"));
        speciesIndex.put(GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER, LocalisedString.of("metsäpeura", "skogsren"));
        return speciesIndex;
    }

    private static List<ClubHuntingDataExcelDTO> excelData() {
        final LocalisedString groupName = LocalisedString.of("Group1", "Group1");

        final List<GroupHuntingDayDTO> days =  new ArrayList<>();
        final GroupHuntingDayDTO day = new GroupHuntingDayDTO();
        day.setStartDate(new LocalDate(2020, 1, 2));
        day.setStartTime(new LocalTime(3, 4, 5));
        day.setEndDate(new LocalDate(2020, 1, 2));
        day.setEndTime(new LocalTime(7, 8, 9));
        days.add(day);

        final List<HarvestDTO> harvests = new ArrayList<>();
        final HarvestDTO harvest = new HarvestDTO();
        harvest.setPointOfTime(new LocalDateTime(2020, 1, 2, 10, 11, 12));
        harvest.setGeoLocation(new GeoLocation(7464001, 550001));
        harvest.setAuthorInfo(PersonWithHunterNumberDTO.create(new Person()));
        harvest.setActorInfo(PersonWithHunterNumberDTO.create(new Person()));
        harvest.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);
        harvest.setDeerHuntingType(DeerHuntingType.DOG_HUNTING);
        final List<HarvestSpecimenDTO> specimens = new ArrayList<>();
        specimens.add(new HarvestSpecimenDTO(GameGender.FEMALE, GameAge.ADULT, 10.0));
        harvest.setSpecimens(specimens);
        harvests.add(harvest);

        final List<ObservationDTO> observations = new ArrayList<>();
        final ObservationDTO observation = new ObservationDTO();
        observation.setPointOfTime(new LocalDateTime(2020, 1, 2, 13, 14, 15));
        observation.setGeoLocation(new GeoLocation(7464002, 550002));
        observation.setAuthorInfo(PersonWithHunterNumberDTO.create(new Person()));
        observation.setActorInfo(PersonWithHunterNumberDTO.create(new Person()));
        observation.setObservationType(ObservationType.NAKO);
        observation.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);
        observation.setDeerHuntingType(DeerHuntingType.OTHER);
        observation.setDeerHuntingTypeDescription("Other hunting type");
        observation.setAmount(1);
        observation.setMooselikeMaleAmount(1);
        observations.add(observation);

        final List<ClubHuntingDataExcelDTO> data = new ArrayList<>();
        data.add(new ClubHuntingDataExcelDTO(groupName, days, harvests, observations));
        return data;
    }

}
