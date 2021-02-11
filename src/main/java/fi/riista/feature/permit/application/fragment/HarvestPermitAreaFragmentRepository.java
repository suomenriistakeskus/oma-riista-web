package fi.riista.feature.permit.application.fragment;

import java.util.List;
import java.util.Map;

public interface HarvestPermitAreaFragmentRepository {
    List<HarvestPermitAreaFragmentSizeDTO> getFragmentSize(HarvestPermitAreaFragmentQueryParams params);

    List<HarvestPermitAreaFragmentSizeDTO> getFragmentSize(final HarvestPermitAreaFragmentQueryParams params,
                                                           final List<String> fragmentHashes);

    Map<String, List<HarvestPermitAreaFragmentPropertyDTO>> getFragmentProperty(HarvestPermitAreaFragmentQueryParams params);
}
