package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventDTOTransformerBase;
import fi.riista.feature.gamediary.srva.SrvaEventSpecVersion;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.feature.organization.person.Person;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Component
public class MobileSrvaEventDTOTransformer extends SrvaEventDTOTransformerBase<MobileSrvaEventDTO> {

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nullable
    public List<MobileSrvaEventDTO> apply(@Nullable final List<SrvaEvent> list,
                                          @Nonnull final SrvaEventSpecVersion specVersion) {

        return list == null ? null : transform(list, specVersion);
    }

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nullable
    public MobileSrvaEventDTO apply(@Nullable final SrvaEvent srvaEvent,
                                    @Nonnull final SrvaEventSpecVersion specVersion) {

        if (srvaEvent == null) {
            return null;
        }

        final List<MobileSrvaEventDTO> singletonList = apply(Collections.singletonList(srvaEvent), specVersion);

        if (singletonList.size() != 1) {
            throw new IllegalStateException(
                    "Expected list containing exactly one srva event but has: " + singletonList.size());
        }

        return singletonList.get(0);
    }

    @Override
    protected List<MobileSrvaEventDTO> transform(@Nonnull final List<SrvaEvent> list) {
        throw new UnsupportedOperationException("No transformation without srvaEventSpecVersion supported");
    }

    @Nonnull
    private List<MobileSrvaEventDTO> transform(@Nonnull final List<SrvaEvent> srvaEvents,
                                               @Nonnull final SrvaEventSpecVersion srvaEventSpecVersion) {

        Objects.requireNonNull(srvaEvents, "srvaEvents cannot be null");
        Objects.requireNonNull(srvaEventSpecVersion, "srvaEventSpecVersion must not be null");

        final Function<SrvaEvent, Person> srvaEventToAuthor = getSrvaEventToAuthorMapping(srvaEvents);
        final Function<SrvaEvent, GameSpecies> srvaEventToSpecies = getSrvaEventToSpeciesMapping(srvaEvents);
        final Map<SrvaEvent, List<SrvaSpecimen>> groupedSpecimens = getSpecimensGroupedBySrvaEvent(srvaEvents);
        final Map<SrvaEvent, List<SrvaMethod>> groupedMethods = getMethodsGroupedBySrvaEvent(srvaEvents);
        final Map<SrvaEvent, List<GameDiaryImage>> groupedImages = getImagesGroupedBySrvaEvent(srvaEvents);
        final Function<SrvaEvent, SystemUser> srvaEventToApproverAsUser = getSrvaEventToApproverAsUserMapping(srvaEvents);
        final Function<SrvaEvent, Person> srvaEventToApproverAsPerson = getSrvaEventToApproverAsPersonMapping(srvaEvents);

        return srvaEvents.stream().filter(Objects::nonNull).map(srvaEvent -> {
            final MobileSrvaEventDTO dto = MobileSrvaEventDTO.create(srvaEvent, srvaEventSpecVersion);

            dto.setCanEdit(!Objects.equals(srvaEvent.getState(), SrvaEventStateEnum.APPROVED));

            setCommonFields(
                    dto,
                    srvaEventToAuthor.apply(srvaEvent),
                    srvaEventToSpecies.apply(srvaEvent),
                    groupedSpecimens.get(srvaEvent),
                    groupedMethods.get(srvaEvent),
                    groupedImages.get(srvaEvent),
                    srvaEventToApproverAsUser.apply(srvaEvent),
                    srvaEventToApproverAsPerson.apply(srvaEvent));

            return dto;
        }).collect(toList());
    }
}
