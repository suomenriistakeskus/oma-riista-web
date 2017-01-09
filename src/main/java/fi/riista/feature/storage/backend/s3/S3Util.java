package fi.riista.feature.storage.backend.s3;


import fi.riista.config.Constants;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public final class S3Util {
    public static final String ENDPOINT = "s3-eu-west-1.amazonaws.com";
    public static final String SCHEMA = "https://";

    public static class BucketObjectPair {
        private final String bucketName;
        private final String key;

        public BucketObjectPair(final String bucketName, final String key) {
            this.bucketName = Objects.requireNonNull(bucketName);
            this.key = Objects.requireNonNull(key);
        }

        public String getBucketName() {
            return bucketName;
        }

        public String getKey() {
            return key;
        }
    }

    public static URL createResourceURL(final String bucketName,
                                        final String objectKey) {
        final String hostname = generateS3HostnameForBucket(bucketName, ENDPOINT);

        // Determine the resource string (ie the item's path in S3, including the bucket name)
        // @see org.jets3t.service.impl.rest.httpclient.RestStorageService.setupConnection()
        final StringBuilder sb = new StringBuilder();
        sb.append(SCHEMA);
        sb.append(hostname);
        sb.append('/');

        if (hostname.equals(ENDPOINT) && bucketName.length() > 0) {
            sb.append(bucketName);
            sb.append('/');
        }

        try {
            if (objectKey != null) {
                sb.append(encodeUrlPath(objectKey, "/"));
            }

            return new URL(sb.toString());

        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not construct S3 resource URL", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateS3HostnameForBucket(String bucketName, String s3Endpoint) {
        return isBucketNameValidDNSName(bucketName)
                ? bucketName + "." + s3Endpoint
                : s3Endpoint;
    }

    /**
     * Returns true if the given bucket name can be used as a component of a valid
     * DNS name. If so, the bucket can be accessed using requests with the bucket name
     * as part of an S3 sub-domain. If not, the old-style bucket reference URLs must be
     * used, in which case the bucket name must be the first component of the resource
     * path.
     *
     * @param bucketName the name of the bucket to test for DNS compatibility.
     */
    private static boolean isBucketNameValidDNSName(String bucketName) {
        if (bucketName == null || bucketName.length() > 63 || bucketName.length() < 3) {
            return false;
        }

        // Only lower-case letters, numbers, '.' or '-' characters allowed
        if (!Pattern.matches("^[a-z0-9][a-z0-9.-]+$", bucketName)) {
            return false;
        }

        // Cannot be an IP address, i.e. must not contain four '.'-delimited
        // sections with 1 to 3 digits each.
        if (Pattern.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}", bucketName)) {
            return false;
        }

        // Components of name between '.' characters cannot start or end with '-',
        // and cannot be empty
        final String[] fragments = bucketName.split("\\.");

        for (final String fragment : fragments) {
            if (Pattern.matches("^-.*", fragment)
                    || Pattern.matches(".*-$", fragment)
                    || Pattern.matches("^$", fragment)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Encodes a URL string but leaves a delimiter string unencoded.
     * Spaces are encoded as "%20" instead of "+".
     *
     * @return encoded URL string.
     */
    private static String encodeUrlPath(final String path, final String delimiter) throws UnsupportedEncodingException {
        final StringBuilder result = new StringBuilder();
        final StringTokenizer t = new StringTokenizer(path, delimiter);
        if (!t.hasMoreTokens()) {
            return path;
        }
        if (path.startsWith(delimiter)) {
            result.append(delimiter);
        }
        while (t.hasMoreTokens()) {
            result.append(encodeUrlString(t.nextToken()));
            if (t.hasMoreTokens()) {
                result.append(delimiter);
            }
        }
        if (path.endsWith(delimiter)) {
            result.append(delimiter);
        }
        return result.toString();
    }

    /**
     * Encodes a URL string, and ensures that spaces are encoded as "%20" instead of "+" to keep
     * fussy web browsers happier.
     *
     * @return encoded URL.
     */
    private static String encodeUrlString(String path) throws UnsupportedEncodingException {
        String encodedPath = URLEncoder.encode(path, Constants.DEFAULT_ENCODING);
        // Web browsers do not always handle '+' characters well, use the well-supported '%20' instead.
        encodedPath = encodedPath.replaceAll("\\+", "%20");
        // '@' character need not be URL encoded and Google Chrome balks on signed URLs if it is.
        encodedPath = encodedPath.replaceAll("%40", "@");
        return encodedPath;
    }

    /**
     * Builds an object based on the bucket name and object key information
     * available in the components of a URL.
     *
     * @return the object referred to by the URL components.
     */
    public static BucketObjectPair parseResourceURL(final URL url) {
        try {
            String urlPath = url.getPath();

            if (urlPath.startsWith("/")) {
                urlPath = urlPath.substring(1); // Ignore first '/' character in url path.
            }

            String bucketName;
            String objectKey;

            if (!ENDPOINT.equals(url.getHost())) {
                bucketName = findBucketNameInHostname(url.getHost(), ENDPOINT);
            } else {
                // Bucket name must be first component of URL path
                int slashIndex = urlPath.indexOf("/");
                bucketName = URLDecoder.decode(
                        urlPath.substring(0, slashIndex), Constants.DEFAULT_ENCODING);

                // Remove the bucket name component of the host name
                urlPath = urlPath.substring(bucketName.length() + 1);
            }

            objectKey = URLDecoder.decode(urlPath, Constants.DEFAULT_ENCODING);

            return new BucketObjectPair(bucketName, objectKey);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Identifies the name of a bucket from a given host name, if available.
     * Returns null if the bucket name cannot be identified, as might happen
     * when a bucket name is represented by the path component of a URL instead
     * of the host name component.
     *
     * @param host the host name component of a URL that may include the bucket name,
     *             if an alternative host name is in use.
     * @return The S3 bucket name represented by the DNS host name, or null if none.
     */
    public static String findBucketNameInHostname(final String host, final String s3Endpoint) {
        // Bucket name is available in URL's host name.
        if (host.endsWith(s3Endpoint)) {
            // Bucket name is available as S3 subdomain
            return host.substring(0, host.length() - s3Endpoint.length() - 1);
        }
        // URL refers to a virtual host name
        return host;
    }

    private S3Util() {
        throw new AssertionError();
    }
}
