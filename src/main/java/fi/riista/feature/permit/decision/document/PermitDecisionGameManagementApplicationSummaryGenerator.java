package fi.riista.feature.permit.decision.document;

import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsSpeciesDTO;
import fi.riista.feature.permit.application.gamemanagement.amount.GameManagementSpeciesAmountDTO;
import fi.riista.feature.permit.application.gamemanagement.period.GameManagementSpeciesPeriodDTO;
import fi.riista.feature.permit.application.gamemanagement.summary.GameManagementSummaryDTO;
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

import static fi.riista.feature.permit.decision.document.PermitDecisionTextUtils.escape;
import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class PermitDecisionGameManagementApplicationSummaryGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#", new DecimalFormatSymbols(Locales.FI));

    private final GameManagementSummaryDTO model;
    private final Locale locale;
    private final boolean swedish;
    private final MessageSource messageSource;
    private final Map<Integer, String> speciesNameIndex;

    public PermitDecisionGameManagementApplicationSummaryGenerator(final GameManagementSummaryDTO model,
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

        sb.append(i18nKey("bird.application.period.limited"));
        sb.append(' ');
        sb.append(model.getSpeciesPeriods().get(0).getValidityYears());
        sb.append(' ');
        sb.append(i18nKey("bird.application.period.years"));
        sb.append("\n\n");

        createAmounts(sb);
        createPeriods(sb);
        createArea(sb);
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

        if (StringUtils.hasText(model.getJustification())) {
            sb.append(i18nKey("research.application.reasoning.heading.justification"))
                    .append("\n\n");

            sb.append(escape(model.getJustification()));
        }

        return sb.toString();
    }

    private void createHeader(final StringBuilder sb) {
        sb.append(i18n(
                "Hakija on hakenut poikkeuslupaa seuraavasti:",
                "Sökande har ansökt om dispenslicens enligt följande:"));
    }

    private void createAmounts(final StringBuilder sb) {
        sb.append("\n\n");
        sb.append(i18n(
                "Eläinlaji ja lupamäärä:",
                "Djurart och licensantal:"));
        sb.append("\n\n");
        sb.append("---|---|---:\n");

        for (final GameManagementSpeciesAmountDTO speciesAmount : model.getSpeciesAmounts()) {
            final Integer specimenAmount = speciesAmount.getSpecimenAmount();
            final Integer eggAmount = speciesAmount.getEggAmount();

            sb.append(speciesName(speciesAmount.getGameSpeciesCode()));
            if (StringUtils.hasText(speciesAmount.getSubSpeciesName())){
                sb.append(String.format(" (%s)", escape(speciesAmount.getSubSpeciesName())));
            }
            sb.append("|");
            if (specimenAmount != null) {
                sb.append(DECIMAL_FORMAT.format(specimenAmount));
                sb.append(' ');
                sb.append(i18nKey("gamemanagement.application.specimen"));
            }
            sb.append("|");
            if (eggAmount != null) {
                sb.append(DECIMAL_FORMAT.format(eggAmount));
                sb.append(' ');
                sb.append(i18nKey("gamemanagement.application.egg"));
            }
            sb.append("\n");
        }

        sb.append("\n\n");
    }


    private void createPeriods(final StringBuilder sb) {
        sb.append("\n\n")
                .append(i18n(
                        "Eläinlaji ja haettu lupa-aika:",
                        "Djurart och ansökta giltighetstiden:"))
                .append("\n\n");

        sb.append("---|---:\n");
        for (final GameManagementSpeciesPeriodDTO speciesPeriod : model.getSpeciesPeriods()) {
            cell2(sb, speciesName(speciesPeriod.getGameSpeciesCode()),
                    DATE_FORMAT.print(speciesPeriod.getBeginDate()),
                    "-",
                    DATE_FORMAT.print(speciesPeriod.getEndDate()));
        }

        sb.append("\n\n");
    }

    private void createArea(final StringBuilder sb) {
        final DerogationPermitApplicationAreaDTO areaDTO =
                model.getDerogationPermitApplicationAreaDTO();

        sb.append("\n\n")
                .append(i18n("Hakemusalue:", "Ansökningsområde:"))
                .append("\n\n");

        sb.append("---|---:\n");
        final String areaDescription = areaDTO.getAreaDescription();
        if (isNotBlank(areaDescription)) {
            cell2(sb, i18nKey("carnivore.application.area.areaDescription"), escape(areaDescription));
        }
        sb.append("\n\n");

        for (final DerogationPermitApplicationAttachmentDTO s : model.getAreaAttachments()) {
            sb.append("\\- ")
                    .append(s.getName())
                    .append("\n");
        }

        sb.append("\n\n");

    }

    private void createAttachments(final StringBuilder sb) {
        if (model.getOtherAttachments().size() > 0) {
            sb.append(i18n("Liitteet", "Bilagor"));
            sb.append("\n");

            for (DerogationPermitApplicationAttachmentDTO s : model.getOtherAttachments()) {
                sb.append("\\- ");
                sb.append(s.getName());
                sb.append("\n");

                if (StringUtils.hasText(s.getAdditionalInfo())) {
                    sb.append(escape(s.getAdditionalInfo()))
                            .append("\n");
                }
            }

            sb.append("\n\n");
        }
    }

    private void createForbiddenMethods(final StringBuilder sb) {
        sb.append(i18n("Kielletyt menetelmät", "Förbjudna fångsmetoder"));
        sb.append(":\n\n");

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection32())) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.32"));
            sb.append(": ");
            sb.append("\n");
            sb.append(escape(model.getForbiddenMethods().getDeviateSection32()));
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
                sb.append(escape(model.getForbiddenMethods().getDeviateSection33()));
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
                sb.append(escape(model.getForbiddenMethods().getDeviateSection34()));
                sb.append("\n");
            }
        }

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection35())) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.35"));
            sb.append(": ");
            sb.append("\n");
            sb.append(escape(model.getForbiddenMethods().getDeviateSection35()));
            sb.append("\n");
        }

        if (StringUtils.hasText(model.getForbiddenMethods().getDeviateSection51())) {
            sb.append("\\- ");
            sb.append(i18nKey("bird.application.forbidden.51"));
            sb.append(": ");
            sb.append("\n");
            sb.append(escape(model.getForbiddenMethods().getDeviateSection51()));
            sb.append("\n");
        }

        sb.append("\n");
    }

    private void createForbiddenMethodJustification(final StringBuilder sb) {
        sb.append("\n");
        sb.append(i18nKey("bird.application.forbidden.justification"));
        sb.append(":\n");

        for (final DerogationPermitApplicationForbiddenMethodsSpeciesDTO dto :
                model.getForbiddenMethods().getSpeciesJustifications()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");

            if (dto.isActive()) {
                sb.append(escape(dto.getJustification()));
            } else {
                sb.append(i18nKey("bird.application.forbidden.not.applicable"));
            }

            sb.append('\n');
        }

        sb.append("\n");
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
