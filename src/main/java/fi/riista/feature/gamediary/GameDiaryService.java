package fi.riista.feature.gamediary;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.CodesetEntryDTO;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields_;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.pub.PublicDTOFactory;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSubQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class GameDiaryService {

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private PersonLookupService personLookupService;

    @Resource
    private PublicDTOFactory dtoFactory;

    public List<CodesetEntryDTO> getGameCategories() {
        return F.mapNonNullsToList(GameCategory.values(), dtoFactory::newCodesetEntryDTO);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<GameSpeciesDTO> getGameSpecies() {
        return GameSpeciesDTO.transformList(gameSpeciesRepository.findAll());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<GameSpeciesDTO> getGameSpeciesRegistrableAsObservationsWithinMooseHunting() {
        return GameSpeciesDTO.transformList(gameSpeciesRepository.findAll(JpaSubQuery
                .of(GameSpecies_.observationContextSensitiveFields)
                .exists((root, cb) -> cb.isTrue(root.get(ObservationContextSensitiveFields_.withinMooseHunting)))));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GameSpecies getGameSpeciesByOfficialCode(int officialCode) {
        return gameSpeciesRepository
                .findByOfficialCode(officialCode)
                .orElseThrow(() -> new NotFoundException("Unknown officialCode for species: " + officialCode));
    }

    private Optional<Person> getPerson(final PersonWithHunterNumberDTO dto) {
        if (dto == null) {
            return Optional.empty();
        } else if (StringUtils.hasText(dto.getHunterNumber())) {
            return Optional.of(personLookupService.findByHunterNumber(dto.getHunterNumber()).orElseThrow(
                    () -> new NotFoundException("Person not found by hunter number: " + dto.getHunterNumber())));
        } else if (dto.getId() != null) {
            // Usually the actor has a hunter number, unless you are a non-hunter adding a diary entry to yourself
            return Optional.of(personLookupService.findById(dto.getId()).orElseThrow(
                    () -> new NotFoundException("Person not found by personId: " + dto.getId())));
        } else {
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void setAuthorAndActor(final GameDiaryEntry diaryEntry,
                                  final HasAuthorAndActor authorAndActor,
                                  final SystemUser activeUser) {
        Objects.requireNonNull(authorAndActor, "authorAndActor is null");
        Objects.requireNonNull(diaryEntry, "diaryEntry is null");
        Objects.requireNonNull(activeUser, "activeUser is null");

        final Person author;
        final Person actor;

        if (diaryEntry.isNew()) {
            if (activeUser.isModeratorOrAdmin()) {
                author = getPerson(authorAndActor.getAuthorInfo())
                        .orElseThrow(() -> new IllegalArgumentException("Author not specified"));
                actor = getPerson(authorAndActor.getActorInfo())
                        .orElseThrow(() -> new IllegalArgumentException("Actor not specified"));
            } else {
                author = Optional.ofNullable(activeUser.getPerson())
                        .orElseThrow(() -> new IllegalStateException("Active user is not associated with person"));
                actor = getPerson(authorAndActor.getActorInfo()).orElse(author);
            }
        } else {
            if (activeUser.isModeratorOrAdmin()) {
                author = getPerson(authorAndActor.getAuthorInfo()).orElse(diaryEntry.getAuthor());
            } else {
                author = diaryEntry.getAuthor();
            }

            actor = getPerson(authorAndActor.getActorInfo()).orElse(diaryEntry.getActor());
        }

        diaryEntry.setAuthor(author);
        diaryEntry.setActor(actor);
    }
}
