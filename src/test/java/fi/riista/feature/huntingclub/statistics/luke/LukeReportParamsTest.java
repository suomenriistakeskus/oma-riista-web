package fi.riista.feature.huntingclub.statistics.luke;

import com.google.common.collect.ObjectArrays;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Theories.class)
public class LukeReportParamsTest {

    private final String[][][] MOOSE_CLUB_VALUES = {
            {
                    {"CLUB", "FIGURE", "MOOSE_FIGURE"},
                    {"CLUB", "TABLE_FULL", "MOOSE_TABLE_FULL"},
                    {"CLUB", "TABLE_COMPARISON", "MOOSE_TABLE_COMPARISON"},
            },
    };

    private final String[][][] MOOSE_VALUES_WITHOUT_CLUB = {
            {
                    {"RHY", "FIGURE", "MOOSE_FIGURE"},
                    {"RHY", "MAP", "MOOSE_MAP"},
                    {"RHY", "TABLE_FULL", "MOOSE_TABLE_FULL"},
                    {"RHY", "TABLE_COMPARISON", "MOOSE_TABLE_COMPARISON"},
            },
            {
                    {"HTA", "FIGURE", "MOOSE_FIGURE"},
                    {"HTA", "MAP", "MOOSE_MAP"},
                    {"HTA", "TABLE_FULL", "MOOSE_TABLE_FULL"},
                    {"HTA", "TABLE_COMPARISON", "MOOSE_TABLE_COMPARISON"},
                    {"HTA", "FORECAST", "MOOSE_FORECAST"},
            },
            {
                    {"AREA", "FIGURE", "MOOSE_FIGURE"},
                    {"AREA", "MAP", "MOOSE_MAP"},
                    {"AREA", "TABLE_FULL", "MOOSE_TABLE_FULL"},
                    {"AREA", "TABLE_COMPARISON", "MOOSE_TABLE_COMPARISON"},
            },
            {
                    {"COUNTRY", "FIGURE", "MOOSE_FIGURE"},
                    {"COUNTRY", "MAP", "MOOSE_MAP"},
                    {"COUNTRY", "TABLE_FULL", "MOOSE_TABLE_FULL"},
                    {"COUNTRY", "TABLE_COMPARISON", "MOOSE_TABLE_COMPARISON"},
            }
    };

    private final String[][][] MOOSE_ALL_VALUES = ObjectArrays.concat(MOOSE_CLUB_VALUES, MOOSE_VALUES_WITHOUT_CLUB, String[][].class);

    private final String[][][] WTD_2019_VALUES = {
            {
                    {"RHY", "FIGURE", "WTD_PRE2020_FIGURE"},
                    {"RHY", "TABLE_COMPARISON", "WTD_PRE2020_TABLE_FULL"},
            },
            {
                    {"HTA", "FIGURE", "WTD_PRE2020_FIGURE"},
                    {"HTA", "TABLE_COMPARISON", "WTD_PRE2020_TABLE_FULL"},
            },
            {
                    {"AREA", "FIGURE", "WTD_PRE2020_FIGURE"},
                    {"AREA", "TABLE_COMPARISON", "WTD_PRE2020_TABLE_FULL"},
            },
            {
                    {"COUNTRY", "FIGURE", "WTD_PRE2020_FIGURE"},
                    {"COUNTRY", "TABLE_COMPARISON", "WTD_PRE2020_TABLE_FULL"},
            },
    };

    private final String[][][] WTD_2020_CLUB_VALUES = {
            {
                    {"CLUB", "FIGURE", "WTD_HARVEST_FIGURE", "WTD_OBSERVATION_FIGURE"},
                    {"CLUB", "TABLE_COMPARISON", "WTD_HARVEST_TABLE_FULL", "WTD_OBSERVATION_TABLE_FULL_2020"},
            },
    };

