package fi.riista.feature.permit.application.derogation.reasons;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.decision.derogation.DerogationLawSection;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonDTO;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Component
public class DerogationPermitApplicationReasonService {
    @Resource
    private DerogationPermitApplicationReasonRepository derogationPermitApplicationReasonRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public DerogationPermitApplicationReasonsDTO getDerogationReasons(final HarvestPermitApplication application,
                                                                      final Locale locale) {

        Preconditions.checkArgument(application.getHarvestPermitCategory() == HarvestPermitCategory.MAMMAL);


        final List<DerogationPermitApplicationReason> reasons =
                derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);


        final Map<DerogationLawSection, List<Integer>> groupedSpecies =
                harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application).stream()
                        .map(spa -> spa.getGameSpecies().getOfficialCode())
                        .collect(groupingBy(DerogationLawSection::getSpeciesLawSection));

        final List<DerogationPermitApplicationLawSectionReasonsDTO> lawSectionReasonsDTOS =
                groupedSpecies.entrySet().stream()
                        .map(e -> {
                            final DerogationLawSection lawSection = e.getKey();
                            final List<Integer> codes = e.getValue();
                            final HashSet<PermitDecisionDerogationReasonType> reasonTypes =
                                    F.mapNonNullsToSet(reasons,
                                                       DerogationPermitApplicationReason::getReasonType);
                            return DerogationPermitApplicationLawSectionReasonsDTO.of(
                                    lawSection,
                                    mapSpeciesNamesFrom(codes, locale),
                                    PermitDecisionDerogationReasonDTO.toDTOs(reasonTypes, lawSection));
                        })
                        .sorted(comparing(DerogationPermitApplicationLawSectionReasonsDTO::getLawSection))
                        .collect(toList());

        return DerogationPermitApplicationReasonsDTO.of(lawSectionReasonsDTOS);

    }

    private String mapSpeciesNamesFrom(final List<Integer> codes, final Locale locale) {
        return gameSpeciesRepository.findAllByOfficialCodeIn(codes).stream()
                .map(s -> s.getNameLocalisation().getAnyTranslation(locale).toUpperCase())
                .sorted()
                .collect(Collectors.joining(", "));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateDerogationReasons(final HarvestPermitApplication application,
                                        DerogationPermitApplicationReasonsDTO dto) {
        Preconditions.checkArgument(application.getHarvestPermitCategory() == HarvestPermitCategory.MAMMAL);

        final List<DerogationPermitApplicationReason> reasons =
                derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);


        final Set<PermitDecisionDerogationReasonType> newValues =
                getCheckedValuesFrom(dto);
        final Set<PermitDecisionDerogationReasonType> oldValues =
                F.mapNonNullsToSet(reasons, DerogationPermitApplicationReason::getReasonType);

        final Sets.SetView<PermitDecisionDerogationReasonType> addedValues = Sets.difference(newValues, oldValues);
        final Sets.SetView<PermitDecisionDerogationReasonType> removedValues = Sets.difference(oldValues, newValues);

        final List<DerogationPermitApplicationReason> toAdd = addedValues.stream()
                .map(type -> new DerogationPermitApplicationReason(application, type))
                .collect(toList());
        final List<DerogationPermitApplicationReason> toDelete = reasons.stream()
                .filter(r -> removedValues.contains(r.getReasonType())).collect(toList());

        derogationPermitApplicationReasonRepository.save(toAdd);
        derogationPermitApplicationReasonRepository.delete(toDelete);

    }

    private static Set<PermitDecisionDerogationReasonType> getCheckedValuesFrom(DerogationPermitApplicationReasonsDTO dto) {
        if (dto == null) {
            return null;
        }

        final List<PermitDecisionDerogationReasonDTO> reasonDTOS =
                dto.getReasons().stream().flatMap(sectionDTO -> sectionDTO.getLawSectionReasons().stream()).collect(toList());
        return reasonDTOS.stream()
                .filter(PermitDecisionDerogationReasonDTO::isChecked)
                .map(PermitDecisionDerogationReasonDTO::getReasonType)
                .collect(Collectors.toSet());
    }
}
