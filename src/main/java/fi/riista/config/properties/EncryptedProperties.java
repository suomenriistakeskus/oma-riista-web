package fi.riista.config.properties;

import fi.riista.util.JCEUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.jasypt.salt.RandomSaltGenerator;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class EncryptedProperties {
    private static final String ALGORITHM = "PBEWITHSHA256AND256BITAES-CBC-BC";
    private static final int KEY_OBTENTION_ITERATIONS = 4000;

    public static EnvironmentStringPBEConfig createPBEConfig(final String password) {
        final EnvironmentStringPBEConfig encryptor = new EnvironmentStringPBEConfig();
        encryptor.setAlgorithm(ALGORITHM);
        encryptor.setKeyObtentionIterations(KEY_OBTENTION_ITERATIONS);
        encryptor.setPassword(password);
        encryptor.setSaltGenerator(new RandomSaltGenerator());
        encryptor.setProvider(new BouncyCastleProvider());
        return encryptor;
    }

    // mvn exec:java -Dexec.mainClass="fi.riista.config.properties.EncryptedProperties" -Dexec.args="salasana"
    public static void main(String[] args) {
        JCEUtil.removeJavaCryptographyAPIRestrictions();

        if (args.length != 1) {
            return;
        }

        final String password = args[0];
        final StandardPBEStringEncryptor enc = new StandardPBEStringEncryptor();
        enc.setConfig(createPBEConfig(password));

        final List<String> lines = Arrays.stream(getClassPathPropertyResources())
                .map(resource -> {
                    final Properties properties = new Properties();
                    try {
                        properties.load(resource.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return properties;
                })
                .flatMap(props -> props.entrySet().stream())
                .map(entry -> {
                    final String value = entry.getValue().toString();

                    if (!StringUtils.hasText(value)) {
                        return entry.getKey() + "=";
                    } else if (PropertyValueEncryptionUtils.isEncryptedValue(value)) {
                        return entry.getKey() + "=" + value;
                    } else {
                        return entry.getKey() + "=" + PropertyValueEncryptionUtils.encrypt(value, enc);
                    }
                })
                .sorted(Comparator.comparing(line -> line.substring(0, line.indexOf('='))))
                .collect(Collectors.toList());

        for (String s : lines) {
            System.out.println(s);
        }
    }

    private static Resource[] getClassPathPropertyResources() {
        final ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        try {
            return patternResolver.getResources("classpath:configuration/*.properties");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class PlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {
        private final StandardPBEStringEncryptor encryptor;

        public PlaceholderConfigurer(final StandardPBEStringEncryptor encryptor) {
            this.encryptor = encryptor;
        }

        @Override
        protected void doProcessProperties(final ConfigurableListableBeanFactory beanFactoryToProcess,
                                           final StringValueResolver valueResolver) {
            super.doProcessProperties(beanFactoryToProcess, new EncryptedValueResolver(valueResolver, encryptor));
        }
    }

    private static class EncryptedValueResolver implements StringValueResolver {
        private final StringValueResolver valueResolver;
        private final PBEStringEncryptor stringEncryptor;

        private EncryptedValueResolver(final StringValueResolver stringValueResolver,
                                       final StandardPBEStringEncryptor stringEncryptor) {
            this.valueResolver = stringValueResolver;
            this.stringEncryptor = stringEncryptor;
        }

        @Override
        public String resolveStringValue(final String strVal) {
            final String value = valueResolver.resolveStringValue(strVal);
            return PropertyValueEncryptionUtils.isEncryptedValue(value)
                    ? PropertyValueEncryptionUtils.decrypt(value, this.stringEncryptor)
                    : value;
        }
    }

    private EncryptedProperties() {
        throw new AssertionError();
    }
}
