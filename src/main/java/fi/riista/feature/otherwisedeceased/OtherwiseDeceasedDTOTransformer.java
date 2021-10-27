package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.util.F;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;

@Component
public class OtherwiseDeceasedDTOTransformer {

    @Resource
    private OtherwiseDeceasedAttachmentDTOTransformer attachmentDTOTransformer;

    @Resource
    private OtherwiseDeceasedAttachmentRepository attachmentRepository;

    @Resource
    private OtherwiseDeceasedChangeDTOTransformer changeDTOTransformer;

    @Resource
    private OtherwiseDeceasedChangeRepository changeRepository;

    @Resource
    private MunicipalityRepository municipalityRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    public OtherwiseDeceasedDTO createDTO(@Nonnull final OtherwiseDeceased entity) {
        requireNonNull(entity);

        final List<OtherwiseDeceasedAttachmentDTO> attachmentDTOs =
                attachmentDTOTransformer.transform(attachmentRepository.findAllByOtherwiseDeceased(entity));
        final List<OtherwiseDeceasedChangeDTO> changeDTOs =
                changeDTOTransformer.transform(changeRepository.findAllByOtherwiseDeceasedOrderByPointOfTime(entity));

        final OtherwiseDeceasedDTO dto = OtherwiseDeceasedDTO.create(entity, attachmentDTOs, changeDTOs);
        return dto;
    }

    public List<OtherwiseDeceasedDTO> transform(@Nonnull final List<OtherwiseDeceased> entities) {
        requireNonNull(entities);

        final Function<OtherwiseDeceased, GameSpecies> speciesMapper = getSpeciesMapper(entities);
        final Function<OtherwiseDeceased, Municipality> municipalityMapper = getMunicipalityMapper(entities);
        final Function<OtherwiseDeceased, Organisation> rhyMapper = getRhyMapper(entities);
        final Function<OtherwiseDeceased, Organisation> rkaMapper = getRkaMapper(entities);
        final Map<Long, List<OtherwiseDeceasedChangeDTO>> changeMap = changeDTOTransformer.transform(getChangeMap(entities));
        final Map<Long, List<OtherwiseDeceasedAttachmentDTO>> attachmentMap = attachmentDTOTransformer.transform(getAttachmentMap(entities));

        final List<OtherwiseDeceasedDTO> dtos = new ArrayList<>();
        entities.forEach(entity -> dtos.add(OtherwiseDeceasedDTO.create(entity,
                                                                        speciesMapper.apply(entity),
                                                                        municipalityMapper.apply(entity),
                                                                        rhyMapper.apply(entity),
                                                                        rkaMapper.apply(entity),
                                                                        attachmentMap.getOrDefault(entity.getId(), emptyList()),
                                                                        changeMap.get(entity.getId()))));
        return dtos;
    }

    private Function<OtherwiseDeceased, GameSpecies> getSpeciesMapper(final List<OtherwiseDeceased> items) {
        return CriteriaUtils.singleQueryFunction(items, OtherwiseDeceased::getSpecies, gameSpeciesRepository, true);
    }

    private Function<OtherwiseDeceased, Municipality> getMunicipalityMapper(final List<OtherwiseDeceased> items) {
        return CriteriaUtils.singleQueryFunction(items, OtherwiseDeceased::getMunicipality, municipalityRepository, true);
    }

    private Function<OtherwiseDeceased, Organisation> getRhyMapper(final List<OtherwiseDeceased> items) {
        return CriteriaUtils.singleQueryFunction(items, OtherwiseDeceased::getRhy, organisationRepository, true);
    }

    private Function<OtherwiseDeceased, Organisation> getRkaMapper(final List<OtherwiseDeceased> items) {
        return CriteriaUtils.singleQueryFunction(items, OtherwiseDeceased::getRka, organisationRepository, true);
    }

    private Map<Long, List<OtherwiseDeceasedChange>> getChangeMap(final List<OtherwiseDeceased> entities) {
        return F.sortedById(changeRepository.findAll(inCollection(OtherwiseDeceasedChange_.otherwiseDeceased, entities)))
                .stream()
                .collect(groupingBy(e -> e.getOtherwiseDeceased().getId()));
    }

    private Map<Long, List<OtherwiseDeceasedAttachment>> getAttachmentMap(final List<OtherwiseDeceased> entities) {
        return F.sortedById(attachmentRepository.findAll(inCollection(OtherwiseDeceasedAttachment_.otherwiseDeceased, entities)))
                .stream()
                .collect(groupingBy(e -> e.getOtherwiseDeceased().getId()));
    }
}
