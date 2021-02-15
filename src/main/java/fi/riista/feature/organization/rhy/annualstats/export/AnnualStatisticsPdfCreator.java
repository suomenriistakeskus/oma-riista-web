package fi.riista.feature.organization.rhy.annualstats.export;

import fi.riista.feature.common.EnumLocaliser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem.BOW_TEST_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem.FIREARM_TEST_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem.SRVA_ALL_LARGE_CARNIVORE_EVENTS_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem.SRVA_ALL_MOOSELIKE_EVENTS_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem.SRVA_ALL_WILD_BOAR_EVENTS_2017;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem.SRVA_OTHER_ACCIDENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem.SRVA_RAILWAY_ACCIDENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem.SRVA_TRAFFIC_ACCIDENTS;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Component
public class AnnualStatisticsPdfCreator {

    private static final ClassPathResource LOGO_IMAGE_RESOURCE = new ClassPathResource("/riistafi-logo.png");

    private static final PDRectangle PAZE_SIZE = PDRectangle.A4;

    // PDF basic unit is 72 dpi.
    private static final float DOTS_PER_MM = 72f / 25.4f;

    private static final PDFont BASIC_FONT = PDType1Font.HELVETICA;
    private static final PDFont TITLE_FONT = PDType1Font.HELVETICA_BOLD;

    private static final float FONT_SIZE = 10.5f;
    private static final float ROW_HEIGHT = 14f;

    // Margins are in millimeters.
    private static final float MARGIN_TOP = 18f;
    private static final float MARGIN_TEXT_LEFT = 25f;
    private static final float MARGIN_TEXT_RIGHT = 20f;
    private static final float MARGIN_LOGO = 8f;
    private static final float MARGIN_BOTTOM = 30f;
    private static final float INDENT_UNIT = 8f;

    // PDF point unit based constants
    private static final float TOP = PAZE_SIZE.getUpperRightY() - MARGIN_TOP * DOTS_PER_MM;
    private static final float LOGO_IMAGE_WIDTH = (MARGIN_TEXT_LEFT - MARGIN_LOGO - /*gap*/4f) * DOTS_PER_MM;

    private static final int MAX_TEXT_LENGTH = 80;

    private static final DecimalFormat NUMBER_FORMATTER;

    private static final EnumSet<AnnualStatisticItem> INDENTED_STATISTIC_ITEMS = EnumSet.of(
            SRVA_ALL_MOOSELIKE_EVENTS_2017, SRVA_ALL_LARGE_CARNIVORE_EVENTS_2017, SRVA_ALL_WILD_BOAR_EVENTS_2017,
            FIREARM_TEST_EVENTS, BOW_TEST_EVENTS, SRVA_TRAFFIC_ACCIDENTS, SRVA_RAILWAY_ACCIDENTS,
            SRVA_OTHER_ACCIDENTS);

    static {
        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(' ');

        NUMBER_FORMATTER = new DecimalFormat("###,###", symbols);
    }

    @Resource
    private MessageSource messageSource;

    public byte[] create(final int year, @Nonnull final AnnualStatisticsExportDTO rhy, final Locale locale) {
        requireNonNull(rhy);

        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);

