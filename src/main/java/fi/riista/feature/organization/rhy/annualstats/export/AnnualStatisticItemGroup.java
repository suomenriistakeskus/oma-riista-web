package fi.riista.feature.organization.rhy.annualstats.export;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class AnnualStatisticItemGroup {

    private final LocalisedString title;
    private final AnnualStatisticsCategory category;
    private final int printoutPageNumber;
    private final List<AnnualStatisticItemMeta> itemMetadatas;

    private AnnualStatisticItemGroup(@Nonnull final LocalisedString title,
                                     @Nonnull final AnnualStatisticsCategory category,
                                     final int printoutPageNumber,
                                     @Nonnull final List<AnnualStatisticItemMeta> itemMetadatas) {

        this.title = requireNonNull(title, "title is null");
        this.category = requireNonNull(category, "category is null");
        this.printoutPageNumber = printoutPageNumber;
        this.itemMetadatas = ImmutableList.copyOf(requireNonNull(itemMetadatas, "itemMetadatas is null"));
    }

    public LocalisedString getTitle() {
        return title;
    }

    public AnnualStatisticsCategory getCategory() {
        return category;
    }

    public int getPrintoutPageNumber() {
        return printoutPageNumber;
    }

    public List<AnnualStatisticItemMeta> getItemMetadatas() {
        return itemMetadatas;
    }

    public static class Builder<T> {

        private final AnnualStatisticItemGroupId groupId;
        private final Function<? super AnnualStatisticsExportItemDTO, T> dataExtractor;
        private final EnumLocaliser localiser;
        private final List<AnnualStatisticItemMeta> itemMetadatas = new ArrayList<>();

        Builder(@Nonnull final AnnualStatisticItemGroupId groupId,
                @Nonnull final Function<? super AnnualStatisticsExportItemDTO, T> dataExtractor,
                @Nonnull final EnumLocaliser localiser) {

            this.groupId = requireNonNull(groupId, "groupId is null");
            this.dataExtractor = requireNonNull(dataExtractor, "dataExtractor is null");
            this.localiser = requireNonNull(localiser, "localiser is null");
        }

        public Builder<T> addNumberItem(@Nonnull final LocalisedString title,
                                        @Nonnull final Function<? super T, ? extends Number> numberExtractor) {

            return addNumberItem(title, numberExtractor, false);
        }

        public Builder<T> addNumberItem(@Nonnull final LocalisedString title,
                                        @Nonnull final Function<? super T, ? extends Number> numberExtractor,
                                        final boolean indentedOnPrintout) {

            itemMetadatas.add(AnnualStatisticItemMeta
                    .forNumber(title, dataExtractor.andThen(numberExtractor), indentedOnPrintout));
            return this;
        }

        public Builder<T> addNumberItem(@Nonnull final AnnualStatisticItemId statisticId,
                                        @Nonnull final Function<? super T, ? extends Number> numberExtractor) {

            return addNumberItem(
                    localiser.getLocalisedString(statisticId), numberExtractor, statisticId.isIndentedOnPrintout());
        }

        public Builder<T> addTextItem(@Nonnull final AnnualStatisticItemId title,
                                      @Nonnull final Function<? super T, String> textExtractor) {

            itemMetadatas.add(AnnualStatisticItemMeta
                    .forText(localiser.getLocalisedString(title), dataExtractor.andThen(textExtractor)));
            return this;
        }

        public AnnualStatisticItemGroup build() {
            return new AnnualStatisticItemGroup(
                    localiser.getLocalisedString(groupId),
                    groupId.getCategory(),
                    groupId.getPrintoutPageNumber(),
                    itemMetadatas);
        }
    }
}
