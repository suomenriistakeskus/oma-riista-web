package fi.riista.feature.account.area;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gis.kiinteisto.GISPropertyExcelChangedView;
import fi.riista.feature.gis.kiinteisto.GISPropertyExcelExportService;
import fi.riista.feature.gis.kiinteisto.GISPropertyExcelListView;
import fi.riista.feature.gis.kiinteisto.GISPropertyExcelRow;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneMmlPropertyIntersectionDTO;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.security.EntityPermission.READ;
import static java.util.Collections.emptyList;

@Component
public class PersonalAreaExcelFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISPropertyExcelExportService gisPropertyExcelExportService;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public GISPropertyExcelListView exportAll(final long areaId) {
        final PersonalArea personalArea = requireEntityService.requirePersonalArea(areaId, READ);
        final GISZone zone = personalArea.getZone();

        if (zone == null) {
            return createListView(personalArea, emptyList(), emptyList());
        }

        return createListView(personalArea,
                gisPropertyExcelExportService.fetchAll(zone),
                gisPropertyExcelExportService.findIntersectingPalsta(zone));
    }

    @Transactional(readOnly = true)
    public GISPropertyExcelChangedView exportChanged(final long areaId) {
        final PersonalArea personalArea = requireEntityService.requirePersonalArea(areaId, READ);
        final GISZone zone = personalArea.getZone();

        if (zone == null) {
            return createChangedView(personalArea, emptyList());
        }

        return createChangedView(personalArea, gisPropertyExcelExportService.fetchChanged(zone));
    }

    private GISPropertyExcelListView createListView(final PersonalArea area,
                                                    final List<GISPropertyExcelRow> basicRows,
                                                    final List<GISZoneMmlPropertyIntersectionDTO> calculatedRows) {
        return new GISPropertyExcelListView(
                new EnumLocaliser(messageSource, LocaleContextHolder.getLocale()),
                LocalisedString.of(area.getPerson().getFullName()),
                LocalisedString.of(area.getName()),
                basicRows,
                calculatedRows);
    }

    private GISPropertyExcelChangedView createChangedView(final PersonalArea area,
                                                          final List<GISPropertyExcelChangedView.ExcelRow> rows) {
        return new GISPropertyExcelChangedView(
                new EnumLocaliser(messageSource, LocaleContextHolder.getLocale()),
                LocalisedString.of(area.getPerson().getFullName()),
                LocalisedString.of(area.getName()),
                rows);
    }
}
