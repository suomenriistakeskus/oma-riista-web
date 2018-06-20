package fi.riista.feature.mail.bounce;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MailMessageBounceRepository extends JpaRepository<MailMessageBounce, Long> {
}