    private final String[][][] WTD_2020_VALUES_WITHOUT_CLUB = {
            {
                    {"RHY", "FIGURE", "WTD_HARVEST_FIGURE", "WTD_OBSERVATION_FIGURE"},
                    {"RHY", "MAP", "WTD_HARVEST_MAP", "WTD_OBSERVATION_MAP"},
                    {"RHY", "TABLE_COMPARISON", "WTD_HARVEST_TABLE_FULL", "WTD_OBSERVATION_TABLE_FULL_2020"},
            },
            {
                    {"HTA", "FIGURE", "WTD_HARVEST_FIGURE", "WTD_OBSERVATION_FIGURE"},
                    {"HTA", "MAP", "WTD_HARVEST_MAP", "WTD_OBSERVATION_MAP"},
                    {"HTA", "TABLE_COMPARISON", "WTD_HARVEST_TABLE_FULL", "WTD_OBSERVATION_TABLE_FULL_2020"},
            },
            {
                    {"AREA", "FIGURE", "WTD_HARVEST_FIGURE", "WTD_OBSERVATION_FIGURE"},
                    {"AREA", "MAP", "WTD_HARVEST_MAP", "WTD_OBSERVATION_MAP"},
                    {"AREA", "TABLE_COMPARISON", "WTD_HARVEST_TABLE_FULL", "WTD_OBSERVATION_TABLE_FULL_2020"},
            },
            {
                    {"COUNTRY", "FIGURE", "WTD_HARVEST_FIGURE", "WTD_OBSERVATION_FIGURE"},
                    {"COUNTRY", "MAP", "WTD_HARVEST_MAP", "WTD_OBSERVATION_MAP"},
                    {"COUNTRY", "TABLE_COMPARISON", "WTD_HARVEST_TABLE_FULL", "WTD_OBSERVATION_TABLE_FULL_2020"},
            }
    };

    private final String[][][] WTD_2020_ALL_VALUES = ObjectArrays.concat(WTD_2020_CLUB_VALUES, WTD_2020_VALUES_WITHOUT_CLUB, String[][].class);

    private final String[][][] WTD_2021_CLUB_VALUES = {
            {
                    {"CLUB", "FIGURE", "WTD_HARVEST_FIGURE", "WTD_HARVEST_ANTLER_FIGURE", "WTD_HARVEST_WEIGHT_FIGURE", "WTD_OBSERVATION_FIGURE"},
                    {"CLUB", "TABLE_COMPARISON", "WTD_HARVEST_TABLE_FULL", "WTD_OBSERVATION_TABLE_FULL"},
            },
    };

    private final String[][][] WTD_2021_VALUES_WITHOUT_CLUB = {
            {
                    {"RHY", "FIGURE", "WTD_HARVEST_FIGURE", "WTD_HARVEST_ANTLER_FIGURE", "WTD_HARVEST_WEIGHT_FIGURE", "WTD_OBSERVATION_FIGURE"},
                    {"RHY", "MAP", "WTD_HARVEST_MAP", "WTD_OBSERVATION_MAP"},
                    {"RHY", "TABLE_COMPARISON", "WTD_HARVEST_TABLE_FULL", "WTD_OBSERVATION_TABLE_FULL"},
            },
            {
                    {"HTA", "FIGURE", "WTD_HARVEST_FIGURE", "WTD_HARVEST_ANTLER_FIGURE", "WTD_HARVEST_WEIGHT_FIGURE", "WTD_OBSERVATION_FIGURE"},
                    {"HTA", "MAP", "WTD_HARVEST_MAP", "WTD_OBSERVATION_MAP"},
                    {"HTA", "TABLE_COMPARISON", "WTD_HARVEST_TABLE_FULL", "WTD_OBSERVATION_TABLE_FULL"},
            },
            {
                    {"AREA", "FIGURE", "WTD_HARVEST_FIGURE", "WTD_HARVEST_ANTLER_FIGURE", "WTD_HARVEST_WEIGHT_FIGURE", "WTD_OBSERVATION_FIGURE"},
                    {"AREA", "MAP", "WTD_HARVEST_MAP", "WTD_OBSERVATION_MAP"},
                    {"AREA", "TABLE_COMPARISON", "WTD_HARVEST_TABLE_FULL", "WTD_OBSERVATION_TABLE_FULL"},
            },
            {
                    {"COUNTRY", "FIGURE", "WTD_HARVEST_FIGURE", "WTD_HARVEST_ANTLER_FIGURE", "WTD_HARVEST_WEIGHT_FIGURE", "WTD_OBSERVATION_FIGURE"},
                    {"COUNTRY", "MAP", "WTD_HARVEST_MAP", "WTD_OBSERVATION_MAP"},
                    {"COUNTRY", "TABLE_COMPARISON", "WTD_HARVEST_TABLE_FULL", "WTD_OBSERVATION_TABLE_FULL"},
            }
    };

