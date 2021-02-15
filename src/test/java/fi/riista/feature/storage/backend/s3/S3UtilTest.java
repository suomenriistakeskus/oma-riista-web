package fi.riista.feature.storage.backend.s3;

import org.junit.Test;

import java.net.URL;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class S3UtilTest {
    public static final String BUCKET = "bucket";
    public static final String BUCKET_INVALID_DNS = "@bucket$";
    public static final String KEY = "key";
    public static final String KEY_WITH_SPACE = "key file.txt";

    // ENCODE

    @Test
    public void testEncode() {
        final URL resourceURL = S3Util.createResourceURL(BUCKET, KEY);
        assertThat(resourceURL.toString(), equalTo("https://bucket.s3-eu-west-1.amazonaws.com/key"));
    }

    @Test
    public void testEncodeKeyWithSpace() {
        final URL resourceURL = S3Util.createResourceURL(BUCKET, KEY_WITH_SPACE);
        assertThat(resourceURL.toString(), equalTo("https://bucket.s3-eu-west-1.amazonaws.com/key%20file.txt"));
    }

    @Test
    public void testEncodeInvalidDnsName() {
        final URL resourceURL = S3Util.createResourceURL(BUCKET_INVALID_DNS, KEY);
        assertThat(resourceURL.toString(), equalTo("https://s3-eu-west-1.amazonaws.com/@bucket$/key"));
    }

    @Test
    public void testEncodeKeyWithSpaceAndInvalidDnsName() {
        final URL resourceURL = S3Util.createResourceURL(BUCKET_INVALID_DNS, "key file.txt");
        assertThat(resourceURL.toString(), equalTo("https://s3-eu-west-1.amazonaws.com/@bucket$/key%20file.txt"));
    }

    // DECODE

    @Test
    public void testDecode() {
        final URL resourceURL = S3Util.createResourceURL(BUCKET, KEY);
        final S3Util.BucketObjectPair s3Object = S3Util.parseResourceURL(resourceURL);
        assertThat(s3Object.getBucketName(), equalTo(BUCKET));
        assertThat(s3Object.getKey(), equalTo(KEY));
    }

    @Test
    public void testDecodeWithSpace() {
        final URL resourceURL = S3Util.createResourceURL(BUCKET, KEY_WITH_SPACE);
        final S3Util.BucketObjectPair s3Object = S3Util.parseResourceURL(resourceURL);
        assertThat(s3Object.getBucketName(), equalTo(BUCKET));
        assertThat(s3Object.getKey(), equalTo(KEY_WITH_SPACE));
    }

    @Test
    public void testDecodeWithInvalidDnsName() {
        final URL resourceURL = S3Util.createResourceURL(BUCKET_INVALID_DNS, KEY);
        final S3Util.BucketObjectPair s3Object = S3Util.parseResourceURL(resourceURL);
        assertThat(s3Object.getBucketName(), equalTo(BUCKET_INVALID_DNS));
        assertThat(s3Object.getKey(), equalTo(KEY));
    }

    @Test
    public void testDecodeWithSpaceAndInvalidDnsName() {
        final URL resourceURL = S3Util.createResourceURL(BUCKET_INVALID_DNS, KEY_WITH_SPACE);
        final S3Util.BucketObjectPair s3Object = S3Util.parseResourceURL(resourceURL);
        assertThat(s3Object.getBucketName(), equalTo(BUCKET_INVALID_DNS));
        assertThat(s3Object.getKey(), equalTo(KEY_WITH_SPACE));
    }
}
