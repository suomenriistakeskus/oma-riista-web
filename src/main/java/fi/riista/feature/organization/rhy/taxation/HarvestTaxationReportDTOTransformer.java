package fi.riista.feature.organization.rhy.taxation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import fi.riista.util.jpa.JpaGroupingUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
public class HarvestTaxationReportDTOTransformer extends ListTransformer<HarvestTaxationReport, HarvestTaxationReportDTO> {

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private HarvestTaxationReportAttachmentRepository attachmentRepository;

    @Resource
    private GISHirvitalousalueRepository gisHirvitalousalueRepository;

    @Nonnull
    @Override
    protected List<HarvestTaxationReportDTO> transform(@Nonnull final List<HarvestTaxationReport> items) {

        // Fetch related entities only once
        final Function<HarvestTaxationReport, GameSpecies> speciesMapper = getGameSpeciesMapper(items);
        final Function<HarvestTaxationReport, Organisation> rhyMapper = getRhyMapper(items);
        final Function<HarvestTaxationReport, GISHirvitalousalue> htaMapper = getHtaMapper(items);
        final Map<HarvestTaxationReport, List<HarvestTaxationReportAttachment>> groupedAttachments = getAttachments(items);
        final Map<Long, SystemUser> moderatorMapper = getModeratorMapper(items);

        return items.stream()
                .map(item -> {
                    final List<HarvestTaxationReportAttachment> attachments = groupedAttachments.get(item);
                    return HarvestTaxationReportDTO.create(item,
                            speciesMapper.apply(item),
                            rhyMapper.apply(item),
                            htaMapper.apply(item),
                            attachments,
                            moderatorMapper.getOrDefault(item.getId(), null));
                }).
                collect(toList());
    }

    protected HarvestTaxationReportDTO transform(@Nonnull final HarvestTaxationReport item) {
        SystemUser moderator = null;
        if (item.getModifiedByUserId() != null && item.getModifiedByUserId() > 0) {
            moderator = userRepository.getOne(item.getModifiedByUserId());
        }


        final List<HarvestTaxationReportAttachment> attachments = attachmentRepository.findAllByHarvestTaxationReport(item);

        return HarvestTaxationReportDTO.create(item,
                item.getSpecies(),
                item.getRhy(),
                item.getHta(),
                attachments,
                moderator);
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

    private Map<Long, SystemUser> getModeratorMapper(final List<HarvestTaxationReport> items) {
        final Set<Long> moderatorIds = items
                .stream()
                .map(HarvestTaxationReport::getModifiedByUserId)
                .filter(Objects::nonNull).collect(toSet());
        return F.indexById(userRepository.findAllById(moderatorIds));
    }
}
