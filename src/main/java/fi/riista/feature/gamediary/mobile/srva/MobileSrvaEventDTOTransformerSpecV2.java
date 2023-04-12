package fi.riista.feature.gamediary.mobile.srva;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventResultDetailsEnum;
import fi.riista.feature.gamediary.srva.SrvaEventSpecVersion;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.gamediary.srva.SrvaEventTypeDetailsEnum;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.feature.organization.person.Person;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.toList;

@Service
public class MobileSrvaEventDTOTransformerSpecV2 extends MobileSrvaEventDTOTransformerBase {

    @Override
    protected List<MobileSrvaEventDTO> transform(List<SrvaEvent> srvaEvents, SrvaEventSpecVersion srvaEventSpecVersion) {
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

            setSpevV2Fields(
                    dto,
                    srvaEvent.getDeportationOrderNumber(),
                    srvaEvent.getEventTypeDetail(),
                    srvaEvent.getOtherEventTypeDetailDescription(),
                    srvaEvent.getEventResultDetail());
            return dto;
        }).collect(toList());
    }

    private void setSpevV2Fields(
            final MobileSrvaEventDTO dto,
            final String deportationOrderNumber,
            final SrvaEventTypeDetailsEnum eventTypeDetail,
            final String otherEventTypeDetailDescription,
            final SrvaEventResultDetailsEnum eventResultDetail) {

        dto.setDeportationOrderNumber(deportationOrderNumber);
        dto.setEventTypeDetail(eventTypeDetail);
        dto.setOtherEventTypeDetailDescription(otherEventTypeDetailDescription);
        dto.setEventResultDetail(eventResultDetail);
    }
}
