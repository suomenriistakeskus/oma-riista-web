package fi.riista.feature.harvestpermit.report.paper;

import fi.riista.util.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class MammalHarvestReportPdfBuilder {

    static byte[] getPdf(final PermitHarvestReportModel model,
                         final PermitHarvestReportI18n i18n) throws IOException {
        try (final PDDocument pdDocument = new PDDocument()) {
            final MammalHarvestReportPdfBuilder builder = new MammalHarvestReportPdfBuilder(pdDocument, i18n);

            for (PermitHarvestReportModel.SpeciesAndPermitNumber speciesAndPermitNumber : model.getSpeciesList()) {
                builder.addSpeciesPage(model, speciesAndPermitNumber);
            }

            return builder.build();
        }
    }

    private final PDDocument pdfDocument;
    private final PermitHarvestReportI18n i18n;

    public MammalHarvestReportPdfBuilder(final PDDocument pdfDocument,
                                         final PermitHarvestReportI18n i18n) {
        this.pdfDocument = requireNonNull(pdfDocument);
        this.i18n = requireNonNull(i18n);
    }

    private String i18n(final String finnish, final String swedish) {
        return i18n.getLocalisedString(finnish, swedish);
    }

    public MammalHarvestReportPdfBuilder addSpeciesPage(final PermitHarvestReportModel model,
                                                        final PermitHarvestReportModel.SpeciesAndPermitNumber speciesAndPermitNumber) throws IOException {
        final PDPage pdfPage = new PDPage(PDRectangle.A4);
        pdfDocument.addPage(pdfPage);

        try (final PdfWriter writer = new PdfWriter(pdfDocument, pdfPage)) {
            writer.riistaLogo(10, 180, 20);

            // Sender

            writer.topOffsetMm(13).marginLeftMm(20)
                    .font(PDType1Font.TIMES_ITALIC, 10, 12)
                    .writeLine("Lähettäjä / Avsändare")
                    .font(PDType1Font.TIMES_ROMAN, 11, 12);

            for (final String line : model.getRecipientAddress()) {
                writer.writeLine(line);
            }

            // Recipient

            writer.topOffsetMm(40).marginLeftMm(20)
                    .font(PDType1Font.TIMES_ITALIC, 10, 12)
                    .writeLine("Vastaanottaja / Mottagare")
                    .font(PDType1Font.TIMES_BOLD, 11, 12);

            for (String line : model.getSenderAddress()) {
                writer.writeLine(line);
            }
            writer.font(PDType1Font.TIMES_ROMAN, 11, 12)
                    .writeLine(model.getSenderPhoneNumber());

            // Permit number

            writer.topOffsetMm(45).marginLeftMm(150)
                    .font(PDType1Font.TIMES_ITALIC, 14, 18)
                    .writeLine(i18n("Vuosikohtainen viite:", "Årsvis referens:"))
                    .font(PDType1Font.HELVETICA_BOLD, 14, 18)
                    .writeLine(speciesAndPermitNumber.getPermitNumber());

            // Title

            writer.topOffsetMm(72).marginLeftMm(20)
                    .font(PDType1Font.TIMES_BOLD, 14, 13);


            writer.writeParagraph(i18n(
                    "ILMOITUS POIKKEUSLUVALLA TAPAHTUNEEN PYYNNIN TULOKSESTA",
                    "ANMÄLAN OM RESULTAT FÖR FÅNGST SOM SKETT MED STÖD AV DISPENS"));


            writer.writeEmptyLine()
                    .font(PDType1Font.TIMES_ROMAN, 12)
                    .writeParagraph(i18n("Ilmoituksen voi tehdä myös sähköisesti Oma riista -palvelun kautta (Ohje " +
                            "Oma riista -palvelun käyttöön löytyy osoitteesta: http://riista" +
                            ".fi/riistahallinto/sahkoinen-asiointi/).", "Anmälan kan även göras elektroniskt via " +
                            "tjänsten Oma riista (Anvisning för användning av tjänsten Oma riista på adressen: " +
                            "https://riista.fi/sv/viltforvaltningen/elektroniska-tjanster/)."))
                    .writeEmptyLine()

                    .writeParagraph(i18n("Allekirjoittaneelle myönnetyllä poikkeusluvalla on saatu" +
                                    " saalista seuraavasti:",
                            "Med stöd av till undertecknad beviljad dispens har följande byte fällts:"))
                    .writeEmptyLine()

                    .font(PDType1Font.TIMES_BOLD, 14)
                    .writeLine(i18n.getGameSpeciesName(speciesAndPermitNumber.getGameSpeciesCode()))
                    .writeEmptyLine()

                    .font(PDType1Font.TIMES_ROMAN, 12)
                    .writeLine(i18n(
                            "sukupuoli  ____ uros ____ naaras",
                            "kön  ____ handjur ____ hondjur"))
                    .writeEmptyLine()

                    .font(PDType1Font.TIMES_ROMAN, 12)
                    .writeEmptyLine()
                    .writeEmptyLine()

                    .writeLine(i18n(
                            "Pyyntipaikan koordinaatit:               P ______________________      I " +
                                    "______________________",
                            "Fångstplatsens koordinater:             P ______________________      I " +
                                    "______________________"))
                    .writeLine("                                                         " +
                            i18n("ETRS-TM35FIN -tasokoordinaatisto", "ETRS-TM35FIN -plankoordinatsystem"))
                    .writeEmptyLine()
                    .writeEmptyLine()

                    .writeLine("______.______._________                                      ________________________")
                    .writeLine(i18n(
                            "Pyyntipäivä                                                               Pyyntiaika" +
                                    " (HH:mm)",
                            "Datum                                                                       Tidpunkt för" +
                                    " fångsten (HH:mm)"))
                    .writeEmptyLine()

                    .writeLine("__________________________________              ____________________________")
                    .writeLine(i18n("Metsästäjän nimi                                                       Ampujan " +
                                    "metsästäjänumero",
                            "Jägarens namn                                                          Jägarens " +
                                    "jägarnummer"))
                    .writeEmptyLine()

                    .writeLine("__________________________________________________")
                    .writeLine(i18n("Ilmoittajan puhelinnumero tai sähköpostiosoite", "Anmälarens telefonnummer eller" +
                            " e-postadress"));

            writer.topOffsetMm(233).marginLeftMm(95)
                    .font(PDType1Font.TIMES_ROMAN, 12)
                    .writeLine("________________________     ______.______._________")
                    .writeLine(i18n(
                            "Paikka                                          Aika",
                            "Plats                                             Tid"))
                    .writeEmptyLine()

                    .writeLine("_________________________________________________")
                    .writeLine(i18n("Allekirjoitus", "Underskrift"));

            writer.topOffsetMm(260).marginLeftMm(20)
                    .font(PDType1Font.TIMES_BOLD, 12, 11)
                    .writeLine(i18n("Ohje:", "Anvisning"))
                    .font(PDType1Font.TIMES_ROMAN, 12, 11)
                    .writeParagraph(i18n(
                            "Tämä ilmoitus on tehtävä poikkeusluvan ehdot -kohdassa ilmeneville tahoille annettujen " +
                                    "määräaikojen mukaisesti.",
                            "Det här anmälan måste göras i enlighet med den givna tidsfristen till de aktörer som " +
                                    "framkommer under punkten villkor i dispensen."))
                    .writeEmptyLine();
        }

        return this;
    }

    public byte[] build() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        pdfDocument.save(bos);
        pdfDocument.close();
        return bos.toByteArray();
    }

}
