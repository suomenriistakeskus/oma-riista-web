package fi.riista.integration.paytrail.event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaytrailPaymentEventRepository extends JpaRepository<PaytrailPaymentEvent, Long> {
}
