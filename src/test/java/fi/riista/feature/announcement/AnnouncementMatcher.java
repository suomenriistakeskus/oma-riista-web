package fi.riista.feature.announcement;

import fi.riista.feature.announcement.show.ListAnnouncementDTO;
import fi.riista.feature.organization.Organisation;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

public class AnnouncementMatcher {
    private AnnouncementMatcher() {
        throw new AssertionError();
    }

    public static Matcher<ListAnnouncementDTO> isEqualAnnouncement(final Announcement announcement) {
        return Matchers.allOf(
                hasBody(announcement.getBody()),
                hasSubject(announcement.getSubject()),
                hasSenderType(announcement.getSenderType()),
                hasFromOrganisation(announcement.getFromOrganisation()),
                hasVisibleToAll(announcement.isVisibleToAll())
        );
    }

    public static FeatureMatcher<ListAnnouncementDTO, String> hasBody(final String expected) {
        return new FeatureMatcher<ListAnnouncementDTO, String>(equalTo(expected), "body", "body") {
            @Override
            protected String featureValueOf(final ListAnnouncementDTO announcement) {
                return announcement.getBody();
            }
        };
    }

    public static FeatureMatcher<ListAnnouncementDTO, String> hasSubject(final String expected) {
        return new FeatureMatcher<ListAnnouncementDTO, String>(equalTo(expected), "subject", "subject") {
            @Override
            protected String featureValueOf(final ListAnnouncementDTO announcement) {
                return announcement.getSubject();
            }
        };
    }

    public static FeatureMatcher<ListAnnouncementDTO, AnnouncementSenderType> hasSenderType(final AnnouncementSenderType expected) {
        return new FeatureMatcher<ListAnnouncementDTO, AnnouncementSenderType>(equalTo(expected), "senderType", "senderType") {
            @Override
            protected AnnouncementSenderType featureValueOf(final ListAnnouncementDTO announcement) {
                return announcement.getSenderType();
            }
        };
    }

    public static FeatureMatcher<ListAnnouncementDTO, Map<String, String>> hasFromOrganisation(final Organisation expected) {
        return new FeatureMatcher<ListAnnouncementDTO, Map<String, String>>(equalTo(expected.getNameLocalisation().asMap()), "fromOrganisation", "fromOrganisation") {
            @Override
            protected Map<String, String> featureValueOf(final ListAnnouncementDTO announcement) {
                return announcement.getFromOrganisation().getName();
            }
        };
    }

    public static Matcher<ListAnnouncementDTO> hasVisibleToAll(final boolean expected) {
        return new FeatureMatcher<ListAnnouncementDTO, Boolean>(equalTo(expected), "visibleToAll", "visibleToAll") {
            @Override
            protected Boolean featureValueOf(final ListAnnouncementDTO announcement) {
                return announcement.isVisibleToAll();
            }
        };
    }
}
