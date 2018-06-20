package fi.riista.feature.organization.rhy.annualstats.export;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedEnum;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportItemDTO.aggregate;
import static fi.riista.util.DateUtil.now;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.capitalize;

public class AnnualStatisticsExcelView extends AbstractXlsxView {

    private final int calendarYear;
    private final List<AnnualStatisticsExportItemDTO> rhyList;
    private final List<AnnualStatisticItemGroup> statisticGroups;
    private final AnnualStatisticsExcelLayout layout;
    private final EnumLocaliser localiser;

    public AnnualStatisticsExcelView(final int calendarYear,
                                     @Nonnull final List<AnnualStatisticsExportItemDTO> rhyList,
                                     @Nonnull final List<AnnualStatisticItemGroup> statisticGroups,
                                     @Nonnull final EnumLocaliser localiser,
                                     @Nonnull final AnnualStatisticsExcelLayout layout) {

        this.calendarYear = calendarYear;
        this.rhyList = requireNonNull(rhyList, "rhyList is null");
        this.statisticGroups = requireNonNull(statisticGroups, "statisticGroups is null");
        this.layout = requireNonNull(layout, "layout is null");
        this.localiser = requireNonNull(localiser, "localiser is null");
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
                createNormalSheet(workbook);
                break;
            case TRANSPOSED_WITH_MULTIPLE_SHEETS:
                createTransposedSheets(workbook);
                break;
            case WITH_RKA_GROUPING:
                createRkaGroupedSheet(workbook);
                break;
        }
    }

    private String constructFilename() {
        final String organisationLevel;

        if (layout == AnnualStatisticsExcelLayout.WITH_RKA_GROUPING) {
            organisationLevel = localise("rkaAbbrv");
        } else {
            if (rhyList.size() == 1) {
                organisationLevel = localise(rhyList.get(0).getOrganisationName());
            } else {
                organisationLevel = localise("allOfFinland");
            }
        }

        return format("%s-%d-%s-%s.xlsx",
                localise("annualStatistics"),
                calendarYear,
                organisationLevel.replaceAll(" ", "_"),
                Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private void createNormalSheet(final Workbook workbook) {
        final ExcelHelper sheetWrapper = new ExcelHelper(workbook)
                // Freeze statistic item title rows and RHY column.
                .createFreezePane(1, 2)
                .appendRow()
                .appendTextCellBold(format("%s %d", localise("financialYear"), calendarYear));

        statisticGroups.forEach(group -> {
            sheetWrapper
                    .appendTextCellBold(localise(group.getTitle()))
                    .appendEmptyCell(group.getItemMetadatas().size() - 1);
        });

        sheetWrapper.appendRow().appendEmptyCell(1);

        statisticGroups.forEach(group -> group.getItemMetadatas().forEach(itemMetadata -> {
            sheetWrapper.appendTextCell(localise(itemMetadata.getTitle()));
        }));

        Stream<AnnualStatisticsExportItemDTO> organisations = rhyList.stream();

        if (rhyList.size() > 1) {
            final AnnualStatisticsExportItemDTO aggregate = AnnualStatisticsExportItemDTO.aggregate(rhyList);
            aggregate.setOrganisationName(getUppercaseLocalisedString("total"));

            organisations = Stream.concat(organisations, Stream.of(aggregate));
        }

        organisations.forEach(org -> {

            final String orgName = localise(org.getOrganisationName());
            final String rowTitle = Optional.ofNullable(org.getOrganisationCode())
                    .map(code -> format("%s %s", code, orgName))
                    .orElse(orgName);

            sheetWrapper.appendRow().appendTextCellBold(rowTitle);

            statisticGroups.forEach(group -> group.getItemMetadatas().forEach(itemMetadata -> {
                itemMetadata.populateExcelCell(sheetWrapper, org);
            }));
        });

        sheetWrapper.autoSizeColumns();
    }

    private void createRkaGroupedSheet(final Workbook workbook) {

        final SortedMap<String, List<AnnualStatisticsExportItemDTO>> rhyGrouping = rhyList.stream()
                .collect(groupingBy(AnnualStatisticsExportItemDTO::getParentOrganisationCode, TreeMap::new, toList()));

        final ExcelHelper sheetWrapper = new ExcelHelper(workbook)
                // Freeze statistic item title rows and RHY columns.
                .createFreezePane(2, 2)
                .appendRow()
                .appendTextCellBold(format("%s %d", localise("financialYear"), calendarYear))
                .appendEmptyCell(1);

        statisticGroups.forEach(group -> {
            sheetWrapper
                    .appendTextCellBold(localise(group.getTitle()))
                    .appendEmptyCell(group.getItemMetadatas().size() - 1);
        });

        sheetWrapper
                .appendRow()
                .appendTextCell(localise("rhyNumber"), HorizontalAlignment.RIGHT)
                .appendTextCell(localise(OrganisationType.RHY));

        statisticGroups.forEach(group -> group.getItemMetadatas().forEach(itemMetadata -> {
            sheetWrapper.appendTextCell(localise(itemMetadata.getTitle()));
        }));

        final List<AnnualStatisticsExportItemDTO> allRkaAggregates = new ArrayList<>();

        rhyGrouping.forEach((rkaCode, rhyList) -> {

            final LocalisedString rkaName = rhyList.get(0).getParentOrganisationName();
            sheetWrapper.appendRow().appendEmptyCell(1).appendTextCellBold(localise(rkaName).toUpperCase());

            final AnnualStatisticsExportItemDTO aggregate = aggregate(rhyList);
            aggregate.setOrganisationName(localiser.getLocalisedString("total"));

            allRkaAggregates.add(aggregate);

            Stream.concat(rhyList.stream(), Stream.of(aggregate))
                    .forEach(dto -> appendRowForRkaGroupedLayout(dto, sheetWrapper, statisticGroups));

            sheetWrapper.appendRow();
        });

        final AnnualStatisticsExportItemDTO allRhysAggregate = aggregate(allRkaAggregates);
        allRhysAggregate.setOrganisationName(getUppercaseLocalisedString("totalAll"));

        appendRowForRkaGroupedLayout(allRhysAggregate, sheetWrapper, statisticGroups);

        sheetWrapper.autoSizeColumns();
    }

    private void appendRowForRkaGroupedLayout(final AnnualStatisticsExportItemDTO dto,
                                              final ExcelHelper sheetWrapper,
                                              final List<AnnualStatisticItemGroup> groups) {

        sheetWrapper
                .appendRow()
                .appendTextCell(dto.getOrganisationCode(), HorizontalAlignment.RIGHT)
                .appendTextCell(localise(dto.getOrganisationName()));

        groups.forEach(group -> group.getItemMetadatas().forEach(itemMetadata -> {
            itemMetadata.populateExcelCell(sheetWrapper, dto);
        }));
    }

    private void createTransposedSheets(final Workbook workbook) {
        statisticGroups.stream()
                .collect(groupingBy(group -> group.getCategory(), TreeMap::new, toList()))
                .forEach((category, groups) -> createTransposedSheet(workbook, category, groups));
    }

    private void createTransposedSheet(final Workbook workbook,
                                       final AnnualStatisticsCategory category,
                                       final List<AnnualStatisticItemGroup> groups) {

        final ExcelHelper sheetWrapper = createCommonHeaderRowsForTransposedLayout(workbook, category);

        groups.forEach(group -> {

            sheetWrapper.appendTextCellBold(localise(group.getTitle())).appendRow();

            group.getItemMetadatas().forEach(itemMetadata -> {
                sheetWrapper.appendTextCell(localise(itemMetadata.getTitle()));
                rhyList.forEach(rhy -> itemMetadata.populateExcelCell(sheetWrapper, rhy));
                sheetWrapper.appendRow();
            });

            sheetWrapper.appendRow();
        });

        sheetWrapper.autoSizeColumns();
    }

    private ExcelHelper createCommonHeaderRowsForTransposedLayout(final Workbook workbook,
                                                                  final AnnualStatisticsCategory category) {

        final ExcelHelper sheetWrapper = new ExcelHelper(workbook, localise(category))
                // Freeze title column (the first one in a sheet).
                .createFreezePane(1, 0)
                .appendRow()
                .appendTextCell(localise("financialYear"));

        final int numRhys = rhyList.size();

        for (int i = 0; i < numRhys; i++) {
            sheetWrapper.appendNumberCell(calendarYear);
        }

        sheetWrapper.appendRow().appendTextCell(localise(OrganisationType.RHY));

        for (final AnnualStatisticsExportItemDTO rhy : rhyList) {
            sheetWrapper.appendTextCell(localise(rhy.getOrganisationName()), HorizontalAlignment.RIGHT);
        }

        sheetWrapper.appendRow().appendTextCell(localise("rhyNumber"));

        for (final AnnualStatisticsExportItemDTO rhy : rhyList) {
            sheetWrapper.appendTextCell(localise(rhy.getOrganisationCode()), HorizontalAlignment.RIGHT);
        }

        return sheetWrapper.appendRow().appendRow();
    }

    private LocalisedString getUppercaseLocalisedString(final String message) {
        return localiser.getLocalisedString(message).transform(s -> s == null ? null : s.toUpperCase());
    }

    private String localise(final String value) {
        return localiser.getTranslation(value);
    }

    private String localise(final LocalisedString value) {
        return capitalize(localiser.getTranslation(value));
    }

    private <E extends Enum<E> & LocalisedEnum> String localise(final E value) {
        return localiser.getTranslation(value);
    }
}
