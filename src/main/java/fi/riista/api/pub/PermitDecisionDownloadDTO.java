package fi.riista.api.pub;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class PermitDecisionDownloadDTO {

    public static class PermitDecisionLinkDTO {
        private final String url;
        private final String linkName;

        public static PermitDecisionLinkDTO of(final String url, final String linkName) {
            return new PermitDecisionLinkDTO(url, linkName);
        }

        private PermitDecisionLinkDTO(final String url, final String linkName) {
            this.url = url;
            this.linkName = linkName;
        }

        public String getUrl() {
            return url;
        }

        public String getLinkName() {
            return linkName;
        }
    }

    private final PermitDecisionLinkDTO decisionLink;

    private final List<PermitDecisionLinkDTO> attachmentLinks;

    public PermitDecisionDownloadDTO(final PermitDecisionLinkDTO decisionLink,
                                     final List<PermitDecisionLinkDTO> attachmentLinks) {
        this.decisionLink = decisionLink;
        this.attachmentLinks = attachmentLinks;
    }

    public PermitDecisionLinkDTO getDecisionLink() {
        return decisionLink;
    }

    public List<PermitDecisionLinkDTO> getAttachmentLinks() {
        return attachmentLinks;
    }

    public static final class Builder {
        private PermitDecisionLinkDTO decisionLink;
        private List<PermitDecisionLinkDTO> attachmentLinks;

        private Builder() {
            attachmentLinks = Lists.newArrayList();
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withDecisionLink(final String url, final String linkName) {
            this.decisionLink = new PermitDecisionLinkDTO(url, linkName);
            return this;
        }

        public Builder withAttachment(final String url, final String linkName) {
            this.attachmentLinks.add(requireNonNull(new PermitDecisionLinkDTO(url, linkName)));
            return this;
        }

        public PermitDecisionDownloadDTO build() {
            return new PermitDecisionDownloadDTO(decisionLink, ImmutableList.copyOf(attachmentLinks));
        }
    }
}
