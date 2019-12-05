package fi.riista.feature.permit.decision.document;

import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.derogation.damage.DerogationPermitApplicationDamageDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsSpeciesDTO;
import fi.riista.feature.permit.application.derogation.population.DerogationPermitApplicationSpeciesPopulationDTO;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonsDTO;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.mammal.amount.MammalPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.mammal.period.MammalPermitApplicationSpeciesPeriodDTO;
import fi.riista.feature.permit.decision.derogation.DerogationLawSection;
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

import static org.apache.commons.lang.StringUtils.isNotBlank;


public class PermitDecisionMammalApplicationSummaryGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("d.M.YYYY");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#", new DecimalFormatSymbols(Locales.FI));

    private final MammalPermitApplicationSummaryDTO model;
    private final Locale locale;
    private final boolean swedish;
    private final MessageSource messageSource;
    private final Map<Integer, String> speciesNameIndex;

    public PermitDecisionMammalApplicationSummaryGenerator(final MammalPermitApplicationSummaryDTO model,
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

        createAmounts(sb);
        createPeriods(sb);
        createArea(sb);
        createReasons(sb);

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
        sb.append("---|---:\n");

        for (final MammalPermitApplicationSpeciesAmountDTO speciesAmount : model.getSpeciesAmounts()) {
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

        for (final MammalPermitApplicationSpeciesPeriodDTO speciesPeriod : model.getSpeciesPeriods()) {
            sb.append(speciesName(speciesPeriod.getGameSpeciesCode()));
            sb.append('|');
            sb.append(DATE_FORMAT.print(speciesPeriod.getBeginDate()));
            sb.append(" - ");
            sb.append(DATE_FORMAT.print(speciesPeriod.getEndDate()));
            sb.append('\n');
        }

        sb.append(i18nKey("mammal.application.reason.extendedPeriodGrounds"));
        sb.append("\n");
        sb.append(i18nKey("mammal.application.reason.extendedPeriodGrounds." + model.getExtendedPeriodGrounds()));
        sb.append("\n");


        sb.append(i18nKey("mammal.application.reason.protectedAreaName"));
        sb.append("\n");
        sb.append(model.getProtectedAreaName());
        sb.append("\n");

        if (isNotBlank(model.getExtendedPeriodDescription())) {
            sb.append(i18nKey("mammal.application.reason.extendedPeriodGroundsDescription"));
            sb.append("\n");
            sb.append(i18nKey(model.getExtendedPeriodDescription()));
            sb.append("\n");
        }
    }

    private void createArea(final StringBuilder sb) {
        final DerogationPermitApplicationAreaDTO areaDTO =
                model.getDerogationPermitApplicationAreaDTO();

        sb.append("\n\n");
        sb.append(i18n("Hakemusalue:", "Ansökningsområde:"));
        sb.append("\n\n");
        sb.append("---|---:\n");
        cell2(sb, i18nKey("carnivore.application.area.size"), "" + areaDTO.getAreaSize() + " ha");
        final String areaDescription = areaDTO.getAreaDescription();
        if (isNotBlank(areaDescription)) {
            cell2(sb, i18nKey("carnivore.application.area.areaDescription"), areaDescription);
        }
        sb.append("\n\n");

        for (final DerogationPermitApplicationAttachmentDTO s : model.getAreaAttachments()) {
            sb.append("\\- ");
            sb.append(s.getName());
            sb.append("\n");
        }

        sb.append("\n\n");

    }

    private void createReasons(final StringBuilder sb) {
        final DerogationPermitApplicationReasonsDTO reasons = model.getReasons();

        sb.append(i18n("Poikkeusperusteet", "Grund för dispens"));
        sb.append(":\n");

        reasons.getReasons().forEach(lawSectionReason -> {
            sb.append("\n\n");
            // species
            sb.append(lawSectionReason.getSpeciesCodes());
            sb.append("\n");
            // law section
            if (lawSectionReason.getLawSection() == DerogationLawSection.SECTION_41A) {
                sb.append(i18nKey("mammal.application.reason.lawSection41a"));
                sb.append("\n");
            } else if (lawSectionReason.getLawSection() == DerogationLawSection.SECTION_41C) {
                sb.append(i18nKey("mammal.application.reason.lawSection41c"));
                sb.append("\n");
            } else {
                throw new IllegalArgumentException("Illegal law section for mammal application:" + lawSectionReason.getLawSection());
            }
            // reasons
            lawSectionReason.getLawSectionReasons().forEach(r -> {
                if (r.isChecked()) {
                    sb.append("\\-").append(i18nKey("mammal.application.reason." + r.getReasonType()));
                    sb.append("\n");
                }
            });
        });

        sb.append("\n\n");

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

        for (final DerogationPermitApplicationForbiddenMethodsSpeciesDTO dto :
                model.getForbiddenMethods().getSpeciesJustifications()) {
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

        for (DerogationPermitApplicationDamageDTO dto : model.getDamage()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");
            sb.append(dto.getCausedDamageAmount());
            sb.append(" &euro;\n");
        }

        sb.append("\n");
        sb.append(i18nKey("bird.application.damage.description"));
        sb.append(":\n");

        for (DerogationPermitApplicationDamageDTO dto : model.getDamage()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");
            sb.append(dto.getCausedDamageDescription());
            sb.append("\n");
        }

        sb.append("\n");
        sb.append(i18nKey("bird.application.damage.eviction.method"));
        sb.append(":\n");

        for (DerogationPermitApplicationDamageDTO dto : model.getDamage()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");
            sb.append(dto.getEvictionMeasureDescription());
            sb.append("\n");
        }

        sb.append("\n");
        sb.append(i18nKey("bird.application.damage.eviction.effects"));
        sb.append(":\n");

        for (DerogationPermitApplicationDamageDTO dto : model.getDamage()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");
            sb.append(dto.getEvictionMeasureEffect());
            sb.append("\n");
        }

        sb.append("\n");
    }

    private void createPopulation(final StringBuilder sb) {
        sb.append(i18nKey("mammal.application.population.populationInfo"));
        sb.append("\n\n");

        sb.append(i18nKey("mammal.application.population.populationAmount"));
        sb.append(":\n");

        for (DerogationPermitApplicationSpeciesPopulationDTO dto : model.getPopulation()) {
            sb.append("\\- ");
            sb.append(speciesName(dto.getGameSpeciesCode()));
            sb.append(": ");
            sb.append(dto.getPopulationAmount());
            sb.append("\n");
        }

        sb.append("\n");
        sb.append(i18nKey("mammal.application.population.populationDescription"));
        sb.append(":\n");

        for (DerogationPermitApplicationSpeciesPopulationDTO dto : model.getPopulation()) {
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

            for (DerogationPermitApplicationAttachmentDTO s : model.getOtherAttachments()) {
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
