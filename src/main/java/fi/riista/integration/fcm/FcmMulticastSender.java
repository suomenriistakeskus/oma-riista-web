package fi.riista.integration.fcm;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;

import java.util.Optional;

public interface FcmMulticastSender {

    Optional<BatchResponse> send(MulticastMessage message) throws FirebaseMessagingException;
}
