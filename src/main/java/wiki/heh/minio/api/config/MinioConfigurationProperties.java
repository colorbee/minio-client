package wiki.heh.minio.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Minio配置属性
 *
 * @author hehua
 */
@ConfigurationProperties("spring.minio")
public class MinioConfigurationProperties {
    /**
     * Minio实例的URL。可以包括HTTP方案。必须包括端口。如果未提供端口，则使用HTTP的端口.
     */
    private String url = "http://10.0.0.240:9000";

    /**
     * Minio实例上的访问密钥（用户名）
     */
    private String accessKey = "minioadmin";

    /**
     * Minio实例上的密钥（密码）
     */
    private String secretKey = "QWERTYUIOPASDFGHJKLZXCVBNM";

    /**
     * 如果{@code url}属性中未提供该方案，请定义是通过HTTP还是HTTPS完成连接.
     */
    private boolean secure = false;

    /**
     * 应用程序的存储桶名称。该存储桶必须已经在Minio上存在。.
     */
    private String bucket;

    /**
     * 在执行器上注册的度量标准配置前缀.
     */
    private String metricName = "minio.storage";

    /**
     * 定义Minio 客户端的连接超时.
     */
    private Duration connectTimeout = Duration.ofSeconds(10);

    /**
     * 定义Minio 客户端的写超时时间时间.
     */
    private Duration writeTimeout = Duration.ofSeconds(60);

    /**
     * 定义Minio 客户端的读取超时时间.
     */
    private Duration readTimeout = Duration.ofSeconds(10);

    /**
     * 检查存储桶是否在Minio实例上存在.
     * 设置为false将在应用程序上下文初始化期间禁用检查.
     * 此属性应仅用于调试目的，因为对Minio的操作在运行时将不起作用.
     */
    private boolean checkBucket = true;

    /**
     * 如果Minio实例上不存在该存储桶，则会创建该存储桶.
     */
    private boolean createBucket = true;

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public boolean isCheckBucket() {
        return checkBucket;
    }

    public void setCheckBucket(boolean checkBucket) {
        this.checkBucket = checkBucket;
    }

    public boolean isCreateBucket() {
        return createBucket;
    }

    public void setCreateBucket(boolean createBucket) {
        this.createBucket = createBucket;
    }
}
