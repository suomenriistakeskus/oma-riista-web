package fi.riista.common;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import java.util.Objects;

public abstract class AcroFormPdfBuilder {

    protected final PDAcroForm acroForm;

    public AcroFormPdfBuilder(final PDDocument pdfDocument) {
        this.acroForm = Objects.requireNonNull(pdfDocument.getDocumentCatalog().getAcroForm());

        // PDF box uses appearances and font during text field value setting so initialize
        // wanted resources beforehand.

        this.acroForm.setNeedAppearances(false);

        // Define font resources names used in PDF template
        final PDResources dr = new PDResources();
        dr.put(COSName.getPDFName("Helv"), PDType1Font.HELVETICA);
        dr.put(COSName.getPDFName("HeBo"), PDType1Font.HELVETICA_BOLD);
        this.acroForm.setDefaultResources(dr);
    }
}
