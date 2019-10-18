package fi.riista.integration.metsahallitus.mobile;

import fi.riista.integration.metsahallitus.permit.MetsahallitusPermit;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class MetsahallitusMobilePermitDTO {

    public static MetsahallitusMobilePermitDTO create(final @Nonnull MetsahallitusPermit permit) {
        final String harvestFeedbackUrl = permit.getUrl();

        return new MetsahallitusMobilePermitDTO(permit.getPermitIdentifier(),
                permit.getPermitTypeLocalisation().asMap(),
                permit.getPermitNameLocalisation().asMap(),
                permit.getAreaNumber(),
                permit.getAreaNameLocalisation().asMap(),
                LocalisedString.of(harvestFeedbackUrl, harvestFeedbackUrl).asMap(),
                permit.getBeginDate(),
                permit.getEndDate());
    }

    private final String permitIdentifier;
    private final Map<String, String> permitType;
    private final Map<String, String> permitName;
    private final String areaNumber;
    private final Map<String, String> areaName;
    private final Map<String, String> harvestFeedbackUrl;
    private final LocalDate beginDate;
    private final LocalDate endDate;

    public MetsahallitusMobilePermitDTO(final String permitIdentifier,
                                        final @Nonnull Map<String, String> permitType,
                                        final @Nonnull Map<String, String> permitName,
                                        final String areaNumber,
                                        final @Nonnull Map<String, String> areaName,
                                        final @Nonnull Map<String, String> harvestFeedbackUrl,
                                        final LocalDate beginDate,
                                        final LocalDate endDate) {
        this.permitIdentifier = permitIdentifier;
        this.permitType = requireNonNull(permitType);
        this.permitName = requireNonNull(permitName);
        this.areaNumber = areaNumber;
        this.areaName = requireNonNull(areaName);
        this.harvestFeedbackUrl = requireNonNull(harvestFeedbackUrl);
        this.beginDate = beginDate;
        this.endDate = endDate;
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

    public Map<String, String> getHarvestFeedbackUrl() {
        return harvestFeedbackUrl;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
