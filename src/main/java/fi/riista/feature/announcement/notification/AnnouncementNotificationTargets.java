package fi.riista.feature.announcement.notification;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class AnnouncementNotificationTargets {
    private final List<String> emails;
    private final List<String> pushTokens;

    public AnnouncementNotificationTargets(final List<String> emails, final List<String> pushTokens) {
        this.emails = requireNonNull(emails);
        this.pushTokens = requireNonNull(pushTokens);
    }

    public List<String> getEmails() {
        return emails;
    }

    public List<String> getPushTokens() {
        return pushTokens;
    }
}
