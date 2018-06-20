package fi.riista.feature.gamediary.srva;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.Occupation_;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static fi.riista.feature.organization.occupation.OccupationType.SRVA_YHTEYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.util.Collect.idList;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static fi.riista.util.jpa.JpaSpecs.notSoftDeleted;
import static fi.riista.util.jpa.JpaSpecs.withinInterval;
import static java.util.stream.Collectors.toList;

@Component
public class SrvaEventDTOTransformer extends SrvaEventDTOTransformerBase<SrvaEventDTO> {

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Nonnull
    @Override
    protected List<SrvaEventDTO> transform(@Nonnull final List<SrvaEvent> srvaEvents) {
        final Function<SrvaEvent, Person> srvaEventToAuthor = getSrvaEventToAuthorMapping(srvaEvents);
        final Function<SrvaEvent, GameSpecies> srvaEventToSpecies = getSrvaEventToSpeciesMapping(srvaEvents);
        final Map<SrvaEvent, List<SrvaSpecimen>> groupedSpecimens = getSpecimensGroupedBySrvaEvent(srvaEvents);
        final Map<SrvaEvent, List<SrvaMethod>> groupedMethods = getMethodsGroupedBySrvaEvent(srvaEvents);
        final Map<SrvaEvent, List<GameDiaryImage>> groupedImages = getImagesGroupedBySrvaEvent(srvaEvents);
        final Function<SrvaEvent, SystemUser> srvaEventToApproverAsUser = getSrvaEventToApproverAsUserMapping(srvaEvents);
        final Function<SrvaEvent, Person> srvaEventToApproverAsPerson = getSrvaEventToApproverAsPersonMapping(srvaEvents);

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final boolean isModeratorOrAdmin = activeUser.isModeratorOrAdmin();
        final Person activePerson = activeUser.getPerson();

        //List containing all rhyIds where activePerson is SRVA contact person or coordinator
        final List<Long> srvaRoleRhyIds = getSrvaRoleRhyIds(activePerson);

        return srvaEvents.stream().filter(Objects::nonNull).map(srvaEvent -> {
            final SrvaEventDTO dto = SrvaEventDTO.create(srvaEvent);

            final Person author = srvaEventToAuthor.apply(srvaEvent);
            setCommonFields(
                    dto,
                    author,
                    srvaEventToSpecies.apply(srvaEvent),
                    groupedSpecimens.get(srvaEvent),
                    groupedMethods.get(srvaEvent),
                    groupedImages.get(srvaEvent),
                    srvaEventToApproverAsUser.apply(srvaEvent),
                    srvaEventToApproverAsPerson.apply(srvaEvent));

            dto.setCanEdit(resolveCanEdit(
                    author,
                    activePerson,
                    isModeratorOrAdmin,
                    srvaRoleRhyIds,
                    srvaEvent.getRhy().getId(),
                    srvaEvent.getState()));

            return dto;
        }).collect(toList());
    }

    private static boolean resolveCanEdit(final Person author,
                                          final Person activeUser,
                                          final boolean isModeratorOrAdmin,
                                          final List<Long> srvaRoleRhyIds,
                                          final Long rhyId,
                                          final SrvaEventStateEnum state) {
        return state != SrvaEventStateEnum.APPROVED && (
                isModeratorOrAdmin
                        || srvaRoleRhyIds.contains(rhyId)
                        || Objects.equals(author.getId(), activeUser.getId())
        );
    }

    private List<Long> getSrvaRoleRhyIds(final Person activePerson) {
        if (activePerson == null) {
            return Collections.emptyList();
        }

        final Specifications<Occupation> specs = Specifications
                .where(equal(Occupation_.person, activePerson))
                .and(inCollection(Occupation_.occupationType, EnumSet.of(TOIMINNANOHJAAJA, SRVA_YHTEYSHENKILO)))
                .and(withinInterval(Occupation_.beginDate, Occupation_.endDate, DateUtil.today()))
                .and(notSoftDeleted());

        return occupationRepository.findAll(specs).stream()
                .map(Occupation::getOrganisation)
                .collect(idList());
    }
}
