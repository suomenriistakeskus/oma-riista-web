package fi.riista.feature.huntingclub.statistics.luke;

import com.google.common.base.Joiner;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.util.F;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Objects;

public class LukeReportUriBuilder {
    private static int findHuntingYear(final HarvestPermit permit) {
        final int[] years = Has2BeginEndDates.streamUniqueHuntingYearsSorted(
                permit.getSpeciesAmounts().stream()).toArray();

        if (years.length != 1) {
            throw new IllegalStateException(String.format(
                    "Cannot resolve unique hunting year for permit id:%d permitNumber:%s",
                    permit.getId(), permit.getPermitNumber()));
        }

        return years[0];
    }

    private final URI baseUri;
    private final Long permitId;
    private final Long clubId;
    private final int huntingYear;
    private final String rhyOfficialCode;
    private final String rkaOfficialCode;
    private final String htaOfficialCode;
    private final String clubOfficialCode;
    private final boolean isInPilot;

    public LukeReportUriBuilder(final @Nonnull URI baseUri,
                                final @Nonnull HarvestPermit permit,
                                final HuntingClub club,
                                final boolean isInPilot) {
        this.baseUri = Objects.requireNonNull(baseUri);
        this.permitId = permit.getId();
        this.clubId = F.getId(club);
        this.huntingYear = findHuntingYear(permit);
        this.rhyOfficialCode = permit.getRhy().getOfficialCode();
        this.rkaOfficialCode = permit.getRhy().getParentOrganisation().getOfficialCode();
        this.htaOfficialCode = permit.getMooseArea() != null ? permit.getMooseArea().getNumber() : null;
        this.clubOfficialCode = club != null ? club.getOfficialCode() : null;
        this.isInPilot = isInPilot;
    }

    public Long getPermitId() {
        return permitId;
    }

    public Long getClubId() {
        return clubId;
    }

    public URI getCheckClubReportExistsUri() {
        if (clubOfficialCode == null) {
            return null;
        }

        return getReportUri(
                LukeReportParams.LukeArea.CLUB,
                LukeReportParams.Presentation.MOOSE_FIGURE,
                LukeReportParams.Presentation.MOOSE_FIGURE.getFileNames().get(0));
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    @Nonnull
    public URI getReportUri(final LukeReportParams.LukeArea org,
                            final LukeReportParams.Presentation presentation,
                            final String fileName) {
        return UriComponentsBuilder.fromUri(baseUri)
                .replacePath(Joiner.on('/').skipNulls().join(
                        this.huntingYear,
                        presentation.getLukeType(),
                        org.getLukeValue(),
                        // we want to strip leading zeros, but '000' should map to '0'
                        Long.parseLong(getOrganisationId(org)),
                        presentation.getLukeValue(),
                        presentation.composeFileName(fileName)))
                .build()
                .toUri();
    }

    public boolean isInPilot() {
        return isInPilot;
    }

    @Nonnull
    private String getOrganisationId(final LukeReportParams.LukeArea area) {
        switch (area) {
            case CLUB:
                return Objects.requireNonNull(clubOfficialCode);
            case RHY:
                return Objects.requireNonNull(rhyOfficialCode);
            case HTA:
                return Objects.requireNonNull(htaOfficialCode, "permit.mooseArea should not be null, permitId:" + this.permitId);
            case AREA:
                return Objects.requireNonNull(rkaOfficialCode);
            case COUNTRY:
                return Riistakeskus.OFFICIAL_CODE;
            default:
                return "";
        }
    }
}
