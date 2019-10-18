package fi.riista.security.authentication;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Pattern;

public class RiistaPasswordEncoder implements PasswordEncoder {
    private Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}");
    private Pattern PLAIN_PATTERN = Pattern.compile("\\Aplaintext:.{16,}");

    @Override
    public String encode(final CharSequence rawPassword) {
        return BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt(10));
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        if (rawPassword == null || rawPassword.length() == 0) {
            return false;
        }

        if (encodedPassword == null || encodedPassword.length() == 0) {
            return false;
        }

        if (BCRYPT_PATTERN.matcher(encodedPassword).matches()) {
            return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
        }

        if (PLAIN_PATTERN.matcher(encodedPassword).matches()) {
            // Allow fast plaint-text comparison for strong generated passwords when storing digest does not make sense.
            return rawPassword.equals(encodedPassword.substring(10));
        }

        return false;
    }
}
