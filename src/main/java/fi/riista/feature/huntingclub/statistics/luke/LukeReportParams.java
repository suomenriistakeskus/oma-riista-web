package fi.riista.feature.huntingclub.statistics.luke;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Component
public class LukeReportParams {

    private static final Logger LOG = LoggerFactory.getLogger(LukeReportParams.class);

    @Value("${luke.moose.reports.url.prefix}")
    private String urlPrefix;

    private final LoadingCache<String, Boolean> urlExistsCache;

    public LukeReportParams() {
        this.urlExistsCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<String, Boolean>() {
                    @Override
                    public Boolean load(String key) {
                        return loadUrl(key);
                    }
                });
    }

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

    public String composeUrl(final HuntingClub club,
                             final HarvestPermit permit,
                             final String htaNumber,
                             final Organisation org,
                             final Presentation presentation,
                             final String fileName) {

        final int huntingYear = findHuntingYear(permit);
        String orgId = null;

        switch (org) {
            case CLUB:
                orgId = club.getOfficialCode();
                break;
            case RHY:
                orgId = permit.getRhy().getOfficialCode();
                break;
            case HTA:
                orgId = htaNumber;
                break;
            case AREA:
                orgId = permit.getRhy().getParentOrganisation().getOfficialCode();
                break;
            case COUNTRY:
                orgId = "850";
                break;
        }
        final long orgIdNumber = Long.parseLong(orgId);// we want to strip leading zeros, but '000' should map to '0'
        return Joiner.on("/").join(urlPrefix, huntingYear, org.getLukeValue(), orgIdNumber,
                presentation.getLukeValue(), presentation.composeFileName(fileName));
    }

    private static int findHuntingYear(HarvestPermit permit) {
        List<Integer> years = Has2BeginEndDates.collectUniqueHuntingYearsSorted(permit.getSpeciesAmounts().stream());
        if (years.size() != 1) {
            throw new IllegalStateException("Can not resolve unique hunting year for permit id:" + permit.getId() + " permitNumber:" + permit.getPermitNumber());
        }
        return years.get(0);
    }

    public Map<String, Map<String, Boolean>> checkReportsAvailability(HuntingClub club, HarvestPermit permit) {
        return Stream.of(Organisation.values())
                .collect(toMap(Enum::name, o -> checkAvailabilities(o, club, permit)));
    }

    private Map<String, Boolean> checkAvailabilities(Organisation o, HuntingClub club, HarvestPermit permit) {
        if (o == Organisation.CLUB && club == null) {
            return Collections.emptyMap();
        }
        return o.getPresentations().stream()
                .collect(toMap(Enum::name, p -> {
                    final String htaNumber = permit.getMooseArea().getNumber();
                    final String url = composeUrl(club, permit, htaNumber, o, p, p.getFileNames().get(0));
                    try {
                        return urlExistsCache.get(url);
                    } catch (ExecutionException e) {
                        return false;
                    }
                }));
    }

    private static boolean loadUrl(String url) {
        try {
            return doTestUrlExists(url);
        } catch (Exception e) {
            LOG.warn("Unexpected exception", e);
            return false;
        }
    }

    private static boolean doTestUrlExists(String url) {
        try {
            final URLConnection c = new URL(url).openConnection();

            if (c instanceof HttpURLConnection) {
                final HttpURLConnection connection = (HttpURLConnection) c;
                connection.setRequestMethod("HEAD");
                final int code = connection.getResponseCode();
                return code == HttpStatus.OK.value();
            }

            LOG.error("error - not a http request!");
        } catch (final IOException e) {
            LOG.error("Exception, message: " + e.getMessage());
        }
        return false;
    }
}
