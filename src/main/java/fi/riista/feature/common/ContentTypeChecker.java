package fi.riista.feature.common;

import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
public class ContentTypeChecker {

    private static final Logger LOG = LoggerFactory.getLogger(ContentTypeChecker.class);

    private final Tika tika = new Tika();

    public Optional<ResponseEntity<?>> validate(final MultipartFile file, final Set<String> allowed) {
        try (final TikaInputStream stream = TikaInputStream.get(file.getInputStream())) {

            final String contentType = tika.detect(stream, file.getName());
            if (!allowed.contains(contentType)) {
                return badRequest("Invalid media type, allowed:" + allowed);
            }

        } catch (final IOException e) {
            LOG.error("Unknown error", e);
            return badRequest("Unknown error");
        }
        return Optional.empty();
    }

    private static Optional<ResponseEntity<?>> badRequest(final String body) {
        return Optional.of(ResponseEntity.badRequest().body(body));
    }
}
