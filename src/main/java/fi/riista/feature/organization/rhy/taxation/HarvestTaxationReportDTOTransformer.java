package fi.riista.feature.organization.rhy.taxation;

import static java.util.stream.Collectors.toList;

import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.common.service.LastModifierService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import fi.riista.util.jpa.JpaGroupingUtils;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class HarvestTaxationReportDTOTransformer extends ListTransformer<HarvestTaxationReport, HarvestTaxationReportDTO> {

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private HarvestTaxationReportAttachmentRepository attachmentRepository;

    @Resource
    private GISHirvitalousalueRepository gisHirvitalousalueRepository;

    @Resource
    private LastModifierService lastModifierService;

    @Nonnull
    @Override
    protected List<HarvestTaxationReportDTO> transform(@Nonnull final List<HarvestTaxationReport> items) {

        // Fetch related entities only once
        final Function<HarvestTaxationReport, GameSpecies> speciesMapper = getGameSpeciesMapper(items);
        final Function<HarvestTaxationReport, Organisation> rhyMapper = getRhyMapper(items);
        final Function<HarvestTaxationReport, GISHirvitalousalue> htaMapper = getHtaMapper(items);
        final Map<HarvestTaxationReport, List<HarvestTaxationReportAttachment>> groupedAttachments = getAttachments(items);
        final Map<HarvestTaxationReport, LastModifierDTO> lastModifiers = lastModifierService.getLastModifiers(items);

        return items.stream()
                .map(item -> {
                    final List<HarvestTaxationReportAttachment> attachments = groupedAttachments.get(item);
                    return HarvestTaxationReportDTO.create(item,
                            speciesMapper.apply(item),
                            rhyMapper.apply(item),
                            htaMapper.apply(item),
                            attachments,
                            lastModifiers.getOrDefault(item.getId(), null));
                }).
                collect(toList());
    }

    protected HarvestTaxationReportDTO transform(@Nonnull final HarvestTaxationReport item) {
        final LastModifierDTO lastModifier = lastModifierService.getLastModifier(item);

        final List<HarvestTaxationReportAttachment> attachments = attachmentRepository.findAllByHarvestTaxationReport(item);

        return HarvestTaxationReportDTO.create(item,
                item.getSpecies(),
                item.getRhy(),
                item.getHta(),
                attachments,
                lastModifier);
    }

    private Function<HarvestTaxationReport, GameSpecies> getGameSpeciesMapper(final List<HarvestTaxationReport> items) {
        return CriteriaUtils.singleQueryFunction(items, HarvestTaxationReport::getSpecies, gameSpeciesRepository, true);
    }

    private Function<HarvestTaxationReport, GISHirvitalousalue> getHtaMapper(final List<HarvestTaxationReport> items) {
        return CriteriaUtils.singleQueryFunction(items, HarvestTaxationReport::getHta, gisHirvitalousalueRepository, true);
    }

    private Function<HarvestTaxationReport, Organisation> getRhyMapper(final List<HarvestTaxationReport> items) {
        return CriteriaUtils.singleQueryFunction(items, HarvestTaxationReport::getRhy, organisationRepository, true);
    }

    private Map<HarvestTaxationReport, List<HarvestTaxationReportAttachment>> getAttachments(final List<HarvestTaxationReport> items) {
        return JpaGroupingUtils.groupRelations(items, HarvestTaxationReportAttachment_.harvestTaxationReport, attachmentRepository);
    }
}
