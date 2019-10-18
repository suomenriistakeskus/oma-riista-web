package fi.riista.feature.huntingclub.area;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gis.kiinteisto.GISPropertyExcelChangedView;
import fi.riista.feature.gis.kiinteisto.GISPropertyExcelExportService;
import fi.riista.feature.gis.kiinteisto.GISPropertyExcelListView;
import fi.riista.feature.gis.kiinteisto.GISPropertyExcelRow;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneMmlPropertyIntersectionDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.security.EntityPermission.READ;
import static java.util.Collections.emptyList;

@Component
public class HuntingClubAreaExcelFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISPropertyExcelExportService gisPropertyExcelExportService;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public GISPropertyExcelListView exportAll(final long clubAreaId) {
        final HuntingClubArea huntingClubArea = requireEntityService.requireHuntingClubArea(clubAreaId, READ);
        final GISZone zone = huntingClubArea.getZone();

        if (zone == null) {
            return createListView(huntingClubArea, emptyList(), emptyList());
        }

        return createListView(huntingClubArea,
                gisPropertyExcelExportService.fetchAll(zone),
                gisPropertyExcelExportService.findIntersectingPalsta(zone));
    }

    @Transactional(readOnly = true)
    public GISPropertyExcelChangedView exportChanged(final long clubAreaId) {
        final HuntingClubArea huntingClubArea = requireEntityService.requireHuntingClubArea(clubAreaId, READ);
        final GISZone zone = huntingClubArea.getZone();

        if (zone == null) {
            return createChangedView(huntingClubArea, emptyList());
        }

        return createChangedView(huntingClubArea, gisPropertyExcelExportService.fetchChanged(zone));
    }

    private GISPropertyExcelListView createListView(final HuntingClubArea huntingClubArea,
                                                    final List<GISPropertyExcelRow> basicRows,
                                                    final List<GISZoneMmlPropertyIntersectionDTO> calculatedRows) {
        return new GISPropertyExcelListView(
                new EnumLocaliser(messageSource, LocaleContextHolder.getLocale()),
                huntingClubArea.getClub().getNameLocalisation(),
                huntingClubArea.getNameLocalisation(),
                basicRows,
                calculatedRows);
    }

    private GISPropertyExcelChangedView createChangedView(final HuntingClubArea huntingClubArea,
                                                          final List<GISPropertyExcelChangedView.ExcelRow> rows) {
        return new GISPropertyExcelChangedView(
                new EnumLocaliser(messageSource, LocaleContextHolder.getLocale()),
                huntingClubArea.getClub().getNameLocalisation(),
                huntingClubArea.getNameLocalisation(),
                rows);
    }
}
