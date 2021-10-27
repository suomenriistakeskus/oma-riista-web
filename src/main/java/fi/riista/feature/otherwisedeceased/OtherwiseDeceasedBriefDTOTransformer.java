package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Component
public class OtherwiseDeceasedBriefDTOTransformer extends ListTransformer<OtherwiseDeceased, OtherwiseDeceasedBriefDTO> {

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private MunicipalityRepository municipalityRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Nonnull
    @Override
    protected List<OtherwiseDeceasedBriefDTO> transform(@Nonnull final List<OtherwiseDeceased> items) {

        // Fetch related entities only once
        final Function<OtherwiseDeceased, GameSpecies> speciesMapper = getGameSpeciesMapper(items);
        final Function<OtherwiseDeceased, Municipality> municipalityMapper = getMunicipalityMapper(items);
        final Function<OtherwiseDeceased, Organisation> rhyMapper = getRhyMapper(items);
        final Function<OtherwiseDeceased, Organisation> rkaMapper = getRkaMapper(items);

        return items.stream()
                .map(item -> OtherwiseDeceasedBriefDTO.create(item,
                                                              speciesMapper.apply(item),
                                                              municipalityMapper.apply(item),
                                                              rhyMapper.apply(item),
                                                              rkaMapper.apply(item)))
                .collect(toList());
    }

    private Function<OtherwiseDeceased, GameSpecies> getGameSpeciesMapper(final List<OtherwiseDeceased> items) {
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
}
