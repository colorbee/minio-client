package wiki.heh.minio.api;

import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import wiki.heh.minio.api.config.MinioConfigurationProperties;
import wiki.heh.minio.api.config.WatermarkConfiguration;
import wiki.heh.minio.api.enums.Image;
import wiki.heh.minio.api.util.ImageUtil;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 与Minio存储桶进行交互的服务类。此类注册为Bean并使用{@link MinioConfigurationProperties}中定义的属性。
 * 所有方法都返回{@link wiki.heh.minio.api.MinioException}，其中包装了Minio SDK异常.
 * 存储桶名称随配置属性中定义的名称一起提供.
 *
 * @author hehua
 */
@Service
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfigurationProperties configurationProperties;
    private final WatermarkConfiguration conf;

    public MinioService(MinioClient minioClient, MinioConfigurationProperties configurationProperties, WatermarkConfiguration conf) {
        this.minioClient = minioClient;
        this.configurationProperties = configurationProperties;
        this.conf = conf;
    }

    /**
     * 列出存储桶根目录中的所有对象
     *
     * @return 项目列表
     */
    public List<Item> list() {
        Iterable<Result<Item>> myObjects = minioClient.listObjects(configurationProperties.getBucket(), "", false);
        return getItems(myObjects);
    }

    /**
     * 列出存储桶根目录中的所有对象
     *
     * @return 项目列表
     * @throws wiki.heh.minio.api.MinioException 如果在获取列表时发生错误
     */
    public List<Item> fullList() throws wiki.heh.minio.api.MinioException {
        try {
            Iterable<Result<Item>> myObjects = minioClient.listObjects(configurationProperties.getBucket());
            return getItems(myObjects);
        } catch (XmlPullParserException e) {
            throw new wiki.heh.minio.api.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * 列出具有存储桶参数中给定前缀的所有对象.
     * 模拟文件夹层次结构。文件夹内的对象（即与模式{@code {prefix} / {objectName} / ...}匹配的所有对象）不返回
     *
     * @param path 搜寻对象列表的前缀
     * @return 项目列表
     */
    public List<Item> list(Path path) {

        Iterable<Result<Item>> myObjects = minioClient.listObjects(configurationProperties.getBucket(), path.toString(), false);
        return getItems(myObjects);
    }

    /**
     * 列出具有存储桶参数中给定前缀的所有对象
     * <p>
     * 返回所有对象，即使是文件夹中的对象.
     *
     * @param path 搜寻对象列表的前缀
     * @return 项目列表
     */
    public List<Item> getFullList(Path path) throws wiki.heh.minio.api.MinioException {
        try {
            Iterable<Result<Item>> myObjects = minioClient.listObjects(configurationProperties.getBucket(), path.toString());
            return getItems(myObjects);
        } catch (XmlPullParserException e) {
            throw new wiki.heh.minio.api.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * 将结果映射到项目并返回列表的实用程序方法
     *
     * @param myObjects 结果的迭代
     * @return 项目列表
     */
    private List<Item> getItems(Iterable<Result<Item>> myObjects) {
        return StreamSupport
                .stream(myObjects.spliterator(), true)
                .map(itemResult -> {
                    try {
                        return itemResult.get();
                    } catch (InvalidBucketNameException |
                            NoSuchAlgorithmException |
                            InsufficientDataException |
                            IOException |
                            InvalidKeyException |
                            NoResponseException |
                            XmlPullParserException |
                            ErrorResponseException |
                            InternalException e) {
                        throw new MinioFetchException("Error while parsing list of objects", e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 从Minio获取对象 InputStream
     *
     * @param path 带有对象前缀的路径.必须包含对象名称.
     * @return 该对象的InputStream
     * @throws wiki.heh.minio.api.MinioException 如果在获取对象时发生错误
     */
    public InputStream get(Path path) throws wiki.heh.minio.api.MinioException {
        try {
            return minioClient.getObject(configurationProperties.getBucket(), path.toString());
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | InvalidResponseException e) {
            throw new wiki.heh.minio.api.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * 从Minio获取对象的元数据
     *
     * @param path 带有对象前缀的路径.必须包含对象名称.
     * @return 对象的元数据
     * @throws wiki.heh.minio.api.MinioException 如果在获取对象元数据时发生错误
     */
    public ObjectStat getMetadata(Path path) throws wiki.heh.minio.api.MinioException {
        try {
            return minioClient.statObject(configurationProperties.getBucket(), path.toString());
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | InvalidResponseException e) {
            throw new wiki.heh.minio.api.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * 从Minio获取多个对象的元数据
     *
     * @param paths 带有对象前缀的路径.必须包含对象名称.
     * @return 所有路径都是key而元数据是值的Map
     */
    public Map<Path, ObjectStat> getMetadata(Iterable<Path> paths) {
        return StreamSupport.stream(paths.spliterator(), false)
                .map(path -> {
                    try {
                        return new HashMap.SimpleEntry<>(path, minioClient.statObject(configurationProperties.getBucket(), path.toString()));
                    } catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidResponseException | InvalidArgumentException e) {
                        throw new MinioFetchException("Error while parsing list of objects", e);
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 在给定存储桶中获取对象的数据，并将其存储到给定的{@code fileName}文件中
     *
     * @param source   带有对象前缀的路径.必须包含对象名称.
     * @param fileName 文件名称
     * @throws wiki.heh.minio.api.MinioException 如果在获取对象时发生错误
     */
    public void getAndSave(Path source, String fileName) throws wiki.heh.minio.api.MinioException {
        try {
            minioClient.getObject(configurationProperties.getBucket(), source.toString(), fileName);
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | InvalidResponseException e) {
            throw new wiki.heh.minio.api.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * 将文件上传到Minio
     *
     * @param source      带有对象前缀的路径.必须包含对象名称.
     * @param file        文件作为输入流 InputStream
     * @param contentType 对象的MIME类型
     * @throws wiki.heh.minio.api.MinioException 如果上传对象时发生错误
     */
    public void upload(Path source, InputStream file, ContentType contentType) throws wiki.heh.minio.api.MinioException {
        upload(source, file, contentType, null);
    }

    /**
     * 将文件上传到Minio
     *
     * @param source      带有对象前缀的路径.必须包含对象名称.
     * @param file        文件作为输入流 InputStream
     * @param contentType 对象的MIME类型
     * @param headers     要放在文件上的其他标题。地图必须是可变的。使用{@code getMetadata（）}方法获取时，所有自定义标头均以'x-amz-meta-'前缀开头.
     * @throws wiki.heh.minio.api.MinioException 如果上传对象时发生错误
     */
    public void upload(Path source, InputStream file, ContentType contentType, Map<String, String> headers) throws wiki.heh.minio.api.MinioException {
        upload(source, file, contentType.getMimeType(), null, null);
    }

    /**
     * 将文件上传到Minio
     *
     * @param source      带有对象前缀的路径.必须包含对象名称.
     * @param file        文件作为输入流 InputStream
     * @param contentType 对象的MIME类型
     * @throws wiki.heh.minio.api.MinioException 如果上传对象时发生错误
     */
    public void upload(Path source, InputStream file, String contentType) throws wiki.heh.minio.api.MinioException {
        upload(source, file, contentType, null, null);
    }

    /**
     * 将文件上传到Minio
     *
     * @param source      带有对象前缀的路径.必须包含对象名称.
     * @param file        文件作为输入流 InputStream
     * @param contentType 对象的MIME类型
     * @param headers     要放在文件上的其他标题.Map必须是可变的
     * @throws wiki.heh.minio.api.MinioException 如果上传对象时发生错误
     */
    public void upload(Path source, InputStream file, String contentType, Map<String, String> headers) throws wiki.heh.minio.api.MinioException {
        upload(source, file, contentType, headers, null);
    }

    /**
     * @param source      带有对象前缀的路径.必须包含对象名称.
     * @param file        文件作为输入流 InputStream
     * @param contentType 对象的MIME类型
     * @param image       图片的处理方式，压缩或者加水印
     * @throws wiki.heh.minio.api.MinioException 如果上传对象时发生错误
     */
    public void upload(Path source, InputStream file, String contentType, Image image) throws wiki.heh.minio.api.MinioException {
        upload(source, file, contentType, null, image);
    }

    /**
     * @param source      带有对象前缀的路径.必须包含对象名称.
     * @param file        文件作为输入流 InputStream
     * @param contentType 对象的MIME类型
     * @param headers     要放在文件上的其他标题。Map必须是可变的。使用{@code getMetadata（）}方法获取时，所有自定义标头均以'x-amz-meta-'前缀开头.
     * @param image       图片的处理方式，压缩或者加水印
     * @throws wiki.heh.minio.api.MinioException 如果上传对象时发生错误
     */
    public void upload(Path source, InputStream file, String contentType, Map<String, String> headers, Image image) throws wiki.heh.minio.api.MinioException {
        try {
            ImageUtil util = new ImageUtil(conf);
            //如果是图片文件就进行压缩
            if (image != null && util.isImage(source.getFileName().toString())) {
                BufferedImage bufferedImage = ImageIO.read(file);
                if (image.isAll()) {
                    bufferedImage = util.setWatermark(util.compress(bufferedImage));
                }
                if (image.isWatermark()) {
                    bufferedImage = util.setWatermark(bufferedImage);
                }
                if (image.isCompression()) {
                    bufferedImage = util.compress(bufferedImage);
                }
                file = util.getInputStream(bufferedImage, util.getFileExtention(source.getFileName().toString()));
            }
            minioClient.putObject(configurationProperties.getBucket(), source.toString(), file, (long) file.available(), headers, null, contentType);
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException |
                InsufficientDataException | IOException | InvalidKeyException | NoResponseException |
                ErrorResponseException | InternalException | InvalidArgumentException | InvalidResponseException e) {
            throw new wiki.heh.minio.api.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * 删除Minio中的文件
     *
     * @param source 带有对象前缀的路径.必须包含对象名称.
     * @throws wiki.heh.minio.api.MinioException 如果删除对象时发生错误
     */
    public void remove(Path source) throws wiki.heh.minio.api.MinioException {
        try {
            minioClient.removeObject(configurationProperties.getBucket(), source.toString());
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | InvalidResponseException e) {
            throw new wiki.heh.minio.api.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * 返回一个预签名的URL，以在给定的到期时间下将对象下载到存储桶中.
     *
     * </p><b>Example:</b><br>
     * <pre>{@code String url = minioService.getObjectURL("my-objectname", 60 * 60 * 24);
     * System.out.println(url); }</pre>
     *
     * @param source  带有对象前缀的路径.必须包含对象名称.
     * @param expires 预设网址的有效时间（以秒为单位）.
     * @return 字符串 包含下载对象的URL.
     */
    public String getURL(Path source, Integer expires) throws wiki.heh.minio.api.MinioException {
        try {
            return minioClient.presignedGetObject(configurationProperties.getBucket(), source.toString(), expires);
        } catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidExpiresRangeException | InvalidResponseException e) {
            throw new wiki.heh.minio.api.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * 返回一个可以访问对象的URL.
     *
     * </p><b>Example:</b><br>
     * <pre>{@code String url = minioService.getURL("my-objectname");
     * System.out.println(url); }</pre>
     *
     * @param objectName 对象名称.
     * @return 字符串 包含下载对象的URL.
     */
    public String getURL(String objectName) throws wiki.heh.minio.api.MinioException {
        try {
            return minioClient.getObjectUrl(configurationProperties.getBucket(), objectName);
        } catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidResponseException e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

}
