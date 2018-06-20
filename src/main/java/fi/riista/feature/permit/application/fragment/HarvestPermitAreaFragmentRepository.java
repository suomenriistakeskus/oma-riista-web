package fi.riista.feature.permit.application.fragment;

import fi.riista.feature.gis.GISPoint;

import java.util.List;

public interface HarvestPermitAreaFragmentRepository {

    List<HarvestPermitAreaFragmentInfoDTO> getFragmentInfo(Long applicationId);

    List<HarvestPermitAreaFragmentInfoDTO> getFragmentInfoInLocation(Long id, GISPoint gisPoint);
}
