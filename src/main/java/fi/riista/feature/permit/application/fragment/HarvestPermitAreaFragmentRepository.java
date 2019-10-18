package fi.riista.feature.permit.application.fragment;

import java.util.List;
import java.util.Map;

public interface HarvestPermitAreaFragmentRepository {
    List<HarvestPermitAreaFragmentSizeDTO> getFragmentSize(HarvestPermitAreaFragmentQueryParams params);

    Map<String, List<HarvestPermitAreaFragmentPropertyDTO>> getFragmentProperty(HarvestPermitAreaFragmentQueryParams params);
}
