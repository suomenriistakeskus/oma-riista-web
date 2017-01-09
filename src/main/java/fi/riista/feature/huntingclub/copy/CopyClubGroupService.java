package fi.riista.feature.huntingclub.copy;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.Locales;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Service
public class CopyClubGroupService {

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private MessageSource messageSource;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HuntingClubGroup copy(Long originalGroupId, HuntingClubGroupCopyDTO dto) {
        final HuntingClubGroup originalGroup = requireEntityService.requireHuntingGroup(originalGroupId, EntityPermission.CREATE);
        final HuntingClubArea huntingArea = requireEntityService.requireHuntingClubArea(dto.getHuntingAreaId(), EntityPermission.READ);

        Preconditions.checkState(Objects.equals(huntingArea.getHuntingYear(), dto.getHuntingYear()), "hunting area year must match with selected year");
        return copyGroup(originalGroup, huntingArea);
    }

    private HuntingClubGroup copyGroup(final HuntingClubGroup originalGroup,
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

        final List<Occupation> newOccupations = occupationRepository.findNotDeletedByOrganisation(originalGroup).stream()
                .map(o -> new Occupation(o.getPerson(), group, o.getOccupationType(), o.getContactInfoShare(), o.getCallOrder()))
                .collect(toList());
        occupationRepository.save(newOccupations);
        return group;
    }

    private String suffix(Locale locale) {
        return " " + messageSource.getMessage("copy.suffix.caps", null, locale);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void copyGroupsHavingArea(HuntingClubArea originalArea, HuntingClubArea area) {
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        huntingClubGroupRepository.findAllAsStream(group.huntingArea.eq(originalArea).and(group.fromMooseDataCard.eq(false)))
                .forEach(g -> copyGroup(g, area));
    }
}