    private final String[][][] WTD_2021_ALL_VALUES = ObjectArrays.concat(WTD_2021_CLUB_VALUES, WTD_2021_VALUES_WITHOUT_CLUB, String[][].class);

    @DataPoints("huntingYears")
    public static final int[] HUNTING_YEARS = {2019, 2020, 2021};

    @Theory
    public void allValues_moose(@FromDataPoints("huntingYears") final int huntingYear, final boolean isModeratorOrCoordinator) {
        final LukeReportParamsDTO actual = new LukeReportParamsDTO(
            LukeReportParams.Organisation.allValues(OFFICIAL_CODE_MOOSE, huntingYear, isModeratorOrCoordinator), false);
        assertThatParamsMatch(actual, MOOSE_ALL_VALUES);
    }

    @Theory
    public void valuesWithoutClub_moose(@FromDataPoints("huntingYears") final int huntingYear, final boolean isModeratorOrCoordinator) {
        final LukeReportParamsDTO actual = new LukeReportParamsDTO(
                LukeReportParams.Organisation.valuesWithoutClub(OFFICIAL_CODE_MOOSE, huntingYear, isModeratorOrCoordinator), false);
        assertThatParamsMatch(actual, MOOSE_VALUES_WITHOUT_CLUB);
    }


    @Theory
    public void allValues_wtd(@FromDataPoints("huntingYears") final int huntingYear, final boolean isModeratorOrCoordinator) {
        final Map<Integer, String[][][]> expected = new HashMap<Integer, String[][][]>() {{
            put(2019, WTD_2019_VALUES);
            put(2020, WTD_2020_ALL_VALUES);
            put(2021, WTD_2021_ALL_VALUES);
        }};

        final LukeReportParamsDTO actual = new LukeReportParamsDTO(
                LukeReportParams.Organisation.allValues(OFFICIAL_CODE_WHITE_TAILED_DEER, huntingYear, isModeratorOrCoordinator), false);
        assertThatParamsMatch(actual, expected.get(huntingYear));
    }

    @Theory
    public void valuesWithoutClub_wtd(@FromDataPoints("huntingYears") final int huntingYear, final boolean isModeratorOrCoordinator) {
        final Map<Integer, String[][][]> expected = new HashMap<Integer, String[][][]>() {{
            put(2019, WTD_2019_VALUES);
            put(2020, WTD_2020_VALUES_WITHOUT_CLUB);
            put(2021, WTD_2021_VALUES_WITHOUT_CLUB);
        }};

        final LukeReportParamsDTO actual = new LukeReportParamsDTO(
                LukeReportParams.Organisation.valuesWithoutClub(OFFICIAL_CODE_WHITE_TAILED_DEER, huntingYear, isModeratorOrCoordinator), false);
        assertThatParamsMatch(actual, expected.get(huntingYear));
    }

    private void assertThatParamsMatch(final LukeReportParamsDTO actual, String[][][] expected) {
        assertThat(actual, is(notNullValue()), "Result is null");
        final List<Map<String, Object>> organisations = actual.getParams();
        assertThat(organisations, hasSize(expected.length), "Size mismatch");
        for (int i = 0; i < organisations.size(); i++) {
            final String orgName = (String)organisations.get(i).get("name");
            final List<Map<String, Object>> reportTypes = (List<Map<String, Object>>)organisations.get(i).get("reportTypes");
            assertThat(reportTypes, hasSize(expected[i].length), "reportType size mismatch with " + orgName);

            for (int j = 0; j < reportTypes.size(); j++) {
                final String groupName = (String)reportTypes.get(j).get("name");
                final List<Map<String, Object>> presentations = (List<Map<String, Object>>)reportTypes.get(j).get("presentations");
                assertThat(presentations, hasSize(expected[i][j].length - 2), "report size mismatch with " + orgName + ", " + groupName);

                for (int k = 0; k < presentations.size(); k++) {
                    final String presentationName = (String)presentations.get(k).get("name");
                    assertThat(orgName, equalTo(expected[i][j][0]), "orgName mismatch");
                    assertThat(groupName, equalTo(expected[i][j][1]), "groupName mismatch for " + orgName);
                    assertThat(presentationName, equalTo(expected[i][j][k+2]), "presentationName mismatch for " + orgName + ", " + groupName);
                }
            }
        }
    }
}