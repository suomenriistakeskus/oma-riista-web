package fi.riista.api.pub;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class NominationDecisionDownloadDTO {

    public static class NominationDecisionLinkDTO {
        private final String url;
        private final String linkName;

        private NominationDecisionLinkDTO(final @Nonnull String url, final String linkName) {
            this.url = requireNonNull(url);
            this.linkName = linkName;
        }

        public String getUrl() {
            return url;
        }

        public String getLinkName() {
            return linkName;
        }
    }

    private final NominationDecisionLinkDTO decisionLink;

    private final List<NominationDecisionLinkDTO> attachmentLinks;

    private NominationDecisionDownloadDTO(final @Nonnull NominationDecisionLinkDTO decisionLink,
                                          final @Nonnull List<NominationDecisionLinkDTO> attachmentLinks) {
        this.decisionLink = requireNonNull(decisionLink);
        this.attachmentLinks = requireNonNull(attachmentLinks);
    }

    public NominationDecisionLinkDTO getDecisionLink() {
        return decisionLink;
    }

    public List<NominationDecisionLinkDTO> getAttachmentLinks() {
        return attachmentLinks;
    }

    public static final class Builder {
        private NominationDecisionLinkDTO decisionLink;
        private final List<NominationDecisionLinkDTO> attachmentLinks;

        private Builder() {
            attachmentLinks = new ArrayList<>();
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withDecisionLink(final @Nonnull String url, final @Nonnull String linkName) {
            requireNonNull(linkName);
            this.decisionLink = new NominationDecisionLinkDTO(url, linkName);
            return this;
        }

        public Builder withAttachment(final @Nonnull String url, final @Nullable String linkName) {
            this.attachmentLinks.add(new NominationDecisionLinkDTO(url, linkName));
            return this;
        }

        public NominationDecisionDownloadDTO build() {
            return new NominationDecisionDownloadDTO(decisionLink, ImmutableList.copyOf(attachmentLinks));
        }
    }
}
