package fi.riista.feature.huntingclub.moosedatacard.exception;

import static java.util.stream.Collectors.joining;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MooseDataCardImportException extends Exception {

    private final List<String> messages;

    public static MooseDataCardImportException internalServerError() {
        return of(MooseDataCardImportFailureReasons.internalServerError());
    }

    public static MooseDataCardImportException couldNotCreateHuntingClubGroup() {
        return of(MooseDataCardImportFailureReasons.couldNotCreateHuntingClubGroup());
    }

    public static MooseDataCardImportException couldNotCreateHuntingDays() {
        return of(MooseDataCardImportFailureReasons.couldNotCreateHuntingDays());
    }

    public static MooseDataCardImportException couldNotCreateHarvests() {
        return of(MooseDataCardImportFailureReasons.couldNotCreateHarvests());
    }

    public static MooseDataCardImportException couldNotCreateObservations() {
        return of(MooseDataCardImportFailureReasons.couldNotCreateObservations());
    }

    public static MooseDataCardImportException couldNotCreateMooseDataCardImport() {
        return of(MooseDataCardImportFailureReasons.couldNotCreateMooseDataCardImport());
    }

    public static MooseDataCardImportException failureOnFileStorage() {
        return of(MooseDataCardImportFailureReasons.failureOnFileStorage());
    }

    public static MooseDataCardImportException contactPersonMemberOfMultipleMooseDataCardGroupsButWithNoActiveOccupations() {
        return of(MooseDataCardImportFailureReasons
                .contactPersonMemberOfMultipleMooseDataCardGroupsButWithNoActiveOccupations());
    }

    public static MooseDataCardImportException huntingClubAlreadyHasGroupNotCreatedWithinMooseDataCardImport() {
        return of(MooseDataCardImportFailureReasons.huntingClubAlreadyHasGroupNotCreatedWithinMooseDataCardImport());
    }

    public static MooseDataCardImportException contactPersonMemberOfMultipleMooseDataCardGroupsButNotAsLeader() {
        return of(MooseDataCardImportFailureReasons.contactPersonMemberOfMultipleMooseDataCardGroupsButNotAsLeader());
    }

    public static MooseDataCardImportException contactPersonIsLeaderInMultipleMooseDataCardGroups() {
        return of(MooseDataCardImportFailureReasons.contactPersonIsLeaderInMultipleMooseDataCardGroups());
    }

    public static MooseDataCardImportException of(@Nonnull final String message) {
        return new MooseDataCardImportException(message);
    }

    public static MooseDataCardImportException of(@Nonnull final String message, @Nullable final Throwable cause) {
        return new MooseDataCardImportException(message, cause);
    }

    public static MooseDataCardImportException of(@Nonnull final List<String> messages) {
        return new MooseDataCardImportException(messages);
    }

    public static MooseDataCardImportException of(
            @Nonnull final List<String> messages, @Nullable final Throwable cause) {

        return new MooseDataCardImportException(messages, cause);
    }

    public MooseDataCardImportException(@Nonnull final String message) {
        this(message, null);
    }

    public MooseDataCardImportException(@Nonnull final String message, @Nullable final Throwable cause) {
        super(cause);
        Objects.requireNonNull(message, "message is null");
        this.messages = Collections.singletonList(message);
    }

    public MooseDataCardImportException(@Nonnull final List<String> messages) {
        this(messages, null);
    }

    public MooseDataCardImportException(@Nonnull final List<String> messages, @Nullable final Throwable cause) {
        super(cause);

        Objects.requireNonNull(messages, "messages is null");

        this.messages = messages.isEmpty()
                ? Collections.emptyList()
                : messages.size() == 1 ? Collections.singletonList(messages.get(0)) : new ArrayList<>(messages);
    }

    @Override
    public String getMessage() {
        return getMessages().stream().collect(joining("\n"));
    }

    public List<String> getMessages() {
        return messages;
    }

}
