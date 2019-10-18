package fi.riista.feature.permit.decision.document;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationData;
import fi.riista.util.Locales;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.function.BiFunction;

public class PermitDecisionAmendmentApplicationSummaryGenerator {
    private PermitDecisionAmendmentApplicationSummaryGenerator() {
        throw new AssertionError();
    }

    public static String generate(final HarvestPermitApplication application,
                                  final AmendmentApplicationData data,
                                  final Locale locale,
                                  final MessageSource messageSource) {

        return generate(
                new PermitDecisionAmendmentApplicationSummaryModel(locale, application, data),
                new EnumLocaliser(messageSource, locale));
    }

    public static String generate(final PermitDecisionAmendmentApplicationSummaryModel model,
                                  final EnumLocaliser enumLocaliser) {

        final boolean swedish = Locales.isSwedish(model.getLocale());
        final BiFunction<String, String, String> i18n = (fi, sv) -> swedish ? sv : fi;

        final StringBuilder sb = new StringBuilder();
        sb.append(i18n.apply(
                "Hakija on hakenut hirvieläimen lupaa seuraavasti",
                "Den sökande har ansökt om jaktlicens för hjortdjur enligt följande"));
        sb.append(":\n\n");
        sb.append("---|---:\n");
        cell2(sb, i18n.apply("Alkuperäinen lupa", "Ursprunligt beslut"), model.getOriginalPermitNumber());
        cell2(sb, i18n.apply("Laji", "Art"), model.getSpeciesAmount().getName());
        cell2(sb, i18n.apply("Päivä ja aika", "Dag och tid"), model.getPointOfTime());

        cell2(sb, i18n.apply("Ikä ja sukupuoli", "Ålder och kön"),
                enumLocaliser.getTranslation(model.getAge()),
                enumLocaliser.getTranslation(model.getGender())
        );

        cell2(sb, i18n.apply("Ampuja", "Skytt"), model.getShooter());
        cell2(sb, i18n.apply("Osakas", "Samsöksdeltagare"), model.getPartner());

        cell2(sb, i18n.apply("Sijainti", "Plats"),
                'P', model.getGeoLocation().getLatitude(), 'I', model.getGeoLocation().getLongitude(), " (ETRS-TM35FIN)");

        cell2(sb, i18n.apply("Liitteet", "Bilagor"), "");
        cell2(sb, i18n.apply("Lausunnot", "Utlåtanden"), model.getOfficialStatements().size(), i18n.apply("kpl", "st."));
        cell2(sb, i18n.apply("Muut liitteet", "Övriga bilagor"), model.getOtherAttachments().size(), i18n.apply("kpl", "st."));

        if (model.getOfficialStatements().size() > 0) {
            cell2(sb, i18n.apply("Lausunnot", "Utlåtanden"), "&nbsp;");
            for (String s : model.getOfficialStatements()) {
                cell2(sb, "&nbsp;", s);
            }
        }

        if (model.getOtherAttachments().size() > 0) {
            cell2(sb, i18n.apply("Muut liitteet", "Övriga bilagor"), "&nbsp;");
            for (String s : model.getOtherAttachments()) {
                cell2(sb, "&nbsp;", s);
            }
        }
        sb.append("\n\n");

        return sb.toString();
    }

    private static void cell2(final StringBuilder sb, final String firstCell, final Object... secondCell) {
        sb.append(firstCell);
        sb.append('|');
        for (int i = 0; i < secondCell.length; i++) {
            sb.append(secondCell[i]);
            if (i < secondCell.length - 1) {
                sb.append(' ');
            }
        }
        sb.append('\n');
    }
}
