package fi.riista.feature.huntingclub.copy;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.util.Locales;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class CopyClubGroupService {

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private MessageSource messageSource;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HuntingClubGroup copyGroup(final HuntingClubGroup originalGroup,
                                      final HuntingClubArea huntingArea) {
        final HuntingClubGroup group = new HuntingClubGroup();
        group.setParentOrganisation(originalGroup.getParentOrganisation());
        group.setSpecies(originalGroup.getSpecies());

        final boolean useSuffix = originalGroup.getHuntingYear() == huntingArea.getHuntingYear();
        group.setNameFinnish(originalGroup.getNameFinnish() + (useSuffix ? suffix(Locales.FI) : ""));
        group.setNameSwedish(originalGroup.getNameSwedish() + (useSuffix ? suffix(Locales.SV) : ""));

        group.setHuntingArea(huntingArea);
        group.setHuntingYear(huntingArea.getHuntingYear());

        huntingClubGroupRepository.save(group);

        final List<Occupation> newOccupations =
                occupationRepository.findNotDeletedByOrganisation(originalGroup).stream()
                        .filter(o -> !o.getPerson().isDeceased())
                        .map(o -> {
                            final Occupation newOcc = new Occupation(o.getPerson(),
                                    group,
                                    o.getOccupationType(),
                                    o.getContactInfoShare(),
                                    o.getCallOrder());
                            newOcc.setEmailVisibility(o.isEmailVisibility());
                            newOcc.setNameVisibility(o.isNameVisibility());
                            newOcc.setPhoneNumberVisibility(o.isPhoneNumberVisibility());
                            return newOcc;
                        })
                        .collect(toList());
        occupationRepository.saveAll(newOccupations);
        return group;
    }

    private String suffix(final Locale locale) {
        return " " + messageSource.getMessage("copy.suffix.caps", null, locale);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void copyGroupsHavingArea(final HuntingClubArea originalArea, final HuntingClubArea area) {
        final QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;
        final BooleanExpression predicate = GROUP.huntingArea.eq(originalArea).and(GROUP.fromMooseDataCard.eq(false));

        try (final Stream<HuntingClubGroup> stream = huntingClubGroupRepository.findAllAsStream(predicate)) {
            stream.forEach(g -> copyGroup(g, area));
        }
    }
}
