package fi.riista.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.Code128Constants;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.core.io.ClassPathResource;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class PdfWriter implements Closeable {
    // PDF basic unit is 72 dpi.
    private static final float DOTS_PER_MM = 72f / 25.4f;
    private final PDPageContentStream contentStream;

    private final PDPage page;
    private final PDDocument document;
    private PDFont activeFont;
    private float fontSize;
    private float lineHeight;
    private float posX;
    private float posY;

    public PdfWriter(final PDDocument document, final PDPage page) throws IOException {
        this.document = Objects.requireNonNull(document);
        this.page = Objects.requireNonNull(page);
        this.contentStream = new PDPageContentStream(document, page,
                PDPageContentStream.AppendMode.APPEND, false, true);
        this.posY = page.getMediaBox().getUpperRightY();
        this.posX = 0;
        normalFont();
    }

    public PdfWriter marginLeftMm(float value) {
        this.posX = value * DOTS_PER_MM;
        return this;
    }

    public PdfWriter marginLeftRelative(int percents) {
        this.posX = page.getMediaBox().getUpperRightX() * percents / 100f;
        return this;
    }

    public PdfWriter topOffsetMm(float value) {
        this.posY = page.getMediaBox().getUpperRightY() - value * DOTS_PER_MM;
        return this;
    }

    public PdfWriter addVerticalSpaceMm(float value) {
        this.posY -= value * DOTS_PER_MM;
        return this;
    }

    public PdfWriter writeEmptyLine() {
        this.posY -= lineHeight;
        return this;
    }

    public PdfWriter normalFont() {
        this.activeFont = PDType1Font.TIMES_ROMAN;
        this.fontSize = 12f;
        this.lineHeight = 14f;
        return this;
    }

    public PdfWriter font(final PDType1Font font, final float fontSize, final float lineHeight) {
        this.activeFont = font;
        this.fontSize = fontSize;
        this.lineHeight = lineHeight;
        return this;
    }

    public PdfWriter font(final PDType1Font font, final float fontSize) {
        this.activeFont = font;
        this.fontSize = fontSize;
        //this.lineHeight = 7f * fontSize / 6f;
        this.lineHeight = activeFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
        return this;
    }

    public PdfWriter boldFont() {
        this.activeFont = PDType1Font.TIMES_BOLD;
        this.fontSize = 12f;
        this.lineHeight = 14f;
        return this;
    }

    public PdfWriter italicFont() {
        this.activeFont = PDType1Font.TIMES_ITALIC;
        this.fontSize = 10f;
        this.lineHeight = 14f;
        return this;
    }

    public PdfWriter smallFont() {
        this.activeFont = PDType1Font.TIMES_ITALIC;
        this.fontSize = 9f;
        this.lineHeight = 14f;
        return this;
    }

    public PdfWriter writeLine(final String text) throws IOException {
        writeText(text, this.posX, this.posY);
        this.posY -= this.lineHeight;
        return this;
    }

    public PdfWriter writeParagraph(final String text) throws IOException {
        return writeParagraph(text, 100.0f);
    }

    public PdfWriter writeParagraph(final String text, final float rightMarginPercentage) throws IOException {
        final float upperRightX = page.getMediaBox().getUpperRightX() * rightMarginPercentage / 100;
        final float paragraphWidth = upperRightX - this.posX - 20;
        int start = 0;
        int end = 0;

        this.contentStream.setFont(this.activeFont, this.fontSize);

        for (final int i : possibleWrapPoints(text)) {
            final float width = activeFont.getStringWidth(text.substring(start, i)) / 1000 * fontSize;

            if (start < end && width > paragraphWidth) {
                this.contentStream.beginText();
                this.contentStream.newLineAtOffset(this.posX, this.posY);
                this.contentStream.showText(text.substring(start, end));
                this.contentStream.endText();
                this.writeEmptyLine();

                start = end;
            }

            end = i;
        }

        // Last piece of text
        this.contentStream.beginText();
        this.contentStream.newLineAtOffset(this.posX, this.posY);
        this.contentStream.showText(text.substring(start));
        this.contentStream.endText();
        this.writeEmptyLine();

        return this;
    }

    private static int[] possibleWrapPoints(final String text) {
        final String[] split = text.split("(?<=[^\\p{L}\\p{Nd}])");
        final int[] ret = new int[split.length];

        ret[0] = split[0].length();

        for (int i = 1; i < split.length; i++) {
            ret[i] = ret[i - 1] + split[i].length();
        }

        return ret;
    }

    private PdfWriter writeText(final String text, final float tx, final float ty) throws IOException {
        this.contentStream.setFont(this.activeFont, this.fontSize);
        this.contentStream.beginText();
        this.contentStream.newLineAtOffset(tx, ty);
        this.contentStream.showText(text);
        this.contentStream.endText();
        return this;
    }

    public PdfWriter drawBox(final float top, final float left, final float widthMm, final float heightMm) throws IOException {
        this.contentStream.setLineWidth(1.5f);
        this.contentStream.setStrokingColor(Color.BLACK);
        this.contentStream.addRect(left * DOTS_PER_MM, page.getMediaBox().getUpperRightY() - top * DOTS_PER_MM,
                widthMm * DOTS_PER_MM, heightMm * DOTS_PER_MM);
        this.contentStream.closeAndStroke();
        return this;
    }

    public void barCode(final String barCodeMessage) throws IOException {
        final float scale = 100f / 300;
        final Code128Bean code128Bean = new Code128Bean();
        code128Bean.setCodeset(Code128Constants.CODESET_C);
        code128Bean.setMsgPosition(HumanReadablePlacement.HRP_NONE);
        code128Bean.setBarHeight(scale * 20.0f);

        final BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                300, BufferedImage.TYPE_BYTE_BINARY, false, 0);
        code128Bean.generateBarcode(canvas, barCodeMessage);
        canvas.finish();

        final PDImageXObject pdImage = LosslessFactory.createFromImage(document, canvas.getBufferedImage());

        contentStream.drawImage(pdImage, 75, 80, scale * pdImage.getWidth(), scale * pdImage.getHeight());
    }

    private static final ClassPathResource LOGO_IMAGE_RESOURCE = new ClassPathResource("/riistafi-logo.png");

    public void riistaLogo(final float topMm, final float leftMm, final float widthMm) throws IOException {
        image(LOGO_IMAGE_RESOURCE.getFile(), topMm, leftMm, widthMm);
    }

    public void image(final File imageFile, final float topMm, final float leftMm, final float widthMm) throws IOException {
        final PDImageXObject imageObj = PDImageXObject.createFromFileByContent(imageFile, document);
        final float scaledWidth = DOTS_PER_MM * widthMm;
        final float scale = scaledWidth / imageObj.getWidth();
        final float scaledHeight = scale * imageObj.getHeight();

        contentStream.drawImage(imageObj, leftMm * DOTS_PER_MM,
                page.getMediaBox().getUpperRightY() - topMm * DOTS_PER_MM - scaledHeight,
                scaledWidth, scaledHeight);
    }

    @Override
    public void close() throws IOException {
        this.contentStream.close();
    }
}
