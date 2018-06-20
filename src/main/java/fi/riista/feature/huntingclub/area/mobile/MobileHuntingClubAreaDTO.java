package fi.riista.feature.huntingclub.area.mobile;

import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDateTime;

import java.util.Map;
import java.util.Objects;

public class MobileHuntingClubAreaDTO {

    public enum AreaType {
        CLUB,
        PERMIT
    }

    private final AreaType type;
    private final int huntingYear;
    private final Map<String, String> name;
    private final Map<String, String> clubName;
    private final String externalId;
    private final LocalDateTime modificationTime;

    public MobileHuntingClubAreaDTO(final HuntingClubArea area) {
        this.type = AreaType.CLUB;
        this.huntingYear = area.getHuntingYear();
        this.name = area.getNameLocalisation().asMap();
        this.clubName = area.getClub().getNameLocalisation().asMap();
        this.externalId = Objects.requireNonNull(area.getExternalId());
        this.modificationTime = DateUtil.toLocalDateTimeNullSafe(area.getLifecycleFields().getModificationTime());
    }

    public MobileHuntingClubAreaDTO(final HarvestPermitArea area) {
        this.type = AreaType.PERMIT;
        this.huntingYear = area.getHuntingYear();
        this.name = huntingYear < 2018
                ? area.getNameLocalisation().asMap()
                : LocalisedString.of(area.getExternalId(), area.getExternalId()).asMap();
        this.clubName = area.getClub() != null
                ? area.getClub().getNameLocalisation().asMap()
                : LocalisedString.of("Lupa-alue", "Licensområde").asMap();
        this.externalId = area.getExternalId();
        this.modificationTime = DateUtil.toLocalDateTimeNullSafe(area.getLifecycleFields().getModificationTime());
    }

    public AreaType getType() {
        return type;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public Map<String, String> getName() {
        return name;
    }

    public Map<String, String> getClubName() {
        return clubName;
    }

    public String getExternalId() {
        return externalId;
    }

    public LocalDateTime getModificationTime() {
        return modificationTime;
    }
}
