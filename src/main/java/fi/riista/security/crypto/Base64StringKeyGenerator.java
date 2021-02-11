package fi.riista.security.crypto;

import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

import java.util.Base64;

public class Base64StringKeyGenerator implements StringKeyGenerator {

    public static StringKeyGenerator withBitLength(int length) {
        return new Base64StringKeyGenerator(KeyGenerators.secureRandom(length));
    }

    private final BytesKeyGenerator keyGenerator;

    public Base64StringKeyGenerator(BytesKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    @Override
    public String generateKey() {
        return new String(Base64.getEncoder().encode(keyGenerator.generateKey()));
    }
}
