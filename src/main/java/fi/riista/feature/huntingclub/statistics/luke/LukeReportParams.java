package fi.riista.feature.huntingclub.statistics.luke;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LukeReportParams {
    public enum Presentation {
        FIGURE("figure", ".png", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"),
        MAP("map", ".png",
                "h1", "h2", "h3", "h4", "h5",
                "s1", "s2", "s3", "s4",
                "r1", "r2", "r3", "r4", "r5", "r6"),
        TABLE_FULL("table", ".html", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"),
        TABLE_COMPARISON("table", ".html", "1", "2", "3", "4", "5", "6", "7", "8");

        private final String lukeValue;
        private final String prefix;
        private final List<String> fileNames;

        Presentation(final String lukeValue, final String prefix, final String... fileNames) {
            this.lukeValue = lukeValue;
            this.prefix = prefix;
            this.fileNames = Arrays.asList(fileNames);
        }

        public String getLukeValue() {
            return lukeValue;
        }

        public String composeFileName(final String file) {
            Preconditions.checkArgument(fileNames.contains(file), "value %s not in %s", file, fileNames);
            return file + prefix;
        }

        @JsonValue
        public Map<String, Object> toMapValue() {
            return ImmutableMap.of("name", name(), "files", fileNames);
        }

        public List<String> getFileNames() {
            return fileNames;
        }
    }

    public enum Organisation {
        CLUB("rse", Presentation.FIGURE, Presentation.TABLE_FULL, Presentation.TABLE_COMPARISON),
        RHY("rhy", Presentation.FIGURE, Presentation.MAP, Presentation.TABLE_FULL, Presentation.TABLE_COMPARISON),
        HTA("uta", Presentation.FIGURE, Presentation.MAP, Presentation.TABLE_FULL, Presentation.TABLE_COMPARISON),
        AREA("rhp", Presentation.FIGURE, Presentation.MAP, Presentation.TABLE_FULL, Presentation.TABLE_COMPARISON),
        COUNTRY("maa", Presentation.FIGURE, Presentation.MAP, Presentation.TABLE_FULL, Presentation.TABLE_COMPARISON);

        private final String lukeValue;
        private final List<Presentation> presentations;

        Organisation(final String k, final Presentation... presentations) {
            this.lukeValue = k;
            this.presentations = Arrays.asList(presentations);
        }

        @JsonValue
        public Map<String, Object> toMapValue() {
            return ImmutableMap.of("name", name(), "presentations", presentations);
        }

        public static Organisation[] valuesWithoutClub() {
            return new Organisation[]{RHY, HTA, AREA, COUNTRY};
        }

        public String getLukeValue() {
            return this.lukeValue;
        }

        public List<Presentation> getPresentations() {
            return presentations;
        }
    }

    public static String getCheckExistsPath(final HuntingClub club,
                                            final HarvestPermit permit) {
        if (club == null || permit == null || permit.getMooseArea() == null) {
            return null;
        }

        return getReportPath(
                club,
                permit,
                permit.getMooseArea().getNumber(),
                Organisation.CLUB,
                Presentation.FIGURE,
                Presentation.FIGURE.getFileNames().get(0));
    }

    public static String getReportPath(final HuntingClub club,
                                       final HarvestPermit permit,
                                       final String htaNumber,
                                       final Organisation org,
                                       final Presentation presentation,
                                       final String fileName) {
        final int huntingYear = findHuntingYear(permit);

        return Joiner.on('/').join(
                huntingYear,
                org.getLukeValue(),
                // we want to strip leading zeros, but '000' should map to '0'
                Long.parseLong(getOrganisationId(club, permit, htaNumber, org)),
                presentation.getLukeValue(),
                presentation.composeFileName(fileName));
    }

    @Nonnull
    private static String getOrganisationId(final HuntingClub club,
                                            final HarvestPermit permit,
                                            final String htaNumber,
                                            final Organisation org) {
        switch (org) {
            case CLUB:
                return club.getOfficialCode();
            case RHY:
                return permit.getRhy().getOfficialCode();
            case HTA:
                return htaNumber;
            case AREA:
                return permit.getRhy().getParentOrganisation().getOfficialCode();
            case COUNTRY:
                return "850";
            default:
                return "";
        }
    }

    private static int findHuntingYear(final HarvestPermit permit) {
        final int[] years =
                Has2BeginEndDates.streamUniqueHuntingYearsSorted(permit.getSpeciesAmounts().stream()).toArray();

        if (years.length != 1) {
            throw new IllegalStateException(String.format(
                    "Cannot resolve unique hunting year for permit id:%d permitNumber:%s",
                    permit.getId(), permit.getPermitNumber()));
        }

        return years[0];
    }

    private LukeReportParams() {
        throw new AssertionError();
    }
}
