package fi.riista.feature.gamediary.srva;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.image.GameDiaryImageRepository;
import fi.riista.feature.gamediary.srva.method.SrvaMethod;
import fi.riista.feature.gamediary.srva.method.SrvaMethodDTO;
import fi.riista.feature.gamediary.srva.method.SrvaMethodRepository;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimen;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenDTO;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.util.F;
import fi.riista.util.Functions;
import fi.riista.util.ListTransformer;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class SrvaEventDTOTransformerBase<DTO extends SrvaEventDTOBase>
        extends ListTransformer<SrvaEvent, DTO> {

    @Resource
    private GameSpeciesRepository gameSpeciesRepo;

    @Resource
    private PersonRepository personRepo;

    @Resource
    private SrvaSpecimenRepository srvaSpecimenRepo;

    @Resource
    private SrvaMethodRepository srvaMethodRepo;

    @Resource
    private GameDiaryImageRepository gameDiaryImageRepo;

    @Resource
    private UserRepository userRepo;

    @Nonnull
    protected Function<SrvaEvent, GameSpecies> getSrvaEventToSpeciesMapping(final Iterable<SrvaEvent> srvaEvents) {
        return SrvaJpaUtils.getSrvaEventToSpeciesMapping(srvaEvents, gameSpeciesRepo);
    }

    @Nonnull
    protected Function<SrvaEvent, Person> getSrvaEventToAuthorMapping(final Iterable<SrvaEvent> srvaEvents) {
        return SrvaJpaUtils.getSrvaEventToAuthorMapping(srvaEvents, personRepo);
    }

    @Nonnull
    protected Map<SrvaEvent, List<SrvaSpecimen>> getSpecimensGroupedBySrvaEvent(List<SrvaEvent> srvaEvents) {
        return SrvaJpaUtils.getSpecimensGroupedBySrvaEvent(srvaEvents, srvaSpecimenRepo);
    }

    @Nonnull
    protected Map<SrvaEvent, List<SrvaMethod>> getMethodsGroupedBySrvaEvent(List<SrvaEvent> srvaEvents) {
        return SrvaJpaUtils.getMethodsGroupedBySrvaEvent(srvaEvents, srvaMethodRepo);
    }

    @Nonnull
    protected Map<SrvaEvent, List<GameDiaryImage>> getImagesGroupedBySrvaEvent(final Collection<SrvaEvent> srvaEvents) {
        return SrvaJpaUtils.getImagesGroupedBySrvaEvent(srvaEvents, gameDiaryImageRepo);
    }

    @Nonnull
    protected Function<SrvaEvent, SystemUser> getSrvaEventToApproverAsUserMapping(final Iterable<SrvaEvent> srvaEvents) {
        return SrvaJpaUtils.getSrvaEventToApproverAsUserMapping(srvaEvents, userRepo);
    }

    @Nonnull
    protected Function<SrvaEvent, Person> getSrvaEventToApproverAsPersonMapping(final Iterable<SrvaEvent> srvaEvents) {
        return SrvaJpaUtils.getSrvaEventToApproverAsPersonMapping(srvaEvents, personRepo);
    }

    protected void setCommonFields(final DTO dto,
                                   final Person author,
                                   final GameSpecies species,
                                   final List<SrvaSpecimen> specimens,
                                   final List<SrvaMethod> methods,
                                   final List<GameDiaryImage> images,
                                   final SystemUser approverAsUser,
                                   final Person approverAsPerson) {

        if (author != null) {
            dto.setAuthorInfo(PersonWithNameDTO.create(author));
        }

        if (species != null) {
            dto.setGameSpeciesCode(species.getOfficialCode());
        }

        if (methods != null) {
            dto.setMethods(SrvaMethodDTO.create(methods));
        }

        if (specimens != null) {
            dto.setSpecimens(SrvaSpecimenDTO.create(specimens));
        }

        if (images != null) {
            F.mapNonNulls(images, dto.getImageIds(), Functions.idOf(GameDiaryImage::getFileMetadata));
        }

        if (approverAsPerson != null) {
            dto.setApproverInfo(SrvaEventApproverDTO.create(approverAsPerson));
        } else if (approverAsUser != null) {
            dto.setApproverInfo(SrvaEventApproverDTO.create(approverAsUser));
        }
    }
}
