package fi.riista.feature.permit.application.fragment;

import org.joda.time.DateTime;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class PrintApplicationAreaFragmentDTO {
    private final long zoneId;
    private final String applicationName;
    private final DateTime applicationSubmitDate;
    private final String permitHolderName;
    private final String fragmentId;
    private final HarvestPermitAreaFragmentSizeDTO fragmentSize;
    private final List<HarvestPermitAreaFragmentPropertyDTO> fragmentProperties;

    public PrintApplicationAreaFragmentDTO(final long zoneId,
                                           final String fragmentId,
                                           final String applicationName,
                                           final DateTime applicationSubmitDate,
                                           final String permitHolderName,
                                           final HarvestPermitAreaFragmentSizeDTO fragmentSize,
                                           final List<HarvestPermitAreaFragmentPropertyDTO> fragmentProperties) {
        this.zoneId = zoneId;
        this.fragmentId = requireNonNull(fragmentId);
        this.applicationName = requireNonNull(applicationName);
        this.permitHolderName = requireNonNull(permitHolderName);
        this.applicationSubmitDate = requireNonNull(applicationSubmitDate);
        this.fragmentSize = requireNonNull(fragmentSize);
        this.fragmentProperties = requireNonNull(fragmentProperties);
    }

    public long getZoneId() {
        return zoneId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public DateTime getApplicationSubmitDate() {
        return applicationSubmitDate;
    }

    public String getPermitHolderName() {
        return permitHolderName;
    }

    public String getFragmentId() {
        return fragmentId;
    }

    public HarvestPermitAreaFragmentSizeDTO getFragmentSize() {
        return fragmentSize;
    }

    public List<HarvestPermitAreaFragmentPropertyDTO> getFragmentProperties() {
        return fragmentProperties;
    }
}
