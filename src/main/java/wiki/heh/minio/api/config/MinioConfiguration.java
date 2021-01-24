package wiki.heh.minio.api.config;

import io.minio.MinioClient;
import io.minio.errors.*;
import wiki.heh.minio.api.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Minio配置类
 *
 * @author hehua
 */
@Configuration
@ConditionalOnClass(MinioClient.class)
@EnableConfigurationProperties(MinioConfigurationProperties.class)
@ComponentScan("wiki.heh.minio.api.minio")
public class MinioConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioConfiguration.class);

    @Autowired
    private MinioConfigurationProperties minioConfigurationProperties;

    @Bean
    public MinioClient minioClient() throws InvalidEndpointException, InvalidPortException, IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, wiki.heh.minio.api.MinioException {

        MinioClient minioClient = null;
        try {
            minioClient = new MinioClient(
                minioConfigurationProperties.getUrl(),
                minioConfigurationProperties.getAccessKey(),
                minioConfigurationProperties.getSecretKey(),
                minioConfigurationProperties.isSecure()
            );
            minioClient.setTimeout(
                minioConfigurationProperties.getConnectTimeout().toMillis(),
                minioConfigurationProperties.getWriteTimeout().toMillis(),
                minioConfigurationProperties.getReadTimeout().toMillis()
            );
        } catch (InvalidEndpointException | InvalidPortException e) {
            LOGGER.error("Error while connecting to Minio", e);
            throw e;
        }

        if (minioConfigurationProperties.isCheckBucket()) {
            try {
                LOGGER.debug("Checking if bucket {} exists", minioConfigurationProperties.getBucket());
                boolean b = minioClient.bucketExists(minioConfigurationProperties.getBucket());
                if (!b) {
                    if (minioConfigurationProperties.isCreateBucket()) {
                        try {
                            minioClient.makeBucket(minioConfigurationProperties.getBucket());
                        } catch (RegionConflictException e) {
                            throw new wiki.heh.minio.api.MinioException("Cannot create bucket", e);
                        }
                    } else {
                        throw new InvalidBucketNameException(minioConfigurationProperties.getBucket(), "Bucket does not exists");
                    }
                }
            } catch
            (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidResponseException | MinioException
                    e) {
                LOGGER.error("Error while checking bucket", e);
                throw e;
            }
        }

        return minioClient;
    }

}
