package fi.riista.feature.otherwisedeceased;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
public class OtherwiseDeceasedExcelFeature {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private MessageSource messageSource;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private OtherwiseDeceasedDTOTransformer dtoTransformer;

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('MUUTOIN_KUOLLEET')")
    @Transactional(readOnly = true)
    public View exportToExcel(final OtherwiseDeceasedFilterDTO filterDTO, final Locale locale) {
        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);
        final List<OtherwiseDeceased> entities = OtherwiseDeceasedSearchQueryBuilder.create(jpqlQueryFactory).withFilter(filterDTO).list();
        final Map<Integer, LocalisedString> speciesNameMap = getSpeciesCodeToNameMap(entities);
        final List<OtherwiseDeceasedDTO> data = dtoTransformer.transform(entities);

        return new OtherwiseDeceasedExcelView(localiser, data, speciesNameMap);
    }

    private Map<Integer, LocalisedString> getSpeciesCodeToNameMap(final List<OtherwiseDeceased> entities) {
        final Set<Long> speciesIds = entities.stream()
                .map(OtherwiseDeceased::getSpecies)
                .map(GameSpecies::getId)
                .collect(toSet());
        final List<GameSpecies> species = gameSpeciesRepository.findAllById(speciesIds);
        return species.stream().collect(toMap(GameSpecies::getOfficialCode, GameSpecies::getNameLocalisation));
    }
}
