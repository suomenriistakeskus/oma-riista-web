package fi.riista.feature.organization.rhy.annualstats.export;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.F;
import fi.riista.util.LocalisedEnum;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import static fi.riista.util.Collect.toMap;
import static fi.riista.util.DateUtil.now;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class AnnualStatisticsExcelView extends AbstractXlsxView {

    private final int calendarYear;
    private final List<AnnualStatisticsExportDTO> rhyList;
    private final AnnualStatisticsExcelLayout layout;
    private final EnumLocaliser localiser;
    private final Map<AnnualStatisticsCategory, List<AnnualStatisticGroup>> groupedStatistics;

    public AnnualStatisticsExcelView(final int calendarYear,
                                     @Nonnull final List<AnnualStatisticsExportDTO> rhyList,
                                     @Nonnull final EnumLocaliser localiser,
                                     @Nonnull final AnnualStatisticsExcelLayout layout) {

        this.calendarYear = calendarYear;
        this.rhyList = requireNonNull(rhyList, "rhyList is null");
        this.layout = requireNonNull(layout, "layout is null");
        this.localiser = requireNonNull(localiser, "localiser is null");

        this.groupedStatistics = AnnualStatisticGroupsFactory
                .getAllGroups(calendarYear)
                .stream()
                .collect(groupingBy(AnnualStatisticGroup::getCategory, TreeMap::new, toList()));
    }

    @Override
    protected final void buildExcelDocument(final Map<String, Object> map,
                                            final Workbook workbook,
                                            final HttpServletRequest request,
                                            final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, constructFilename());

        switch (layout) {
            case NORMAL:

                groupedStatistics.forEach((category, groups) -> {
                    addSheetForNormalLayout(workbook, category, groups, rhyList);
                });
                break;

            case TRANSPOSED:

                groupedStatistics.forEach((category, groups) -> addSheetForTransposedLayout(workbook, category, groups));
                break;

            case WITH_RKA_GROUPING:

                groupedStatistics.forEach((category, groups) -> {
                    addSheetForRkaGroupedLayout(workbook, category, groups, createListOfRkaStatistics());
                });
                break;
        }
    }

    private String constructFilename() {
        final String organisationLevel;

        if (layout == AnnualStatisticsExcelLayout.WITH_RKA_GROUPING) {
            organisationLevel = i18n("rkaAbbrv");
        } else {
            if (rhyList.size() == 1) {
                organisationLevel = i18n(rhyList.get(0).getOrganisation().getNameLocalisation());
            } else {
                organisationLevel = i18n("allOfFinland");
            }
        }

        return format("%s-%d-%s-%s.xlsx",
                i18n("annualStatistics"),
                calendarYear,
                organisationLevel.replaceAll(" ", "_"),
                Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private List<RkaStatistics> createListOfRkaStatistics() {
        final SortedMap<String, List<AnnualStatisticsExportDTO>> rhyListGroupedByRkaCode = rhyList
                .stream()
                .collect(groupingBy(dto -> dto.getParentOrganisation().getOfficialCode(), TreeMap::new, toList()));

        return rhyListGroupedByRkaCode.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .map(RkaStatistics::new)
                .collect(toList());
    }

    private void addSheetForNormalLayout(final Workbook workbook,
                                         final AnnualStatisticsCategory category,
                                         final List<AnnualStatisticGroup> statisticGroups,
                                         final List<AnnualStatisticsExportDTO> allRhyStatistics) {

        final ExcelHelper sheetWrapper = new ExcelHelper(workbook, i18n(category))
                // Freeze statistic item title rows and RHY column.
                .withFreezePane(1, 2)
                .appendRow()
                .appendTextCellBold(format("%s %d", i18n("financialYear"), calendarYear));

        appendStatisticGroupTitleRow(sheetWrapper, statisticGroups);

        // Append titles of statistic items.
        sheetWrapper.appendRow().appendEmptyCell(1);
        appendStatisticItemTitles(sheetWrapper, statisticGroups);

        allRhyStatistics.forEach(rhyStatistics -> {

            final String rhyName = i18n(rhyStatistics.getOrganisation().getNameLocalisation());
            final String rhyTitle = format("%s %s", rhyStatistics.getOrganisation().getOfficialCode(), rhyName);

            // Append new row for RHY statistics starting with title column.
            sheetWrapper.appendRow().appendTextCellBold(rhyTitle);

            appendStatistics(sheetWrapper, statisticGroups, rhyStatistics);
        });

        if (allRhyStatistics.size() > 1) {
            // Append summary row.
            sheetWrapper.appendRow().appendTextCellBold(i18n("total").toUpperCase());
            appendStatistics(sheetWrapper, statisticGroups, AnnualStatisticsExportDTO.aggregate(allRhyStatistics));
        }

        sheetWrapper.autoSizeColumns();
    }

    private void addSheetForRkaGroupedLayout(final Workbook workbook,
                                             final AnnualStatisticsCategory category,
                                             final List<AnnualStatisticGroup> statisticGroups,
                                             final List<RkaStatistics> allRkaStatistics) {

        final ExcelHelper sheetWrapper = new ExcelHelper(workbook, i18n(category))
                // Freeze statistic item title rows and RHY columns.
                .withFreezePane(2, 2)
                .appendRow()
                .appendTextCellBold(format("%s %d", i18n("financialYear"), calendarYear))
                .appendEmptyCell(1);

        appendStatisticGroupTitleRow(sheetWrapper, statisticGroups);

        // Append titles of statistic items.
        sheetWrapper.appendRow()
                .appendTextCell(i18n("rhyNumber"), HorizontalAlignment.RIGHT)
                .appendTextCell(i18n(OrganisationType.RHY));
        appendStatisticItemTitles(sheetWrapper, statisticGroups);

        allRkaStatistics.forEach(rkaStats -> appendRkaStatistics(sheetWrapper, statisticGroups, rkaStats));

        // Append summary line which is an aggregate of all RKA summaries.

        sheetWrapper.appendRow()
                .appendEmptyCell(1)
                .appendTextCell(i18n("totalAll").toUpperCase());

        final List<AnnualStatisticsExportDTO> rkaSummaries =
                F.mapNonNullsToList(allRkaStatistics, rkaStats -> rkaStats.summary);

        appendStatistics(sheetWrapper, statisticGroups, AnnualStatisticsExportDTO.aggregate(rkaSummaries));

        sheetWrapper.autoSizeColumns();
    }

    private void appendRkaStatistics(final ExcelHelper sheetWrapper,
                                     final List<AnnualStatisticGroup> groups,
                                     final RkaStatistics rkaStatistics) {

        final LocalisedString rkaName = rkaStatistics.summary.getOrganisation().getNameLocalisation();

        sheetWrapper.appendRow()
                .appendEmptyCell(1)
                .appendTextCellBold(i18n(rkaName).toUpperCase());

        rkaStatistics.rhyList.forEach(rhyStatistics -> {
            appendRhyStatisticsForRkaGroupedLayout(sheetWrapper, groups, rhyStatistics);
        });

        // Append RKA summary line.

        sheetWrapper.appendRow()
                .appendEmptyCell(1)
                .appendTextCell(i18n("total"));

        appendStatistics(sheetWrapper, groups, rkaStatistics.summary);

        sheetWrapper.appendRow();
    }

    private void appendRhyStatisticsForRkaGroupedLayout(final ExcelHelper sheetWrapper,
                                                        final List<AnnualStatisticGroup> groups,
                                                        final AnnualStatisticsExportDTO statistics) {

        final OrganisationNameDTO rhy = statistics.getOrganisation();

        sheetWrapper.appendRow()
                .appendTextCell(rhy.getOfficialCode(), HorizontalAlignment.RIGHT)
                .appendTextCell(i18n(rhy.getNameLocalisation()));

        appendStatistics(sheetWrapper, groups, statistics);
    }

    private void appendStatisticGroupTitleRow(final ExcelHelper sheetWrapper, final List<AnnualStatisticGroup> groups) {
        extractTitlesAndNumberOfItems(groups).forEach((title, numberOfItems) -> {
            sheetWrapper.appendTextCellBold(title).appendEmptyCell(numberOfItems - 1);
        });
    }

    private LinkedHashMap<String, Integer> extractTitlesAndNumberOfItems(final List<AnnualStatisticGroup> groups) {
        return groups.stream().collect(toMap(this::i18n, grp -> grp.getItems().size(), LinkedHashMap::new));
    }

    private void appendStatisticItemTitles(final ExcelHelper sheetWrapper, final List<AnnualStatisticGroup> groups) {
        streamItems(groups).map(this::i18n).forEach(sheetWrapper::appendTextCell);
    }

    private static void appendStatistics(final ExcelHelper sheetWrapper,
                                         final List<AnnualStatisticGroup> groups,
                                         final AnnualStatisticsExportDTO statistics) {

        streamItems(groups).forEach(item -> populateExcelCell(sheetWrapper, item, statistics));
    }

    private void addSheetForTransposedLayout(final Workbook workbook,
                                             final AnnualStatisticsCategory category,
                                             final List<AnnualStatisticGroup> groups) {

        final ExcelHelper sheetWrapper = appendHeaderRowsForTransposedLayout(workbook, category);

        groups.forEach(group -> {

            sheetWrapper.appendTextCellBold(i18n(group)).appendRow();

            group.getItems().forEach(item -> {
                sheetWrapper.appendTextCell(i18n(item));
                rhyList.forEach(rhy -> populateExcelCell(sheetWrapper, item, rhy));
                sheetWrapper.appendRow();
            });

            sheetWrapper.appendRow();
        });

        sheetWrapper.autoSizeColumns();
    }

    private ExcelHelper appendHeaderRowsForTransposedLayout(final Workbook workbook,
                                                            final AnnualStatisticsCategory category) {

        final ExcelHelper sheetWrapper = new ExcelHelper(workbook, i18n(category))
                // Freeze title column (the first one in a sheet).
                .withFreezePane(1, 0)
                .appendRow()
                .appendTextCell(i18n("financialYear"));

        final int numRhys = rhyList.size();

        for (int i = 0; i < numRhys; i++) {
            sheetWrapper.appendNumberCell(calendarYear);
        }

        sheetWrapper.appendRow().appendTextCell(i18n(OrganisationType.RHY));

        for (final AnnualStatisticsExportDTO stats : rhyList) {
            sheetWrapper.appendTextCell(i18n(stats.getOrganisation().getNameLocalisation()), HorizontalAlignment.RIGHT);
        }

        sheetWrapper.appendRow().appendTextCell(i18n("rhyNumber"));

        for (final AnnualStatisticsExportDTO stats : rhyList) {
            sheetWrapper.appendTextCell(i18n(stats.getOrganisation().getOfficialCode()), HorizontalAlignment.RIGHT);
        }

        return sheetWrapper.appendRow().appendRow();
    }

    private static Stream<AnnualStatisticItem> streamItems(final List<AnnualStatisticGroup> groups) {
        return groups.stream().flatMap(group -> group.getItems().stream());
    }

    private String i18n(final String value) {
        return localiser.getTranslation(value);
    }

    private String i18n(final LocalisedString value) {
        return localiser.getTranslation(value);
    }

    private <E extends Enum<E> & LocalisedEnum> String i18n(final E value) {
        return localiser.getTranslation(value);
    }

    private static void populateExcelCell(final ExcelHelper sheetWrapper,
                                          final AnnualStatisticItem item,
                                          final AnnualStatisticsExportDTO statistics) {
        item.extractValue(statistics)
                .peek(text -> sheetWrapper.appendTextCell(text, HorizontalAlignment.RIGHT))
                .peekLeft(sheetWrapper::appendNumberCell);
    }

    private static class RkaStatistics {

        final List<AnnualStatisticsExportDTO> rhyList;
        final AnnualStatisticsExportDTO summary;

        RkaStatistics(@Nonnull final List<AnnualStatisticsExportDTO> rhyList) {
            this.rhyList = requireNonNull(rhyList);

            this.summary = AnnualStatisticsExportDTO.aggregate(rhyList);
            this.summary.setOrganisation(rhyList.get(0).getParentOrganisation());
        }
    }
}
