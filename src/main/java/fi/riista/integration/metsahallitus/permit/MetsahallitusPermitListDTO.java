package fi.riista.integration.metsahallitus.permit;

import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Map;

public class MetsahallitusPermitListDTO {

    public static MetsahallitusPermitListDTO create(final @Nonnull MetsahallitusPermit permit) {
        return new MetsahallitusPermitListDTO(permit.getPermitIdentifier(),
                permit.getPermitTypeLocalisation().asMap(),
                permit.getPermitNameLocalisation().asMap(),
                permit.getAreaNumber(),
                permit.getAreaNameLocalisation().asMap(),
                permit.getBeginDate(),
                permit.getEndDate(),
                DateUtil.toDateTimeNullSafe(permit.getCreationTime()),
                permit.getUrl());
    }

    private final String permitIdentifier;
    private final Map<String, String> permitType;
    private final Map<String, String> permitName;
    private final String areaNumber;
    private final Map<String, String> areaName;
    private final LocalDate beginDate;
    private final LocalDate endDate;
    private final DateTime creationTime;
    private final String url;

    public MetsahallitusPermitListDTO(final String permitIdentifier,
                                      final Map<String, String> permitType,
                                      final Map<String, String> permitName,
                                      final String areaNumber,
                                      final Map<String, String> areaName,
                                      final LocalDate beginDate,
                                      final LocalDate endDate,
                                      final DateTime creationTime,
                                      final String url) {
        this.permitIdentifier = permitIdentifier;
        this.permitType = permitType;
        this.permitName = permitName;
        this.areaNumber = areaNumber;
        this.areaName = areaName;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.creationTime = creationTime;
        this.url = url;
    }

    public String getPermitIdentifier() {
        return permitIdentifier;
    }

    public Map<String, String> getPermitType() {
        return permitType;
    }

    public Map<String, String> getPermitName() {
        return permitName;
    }

    public String getAreaNumber() {
        return areaNumber;
    }

    public Map<String, String> getAreaName() {
        return areaName;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public String getUrl() {
        return url;
    }
}
