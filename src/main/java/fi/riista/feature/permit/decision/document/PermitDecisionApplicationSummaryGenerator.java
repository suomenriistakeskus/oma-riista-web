package fi.riista.feature.permit.decision.document;

import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.mail.HandlebarsHelperSource;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyDTO;
import fi.riista.util.Locales;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.BiFunction;

public class PermitDecisionApplicationSummaryGenerator {
    public static String generate(final Locale locale,
                                  final HarvestPermitApplication application,
                                  final GISZoneSizeDTO areaSize) {
        return generate(new PermitDecisionApplicationSummaryModel(locale, application, areaSize));
    }

    public static String generate(final PermitDecisionApplicationSummaryModel model) {
        final DecimalFormat df = new DecimalFormat("#", new DecimalFormatSymbols(Locales.FI));
        final boolean swedish = Locales.isSwedish(model.getLocale());
        final BiFunction<String, String, String> i18n = (fi, sv) -> swedish ? sv : fi;

        final StringBuilder sb = new StringBuilder();
        sb.append(i18n.apply(
                "Hakija on hakenut hirvieläinten pyyntilupaa seuraavasti",
                "Den sökande har ansökt om jaktlicens för hjortdjur enligt följande"));
        sb.append(":\n\n");
        sb.append(i18n.apply(
                "Eläinlaji ja lupamäärä:",
                "Djurart och licensantal:"));
        sb.append("\n\n");
        sb.append("---|---:\n");

        for (final PermitDecisionApplicationSummaryModel.SpeciesAmount speciesAmount : model.getSpeciesAmounts()) {
            sb.append(speciesAmount.getName());
            sb.append("|");
            sb.append(df.format(speciesAmount.getAmount()));
            sb.append(" ");
            sb.append(i18n.apply("kpl", "st."));
            sb.append("\n");
        }

        sb.append("\n");
        sb.append(i18n.apply("Alue:", "Område:"));
        sb.append("\n");
        sb.append(i18n.apply(
                "Hakemus sijaitsee seuraavien riistanhoitoyhdistysten alueilla:",
                "Ansökningsområdet är på följande jaktvårdsföreningars områden:"));
        sb.append("\n");

        for (final HarvestPermitAreaRhyDTO rhyDTO : model.getRhys()) {
            sb.append(rhyDTO.getRhy().getNameLocalisation().getTranslation(model.getLocale()));
            sb.append(", ");
            sb.append(i18n.apply(
                    "karttaan rajattu alue",
                    "på kartan avgränsat område"));
            sb.append("\n");
        }

        sb.append("\n");
        sb.append(i18n.apply("Alueen tunnus", "Områdets kod"));
        sb.append(" ");
        sb.append(model.getAreaExternalCode());
        sb.append("\n");

        sb.append(i18n.apply("Alueen pinta-ala:", "Areal:"));
        sb.append("\n\n");

        sb.append("---|---:\n");
        sb.append(i18n.apply("Maapinta-ala", "Markareal"));
        sb.append("|");
        sb.append(HandlebarsHelperSource.hectaresRounded(model.getLandAreaSize()));
        sb.append("\n");

        sb.append(i18n.apply("Vesipinta-ala", "Vattenareal"));
        sb.append("|");
        sb.append(HandlebarsHelperSource.hectaresRounded(model.getWaterAreaSize()));
        sb.append("\n");

        sb.append(i18n.apply("Kokonaispinta-ala", "Areal"));
        sb.append("|");
        sb.append(HandlebarsHelperSource.hectaresRounded(model.getTotalAreaSize()));
        sb.append("\n");

        sb.append(i18n.apply("Valtionmaiden maapinta-ala", "Statsägda områdens areal"));
        sb.append("|");
        sb.append(HandlebarsHelperSource.hectaresRounded(model.getStateLandAreaSize()));
        sb.append("\n");

        sb.append(i18n.apply("Yksityismaiden maapinta-ala", "Privatägda områdens areal"));
        sb.append("|");
        sb.append(HandlebarsHelperSource.hectaresRounded(model.getPrivateLandAreaSize()));
        sb.append("\n\n");

        if (model.getPartnerCount() > 0) {
            sb.append("---:|---\n");
            sb.append(i18n.apply(
                    "Lupaosakkaiden määrä,",
                    "Antal licensdelägare,"));
            sb.append("|");
            sb.append(model.getPartnerCount());
            sb.append(" ");
            sb.append(i18n.apply("kpl", "st."));
            sb.append("\n");

            sb.append(i18n.apply(
                    "asiakasnumerot ja nimet",
                    "kundnummer och namn"));
            sb.append("|\n");

            for (OrganisationNameDTO club : model.getPartners()) {
                sb.append(club.getOfficialCode());
                sb.append("|");
                sb.append(club.getNameFI());
                sb.append("\n");
            }
            sb.append("\n");
        }

        if (model.isFreeHunting()) {
            sb.append("---|---:\n");
            sb.append(i18n.apply(
                    "Ampujat, jotka eivät kuulu muuhun pyyntilupaa hakevaan seuraan / seurueeseen",
                    "Skyttar som inte hör till annan förening / annat sällskap som ansöker om jaktlicens"));
            sb.append("|");
            sb.append(model.getShooterOnlyClub());
            sb.append(" ");
            sb.append(i18n.apply("kpl", "st."));
            sb.append("\n");

            sb.append(i18n.apply(
                    "Ampujat, jotka kuuluvat muuhun hirveä metsästävään seuraan / seurueeseen, mutta eivät metsästä siellä tulevana metsästyskautena",
                    "Skyttar som hör till en annan förening / annat sällskap som jagar älg men som inte jagar där under den aktuella jaktsäsongen"));
            sb.append("|");
            sb.append(model.getShooterOtherClubPassive());
            sb.append(" ");
            sb.append(i18n.apply("kpl", "st."));
            sb.append("\n");

            sb.append(i18n.apply(
                    "Ampujat, jotka kuuluvat muuhun hirveä metsästävään seuraan / seurueeseen, ja metsästävät siellä tulevana metsästyskautena",
                    "Skyttar som hör till en annan förening / annat sällskap som jagar älg och som jagar där under den aktuella jaktsäsongen"));
            sb.append("|");
            sb.append(model.getShooterOtherClubActive());
            sb.append(" ");
            sb.append(i18n.apply("kpl", "st."));
            sb.append("\n\n");
        }

        sb.append("---|---:\n");

        if (model.isFreeHunting()) {
            sb.append(i18n.apply(
                    "Metsähallituksen aluelupia",
                    "Områdeslicens JL"));
            sb.append("|");
            sb.append(model.getAreaPermitCount());
            sb.append(" ");
            sb.append(i18n.apply("kpl", "st."));
            sb.append("\n");

            sb.append(i18n.apply(
                    "Ampujaluetteloita",
                    "Skytteförteckningar"));
            sb.append("|");
            sb.append(model.getShooterListCount());
            sb.append(" ");
            sb.append(i18n.apply("kpl", "st."));
            sb.append("\n");
        }

        sb.append(i18n.apply(
                "Muita liitteitä",
                "Övriga bilagor"));
        sb.append("|");
        sb.append(model.getOtherAttachmentCount());
        sb.append(" ");
        sb.append(i18n.apply("kpl", "st."));

        return sb.toString();
    }

    private PermitDecisionApplicationSummaryGenerator() {
        throw new AssertionError();
    }
}