        try (final PDDocument document = new PDDocument()) {

            AnnualStatisticGroupsFactory
                    .getAllGroups(year)
                    .stream()
                    .collect(groupingBy(AnnualStatisticGroup::getPrintoutPageNumber, TreeMap::new, toList()))
                    .forEach((pageNumber, groups) -> {

                        final PDPage page = new PDPage(PAZE_SIZE);
                        document.addPage(page);

                        try (final PageWriter pageWriter = new PageWriter(document, page, localiser)) {

                            final AnnualStatisticsCategory category = groups.get(0).getCategory();

                            pageWriter.writeHeader(year, category, rhy);
                            pageWriter.writeStatisticGroups(rhy, groups);
                            pageWriter.writePageNumber(pageNumber);

                        } catch (final IOException e) {
                            throw new RuntimeException("Exception while writing page to an annual report PDF", e);
                        }
                    });

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();

        } catch (final IOException e) {
            throw new RuntimeException("Exception while creating annual report PDF document", e);
        }
    }

    private static String replaceUnsupportedUnicodeChars(final String text) {
        return text.replaceAll("\u2011", "-");
    }

    private static boolean isIndented(final AnnualStatisticItem item) {
        return INDENTED_STATISTIC_ITEMS.contains(item);
    }

    private static class PageWriter implements Closeable {

        private final PDDocument document;
        private final PDPageContentStream contentStream;
        private final EnumLocaliser localiser;

        private final float center;
        private final float rightEdge;

        private PDFont activeFont;
        private float posY;

        PageWriter(final PDDocument document, final PDPage page, final EnumLocaliser localiser) throws IOException {
            this.document = document;
            this.contentStream = new PDPageContentStream(document, page);
            this.localiser = localiser;

            this.posY = TOP;

            final float width = page.getMediaBox().getWidth();
            this.center = width / 2;
            this.rightEdge = width - MARGIN_TEXT_RIGHT * DOTS_PER_MM;
        }

        public void writeHeader(final int year,
                                final AnnualStatisticsCategory category,
                                final AnnualStatisticsExportDTO dto) throws IOException {

            writeLogo(LOGO_IMAGE_RESOURCE.getFile());

            final String rhyName = this.localiser.getTranslation(dto.getOrganisation().getNameLocalisation());
            final String documentName = format(
                    "%s %s", this.localiser.getTranslation("annualStatistics").toUpperCase(), String.valueOf(year));

            writeTitle(rhyName);
            writeTextOnRight(documentName);
            writeEmptyLine();

            final String rhyNumber =
                    format("%s %s", this.localiser.getTranslation("rhyNumber"), dto.getOrganisation().getOfficialCode());
            writeTitle(rhyNumber);
            writeEmptyLine();
            writeEmptyLine();

            final String categoryName = this.localiser.getTranslation(category).toUpperCase();
            writeTitle(categoryName);
            writeEmptyLine();
            writeEmptyLine();
        }

        private void writeLogo(final File imageFile) throws IOException {
            final PDImageXObject imageObj = PDImageXObject.createFromFileByContent(imageFile, this.document);
            final float scale = LOGO_IMAGE_WIDTH / imageObj.getWidth();
            final float scaledHeight = scale * imageObj.getHeight();

            contentStream.drawImage(imageObj,
                    MARGIN_LOGO * DOTS_PER_MM, TOP - scaledHeight + FONT_SIZE,
                    LOGO_IMAGE_WIDTH, scaledHeight);
        }

        public void writeStatisticGroups(final AnnualStatisticsExportDTO dto,
                                         final List<AnnualStatisticGroup> groups) throws IOException {

            for (final AnnualStatisticGroup group : groups) {
                writeTitle(this.localiser.getTranslation(group));
                writeEmptyLine();

                for (final AnnualStatisticItem item : group.getItems()) {
                    writeStatisticItem(dto, item);
                }

                writeEmptyLine();
            }
        }

        private static List<String> splitItem(final String item, final int size) {
            final ArrayList<String> splitItem = new ArrayList<>();
            for (int i = 0; i < item.length(); i += size) {
                splitItem.add(item.substring(i, Math.min(item.length(), i + size)));
            }

            return splitItem;
        }

        private void writeStatisticItem(final AnnualStatisticsExportDTO dto,
                                        final AnnualStatisticItem item) throws IOException {

            this.activeFont = BASIC_FONT;

            final String title = replaceUnsupportedUnicodeChars(this.localiser.getTranslation(item));
            final float indentMultiplier = isIndented(item) ? 2f : 1f;
            final String value = item
                    .extractValue(dto)
                    .getOrElseGet(number -> number == null ? null : NUMBER_FORMATTER.format(number));

            final List<String> splitTitle = splitItem(title, MAX_TEXT_LENGTH);
            final int numTitleRows = splitTitle.size();
            for (int i = 0; i < numTitleRows; i++) {
                writeText(splitTitle.get(i), (MARGIN_TEXT_LEFT + indentMultiplier * INDENT_UNIT) * DOTS_PER_MM);
                if (i < numTitleRows - 1) {
                    writeEmptyLine();
                }
            }

            writeTextOnRight(value);
            writeEmptyLine();
        }

        public void writePageNumber(final int pageNumber) throws IOException {
            this.activeFont = BASIC_FONT;
            writeText(String.valueOf(pageNumber), this.center, MARGIN_BOTTOM);
        }

        private void writeTitle(final String title) throws IOException {
            this.activeFont = TITLE_FONT;
            writeText(replaceUnsupportedUnicodeChars(title), MARGIN_TEXT_LEFT * DOTS_PER_MM);
        }

        private void writeTextOnRight(String text) throws IOException {
            if (text != null) {
                if (text.length() > MAX_TEXT_LENGTH) {
                    // Truncate and append horizontal ellipsis character.
                    text = text.substring(0, MAX_TEXT_LENGTH) + '\u2026';
                }

                writeText(text, this.rightEdge - getTextWidth(text));
            }
        }

        private void writeText(final String text, final float tx) throws IOException {
            this.contentStream.setFont(this.activeFont, FONT_SIZE);
            writeText(text, tx, this.posY);
        }

        private void writeText(final String text, final float tx, final float ty) throws IOException {
            this.contentStream.beginText();
            this.contentStream.newLineAtOffset(tx, ty);
            this.contentStream.showText(text);
            this.contentStream.endText();
        }

        private void writeEmptyLine() {
            this.posY -= ROW_HEIGHT;
        }

        private float getTextWidth(final String text) throws IOException {
            return this.activeFont.getStringWidth(text) / 1000.0f * FONT_SIZE;
        }

        @Override
        public void close() throws IOException {
            this.contentStream.close();
        }
    }
}
