package fi.riista.feature.gamediary.excel;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationDTOTransformer;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fi.riista.feature.gamediary.GameDiarySpecs.authorOrObserver;
import static fi.riista.feature.gamediary.GameDiarySpecs.authorOrShooter;
import static fi.riista.feature.gamediary.GameDiarySpecs.temporalSort;

@Component
public class GameDiaryExcelFeature {

    @Resource
    private MessageSource messageSource;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private HarvestDTOTransformer harvestDtoTransformer;

    @Resource
    private ObservationDTOTransformer observationDtoTransformer;

    @Transactional(readOnly = true)
    public GameDiaryExcelView export() {
        final Person person = activeUserService.requireActivePerson();
        final Locale locale = LocaleContextHolder.getLocale();

        final Map<Integer, LocalisedString> species = gameSpeciesService.getNameIndex();

        final List<Harvest> harvests = harvestRepository.findAll(authorOrShooter(person), sort());
        final List<Observation> observations = observationRepository.findAll(authorOrObserver(person), sort());

        return new GameDiaryExcelView(
                new EnumLocaliser(messageSource, locale),
                species,
                // TODO Update to currently supported HarvestSpecVersion.
                harvestDtoTransformer.apply(harvests, HarvestSpecVersion._7),
                observationDtoTransformer.apply(observations));
    }

    private static JpaSort sort() {
        return temporalSort(Sort.Direction.DESC);
    }
}
