package fi.riista.feature.permit.application.fragment;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.GISPoint;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.security.EntityPermission;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component
public class ListApplicationAreaFragmentsFeature {

    @Resource
    private HarvestPermitAreaFragmentRepository fragmentRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public Map<String, Object> getFragmentInfo(final long applicationId, final GeoLocation location) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.READ);

        if (application.getArea() == null) {
            return Collections.emptyMap();
        }

        final List<HarvestPermitAreaFragmentInfoDTO> infos = fragmentRepository.getFragmentInfoInLocation(
                application.getId(), GISPoint.create(location));

        if (infos.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, Object> map = new HashMap<>();

        // At given location, there should be only one fragment, therefore any of the dtos should have
        // same information of the whole fragment.
        final HarvestPermitAreaFragmentInfoDTO first = infos.get(0);
        map.put("waterAreaSize", first.getWaterAreaSize());
        map.put("valtionmaaAreaSize", first.getValtionmaaAreaSize());
        map.put("valtionmaaWaterAreaSize", first.getValtionmaaWaterAreaSize());
        map.put("yksityismaaAreaSize", first.getAreaSize() - first.getValtionmaaAreaSize());
        map.put("yksityismaaWaterAreaSize", first.getWaterAreaSize() - first.getValtionmaaWaterAreaSize());
        map.put("areaSize", first.getAreaSize());
        map.put(GeoJSONConstants.PROPERTY_HASH, first.getHash());

        final List<PropertyNumberAreaSizeDTO> properties = infos.stream()
                .map(i -> new PropertyNumberAreaSizeDTO(i.getPropertyNumber(), i.getPropertyArea(), i.isMetsahallitus()))
                .collect(toList());
        map.put("propertyNumbers", properties);

        return map;
    }

    @Transactional(readOnly = true)
    public HarvestPermitAreaFragmentExcelView getFragmentExcel(long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.READ);

        final Locale locale = LocaleContextHolder.getLocale();
        final EnumLocaliser enumLocaliser = new EnumLocaliser(messageSource, locale);

        final List<HarvestPermitAreaFragmentInfoDTO> rows = fragmentRepository.getFragmentInfo(application.getId());
        return new HarvestPermitAreaFragmentExcelView(enumLocaliser, application.getPermitNumber(), rows);
    }
}
