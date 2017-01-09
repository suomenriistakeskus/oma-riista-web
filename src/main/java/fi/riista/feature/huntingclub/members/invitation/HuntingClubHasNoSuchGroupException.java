package fi.riista.feature.huntingclub.members.invitation;

public class HuntingClubHasNoSuchGroupException extends RuntimeException {

    public HuntingClubHasNoSuchGroupException(Long clubId, Long groupId) {
        super(String.format("Club id:%s has no such group id:%s", clubId, groupId));
    }
}
