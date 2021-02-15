package fi.riista.integration.koiratutka.export;

import fi.riista.feature.account.area.PersonalArea;
import fi.riista.feature.account.area.union.PersonalAreaUnion;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.moderatorarea.ModeratorArea;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;
import java.util.Optional;

import static fi.riista.feature.permit.area.HarvestPermitArea.StatusCode.READY;

class AreaExportDTO {

    public static final LocalisedString EMPTY_NAME = LocalisedString.of("", "", "");

    private static LocalisedString getPermitAreaName(final HarvestPermitArea permitArea) {
        final String fi = String.format("Lupa-alue %s", permitArea.getExternalId());
        final String sv = String.format("Licensområde %s", permitArea.getExternalId());
        final String en = String.format("Permit area %s", permitArea.getExternalId());

        return LocalisedString.of(fi, sv, en);
    }

    public static AreaExportDTO create(final HuntingClubArea clubArea) {
        return new AreaExportDTO(clubArea.getNameLocalisation(),
                clubArea.getClub().getNameLocalisation(),
                clubArea.getModificationTime(),
                Integer.toHexString(clubArea.getConsistencyVersion()),
                F.getId(clubArea.getZone()),
                clubArea.getHuntingYear());
    }

    public static AreaExportDTO create(final HarvestPermitArea permitArea) {
        return new AreaExportDTO(getPermitAreaName(permitArea),
                EMPTY_NAME,
                permitArea.getModificationTime(),
                Integer.toHexString(permitArea.getConsistencyVersion()),
                F.getId(permitArea.getZone()),
                permitArea.getHuntingYear());
    }

    public static AreaExportDTO create(final PersonalArea personalArea) {
        final String fullName = personalArea.getPerson().getFullName();
        final String areaName = personalArea.getName();

        return new AreaExportDTO(
                LocalisedString.of(areaName, areaName, areaName),
                LocalisedString.of(fullName, fullName, fullName),
                personalArea.getModificationTime(),
                Integer.toHexString(personalArea.getConsistencyVersion()),
                F.getId(personalArea.getZone()),
                DateUtil.currentYear());
    }

    public static AreaExportDTO create(final ModeratorArea moderatorArea) {
        final String fullName = moderatorArea.getModerator().getFullName();
        final String areaName = moderatorArea.getName();

        return new AreaExportDTO(
                LocalisedString.of(areaName, areaName, areaName),
                LocalisedString.of(fullName, fullName, fullName),
                moderatorArea.getModificationTime(),
                Integer.toHexString(moderatorArea.getConsistencyVersion()),
                F.getId(moderatorArea.getZone()),
                moderatorArea.getYear());
    }

    public static Optional<AreaExportDTO> create(final PersonalAreaUnion personalAreaUnion) {
        final HarvestPermitArea harvestPermitArea = personalAreaUnion.getHarvestPermitArea();
        if (harvestPermitArea.getStatus() == READY) {
            return Optional.of(new AreaExportDTO(LocalisedString.of(personalAreaUnion.getName()),
                    EMPTY_NAME,
                    personalAreaUnion.getModificationTime(),
                    Integer.toHexString(personalAreaUnion.getConsistencyVersion()),
                    F.getId(harvestPermitArea.getZone()),
                    harvestPermitArea.getHuntingYear()));
        }
        return Optional.empty();

    }

    private final LocalisedString areaName;
    private final LocalisedString clubName;
    private final DateTime areaModificationTime;
    private final String responseEtag;
    private final Long zoneId;
    private final int huntingYear;

    AreaExportDTO(final LocalisedString areaName,
                  final LocalisedString clubName,
                  final DateTime areaModificationTime,
                  final String responseEtag,
                  final Long zoneId,
                  final int huntingYear) {
        this.areaName = areaName;
        this.clubName = clubName;
        this.areaModificationTime = areaModificationTime;
        this.responseEtag = responseEtag;
        this.zoneId = zoneId;
        this.huntingYear = huntingYear;
    }

    public String getResponseEtag() {
        return responseEtag;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public DateTime getAreaModificationTime() {
        return areaModificationTime;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public LocalisedString getAreaName() {
        return areaName;
    }

    public LocalisedString getClubName() {
        return clubName;
    }
}
