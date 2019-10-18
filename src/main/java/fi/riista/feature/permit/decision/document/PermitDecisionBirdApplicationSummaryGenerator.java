package fi.riista.feature.permit.decision.document;

import fi.riista.feature.permit.application.bird.BirdPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.bird.amount.BirdPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.bird.attachments.BirdPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.bird.damage.BirdPermitApplicationDamageDTO;
import fi.riista.feature.permit.application.bird.forbidden.BirdPermitApplicationForbiddenMethodsSpeciesDTO;
import fi.riista.feature.permit.application.bird.period.BirdPermitApplicationSpeciesPeriodDTO;
import fi.riista.feature.permit.application.bird.population.BirdPermitApplicationSpeciesPopulationDTO;
import fi.riista.util.Locales;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PermitDecisionBirdApplicationSummaryGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("d.M.");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#", new DecimalFormatSymbols(Locales.FI));

    private final BirdPermitApplicationSummaryDTO model;
    private final Locale locale;
    private final boolean swedish;
    private final MessageSource messageSource;
    private final Map<Integer, String> speciesNameIndex;

    public PermitDecisionBirdApplicationSummaryGenerator(final BirdPermitApplicationSummaryDTO model,
                                                         final Locale locale,
                                                         final Map<Integer, String> speciesNameIndex,
                                                         final MessageSource messageSource) {
        this.model = Objects.requireNonNull(model);
        this.locale = Objects.requireNonNull(locale);
        this.swedish = Locales.isSwedish(locale);
        this.messageSource = Objects.requireNonNull(messageSource);
        this.speciesNameIndex = Objects.requireNonNull(speciesNameIndex);
    }

    private String speciesName(final int gameSpeciesCode) {
        return speciesNameIndex.get(gameSpeciesCode);
    }

    private String i18n(final String fi, final String sv) {
        return swedish ? sv : fi;
    }

    private String i18nKey(final String key) {
        return messageSource.getMessage(key, null, locale);
    }

    public String generateApplicationMain() {
        final StringBuilder sb = new StringBuilder();

        createHeader(sb);
        sb.append("\n\n");

        if (model.getValidityYears() > 0) {
            sb.append(i18nKey("bird.application.period.limited"));
            sb.append(' ');
            sb.append(model.getValidityYears());
            sb.append(' ');
            sb.append(i18nKey("bird.application.period.years"));
            sb.append("\n\n");
        } else {
            sb.append(i18nKey("bird.application.period.limitless"));
            sb.append("\n\n");
        }

        createAmounts(sb);
        createPeriods(sb);
        createProtectedArea(sb);
        createCause(sb);

        if (model.getForbiddenMethods().isForbiddenMethodSelected()) {
            createForbiddenMethods(sb);
        }

        createAttachments(sb);

        return sb.toString();
    }

    public String generateApplicationReasoning() {
        final StringBuilder sb = new StringBuilder();

        if (model.getForbiddenMethods().isForbiddenMethodSelected()) {
            createForbiddenMethodJustification(sb);
        }

        createDamage(sb);
        createPopulation(sb);

        return sb.toString();
    }

    private void createHeader(final StringBuilder sb) {
        final Integer validityYears = model.getValidityYears();

        if (validityYears != null && validityYears > 0) {
            sb.append(i18n(
                    "Hakija on hakenut poikkeuslupaa seuraavasti:",
                    "Sökande har ansökt om dispenslicens enligt följande:"));
        } else {
            sb.append(i18n(
                    "Hakija on hakenut ilmoitusmenettelyyn seuraavasti:",
                    "Sökande har ansökt om anmälningsförfarandet enligt följande:"));
        }
    }

    private void createAmounts(final StringBuilder sb) {
        sb.append("\n\n");
        sb.append(i18n(
                "Eläinlaji ja lupamäärä:",
                "Djurart och licensantal:"));
        sb.append("\n\n");
        sb.append("---|---:\n");

        for (final BirdPermitApplicationSpeciesAmountDTO speciesAmount : model.getSpeciesAmounts()) {
            sb.append(speciesName(speciesAmount.getGameSpeciesCode()));
            sb.append('|');
            sb.append(DECIMAL_FORMAT.format(speciesAmount.getAmount()));
            sb.append(' ');
            sb.append(i18n("kpl", "st."));
            sb.append('\n');
        }
    }

    private void createPeriods(final StringBuilder sb) {
        sb.append("\n\n");
        sb.append(i18n(
                "Eläinlaji ja haettu lupa-aika:",
                "Djurart och ansökta giltighetstiden:"));
        sb.append("\n\n");
        sb.append("---|---:\n");

        for (final BirdPermitApplicationSpeciesPeriodDTO speciesPeriod : model.getSpeciesPeriods()) {
            sb.append(speciesName(speciesPeriod.getGameSpeciesCode()));
            sb.append('|');
            sb.append(DATE_FORMAT.print(speciesPeriod.getBeginDate()));
            sb.append(" - ");
            sb.append(DATE_FORMAT.print(speciesPeriod.getEndDate()));
            sb.append('\n');
        }
    }

    private void createProtectedArea(final StringBuilder sb) {
        sb.append("\n\n");
        sb.append(i18n("Hakemusalue:", "Ansökningsområde:"));
        sb.append("\n\n");
        sb.append("---|---:\n");
        cell2(sb, i18nKey("bird.application.area.name"), model.getProtectedArea().getName());
        cell2(sb, i18nKey("bird.application.area.address"), model.getProtectedArea().getStreetAddress());
        cell2(sb, i18nKey("bird.application.area.postalcode"), model.getProtectedArea().getPostalCode());
        cell2(sb, i18nKey("bird.application.area.city"), model.getProtectedArea().getCity());
        cell2(sb, i18nKey("bird.application.area.size"), "" + model.getProtectedArea().getProtectedAreSize() + " ha");
        sb.append("\n\n");

        sb.append(i18nKey("bird.application.area.type"));
        sb.append(":\n");
        sb.append(i18nKey("bird.application.area.type." + model.getProtectedArea().getProtectedAreaType()));
        sb.append("\n\n");

        sb.append(i18nKey("bird.application.area.map"));
        sb.append(":\n");

        for (final BirdPermitApplicationAttachmentDTO s : model.getAreaAttachments()) {
            sb.append("\\- ");
            sb.append(s.getName());
            sb.append("\n");
        }

        sb.append("\n\n");
        sb.append(i18nKey("bird.application.area.rights"));
        sb.append(":\n");
        sb.append(model.getProtectedArea().getDescriptionOfRights());
        sb.append("\n\n");
    }

    private void createCause(final StringBuilder sb) {
        sb.append(i18n("Poikkeusperusteet", "Grund för dispens"));
        sb.append(":\n");

        if (model.getPermitCause().isCausePublicHealth()) {
            createCauseItem(sb, "health");
        }
        if (model.getPermitCause().isCausePublicSafety()) {
            createCauseItem(sb, "safety");
        }
        if (model.getPermitCause().isCauseAviationSafety()) {
            createCauseItem(sb, "aviation");
        }
        if (model.getPermitCause().isCauseCropsDamage()) {
            createCauseItem(sb, "crops");
        }
        if (model.getPermitCause().isCauseDomesticPets()) {
            createCauseItem(sb, "domestic");
        }
        if (model.getPermitCause().isCauseForestDamage()) {
            createCauseItem(sb, "forest");
        }
        if (model.getPermitCause().isCauseFishing()) {
            createCauseItem(sb, "fish");
        }
        if (model.getPermitCause().isCauseWaterSystem()) {
            createCauseItem(sb, "water");
        }
        if (model.getPermitCause().isCauseFlora()) {
            createCauseItem(sb, "flora");
        }
        if (model.getPermitCause().isCauseFauna()) {
            createCauseItem(sb, "fauna");
        }
        if (model.getPermitCause().isCauseResearch()) {
            createCauseItem(sb, "research");
        }

        sb.append("\n");
    }

    private void createCauseItem(final StringBuilder sb, final String key) {
        sb.append("\\- ");
        sb.append(i18nKey("bird.application.cause." + key));
        sb.append("\n");
    }

    private void createForbiddenMethods(final StringBuilder sb) {
        sb.append(i18n("Kielletyt menetelmät", "Förbjudna fångsmetoder"));
        sb.append(":\n\n");

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection32())) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.32"));
            sb.append(": ");
            sb.append("\n");
            sb.append(model.getForbiddenMethods().getDeviateSection32());
            sb.append("\n");
        }

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection33()) || model.getForbiddenMethods().isTapeRecorders()) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.33"));
            sb.append(": ");
            sb.append("\n");

            if (model.getForbiddenMethods().isTapeRecorders()) {
                sb.append(i18nKey("bird.application.forbidden.tape"));
                sb.append("\n");
            }

            if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection33())) {
                sb.append(model.getForbiddenMethods().getDeviateSection33());
                sb.append("\n");
            }
        }

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection34()) || model.getForbiddenMethods().isTraps()) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.34"));
            sb.append(": ");
            sb.append("\n");

            if (model.getForbiddenMethods().isTraps()) {
                sb.append(i18nKey("bird.application.forbidden.traps"));
                sb.append("\n");
            }

            if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection34())) {
                sb.append(model.getForbiddenMethods().getDeviateSection34());
                sb.append("\n");
            }
        }

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection35())) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.35"));
            sb.append(": ");
            sb.append("\n");
            sb.append(model.getForbiddenMethods().getDeviateSection35());
            sb.append("\n");
        }

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection51())) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.51"));
            sb.append(": ");
            sb.append("\n");
            sb.append(model.getForbiddenMethods().getDeviateSection51());
            sb.append("\n");
        }

        sb.append("\n");
    }

    private void createForbiddenMethodJustification(final StringBuilder sb) {
        sb.append("\n");
        sb.append(i18nKey("bird.application.forbidden.justification"));
        sb.append(":\n");

        for (final BirdPermitApplicationForbiddenMethodsSpeciesDTO dto : model.getForbiddenMethods().getSpeciesJustifications()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");

            if (dto.isActive()) {
                sb.append(dto.getJustification());
            } else {
                sb.append(i18nKey("bird.application.forbidden.not.applicable"));
            }

            sb.append('\n');
        }

        sb.append("\n");
    }

    private void createDamage(final StringBuilder sb) {
        sb.append(i18nKey("bird.application.damage.title"));
        sb.append("\n\n");

        sb.append(i18nKey("bird.application.damage.amount"));
        sb.append(":\n");

        for (BirdPermitApplicationDamageDTO dto : model.getDamage()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");
            sb.append(dto.getCausedDamageAmount());
            sb.append(" &euro;\n");
        }

        sb.append("\n");
        sb.append(i18nKey("bird.application.damage.description"));
        sb.append(":\n");

        for (BirdPermitApplicationDamageDTO dto : model.getDamage()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");
            sb.append(dto.getCausedDamageDescription());
            sb.append("\n");
        }

        sb.append("\n");
        sb.append(i18nKey("bird.application.damage.eviction.method"));
        sb.append(":\n");

        for (BirdPermitApplicationDamageDTO dto : model.getDamage()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");
            sb.append(dto.getEvictionMeasureDescription());
            sb.append("\n");
        }

        sb.append("\n");
        sb.append(i18nKey("bird.application.damage.eviction.effects"));
        sb.append(":\n");

        for (BirdPermitApplicationDamageDTO dto : model.getDamage()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");
            sb.append(dto.getEvictionMeasureEffect());
            sb.append("\n");
        }

        sb.append("\n");
    }

    private void createPopulation(final StringBuilder sb) {
        sb.append(i18nKey("bird.application.population.title"));
        sb.append("\n\n");

        sb.append(i18nKey("bird.application.population.amount"));
        sb.append(":\n");

        for (BirdPermitApplicationSpeciesPopulationDTO dto : model.getPopulation()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");
            sb.append(dto.getPopulationAmount());
            sb.append("\n");
        }

        sb.append("\n");
        sb.append(i18nKey("bird.application.population.description"));
        sb.append(":\n");

        for (BirdPermitApplicationSpeciesPopulationDTO dto : model.getPopulation()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");
            sb.append(dto.getPopulationDescription());
            sb.append("\n");
        }

        sb.append("\n");
    }

    private void createAttachments(final StringBuilder sb) {
        if (model.getOtherAttachments().size() > 0) {
            sb.append(i18n("Liitteet", "Bilagor"));
            sb.append("\n");

            for (BirdPermitApplicationAttachmentDTO s : model.getOtherAttachments()) {
                sb.append("\\- ");
                sb.append(s.getName());
                sb.append("\n");

                if (StringUtils.hasText(s.getAdditionalInfo())) {
                    sb.append(s.getAdditionalInfo());
                    sb.append("\n");
                }
            }

            sb.append("\n\n");
        }
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
