# Spring Boot Starter Minio

Spring Boot Starter,它可以连接到Minio存储桶,以保存,获取和删除对象.入门者还为执行器嵌入了指标和运行状况检查.

## 快速开始

只需将依赖项添加到现有的Spring Boot项目中即可.

Maven
```xml
<dependency>
    <groupId>wiki.heh.minio.api</groupId>
    <artifactId>minio-client</artifactId>
    <version>1.0.0.RELEASE</version>
</dependency>
```


然后,将以下属性添加到您的`application.properties` 文件中.

```yml
spring:
  minio:
    # Minio Host
    url: http://10.0.0.240:9000/
    # 您的应用的Minio桶名称
    bucket: test
    # Minio access key (用户名)
    access-key: minioadmin
    # Minio secret key (密码)
    secret-key: minioadmin
```

默认值在公共Minio实例上参数化.

然后,您就可以开始启动应用程序了.在Spring上下文初始化时建立Minio连接.如果无法建立连接,则您的应用程序将无法启动.

## 取得资料

启动程序包括一个实用程序bean`MinioService`,它允许尽可能简单地请求Minio.异常被包装到一个MinioException中,并且bucket参数根据应用程序属性中设置的内容进行填充.

这个快速的示例是一个Spring REST控制器,它允许在存储桶的根目录中列出文件,并下载其中之一.

```java
@RestController
@RequestMapping("/files")
public class TestController {

    @Autowired
    private MinioService minioService;


    @GetMapping("/")
    public List<Item> testMinio() throws MinioException {
        return minioService.list();
    }

    @GetMapping("/{object}")
    public void getObject(@PathVariable("object") String object, HttpServletResponse response) throws MinioException, IOException {
        InputStream inputStream = minioService.get(Path.of(object));
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        // 设置内容类型和附件标题.
        response.addHeader("Content-disposition", "attachment;filename=" + object);
        response.setContentType(URLConnection.guessContentTypeFromName(object));

        // 将流复制到响应的输出流.
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
    }
}
```

您也可以直接使用原始SDK中声明为bean的`MinioClient`.只需添加：

```java
public class TestController {
    @Autowired
    private MinioClient minioClient;
}
```

## 通知事项

您可以通过`MinioClient`实例来处理存储桶中的通知,也可以直接在顶部添加带有`@MinioNotification`的方法.该方法必须在已声明的Spring bean中才能进行处理.

每次从Minio存储桶下载对象时,以下示例都会打印“ Hello world”.

```java
public class Test {
    @MinioNotification({"s3:ObjectAccessed:Get"})
    public void handleGet(NotificationInfo notificationInfo) {
        System.out.println("Hello world");
    }
}
```

为了起作用,您的方法必须仅具有`NotificationInfo`类的一个参数并返回`void`.

## 执行器

启动器向Actuator添加一些指标和运行状况检查,以提供Minio连接状态.

### Metric

Minio客户端上的所有操作都会在Spring Actuator中添加一个指标.默认度量标准名称是`minio.storage`.可以通过设置属性`spring.minio.metric-name`来覆盖.

```json
{
  "name": "minio.storage",
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 0.175
    },
    {
      "statistic": "MAX",
      "value": 0
    }
  ],
  "availableTags": [
    {
      "tag": "bucket",
      "values": [
        "customer-care-api"
      ]
    },
    {
      "tag": "operation",
      "values": [
        "getObject",
        "listObjects",
        "putObject",
        "removeObject"
      ]
    },
    {
      "tag": "status",
      "values": [
        "ko",
        "ok"
      ]
    }
  ]
}
```

清单存储区操作也以`minio.storage.list.bucket`度量标准进行监控.

所有指标均与Prometheus抓取兼容.如果您对Actuator具有Prometheus依赖性,则可以使用以下指标.

```
minio_storage_seconds_count{bucket="customer-care-api",operation="getObject",status="ok",} 1.0
minio_storage_seconds_sum{bucket="customer-care-api",operation="getObject",status="ok",} 0.175
```

然后,您可以通过自己喜欢的监视工具进行请求.例如,要获取每个存储桶上的所有getObject操作 :

```
increase(minio_storage_seconds_count{ operation="getObject" }
```

### 健康检查

附加的运行状况指示器可用于提供Minio连接状态.将启动程序添加到项目中并通过调用管理端点（默认值为`/actuator/health`）后,将显示以下运行状况指示器.

```json
{
  "status": "UP",
  "details": {
    "minio": {
      "status": "UP",
      "details": {
        "bucketName": "00000qweqwe"
      }
    }
  }
}
``` 

通过检查在应用程序属性中参数化的存储桶是否存在来完成运行状况检查.然后,

 * 如果启动应用程序后删除了存储桶,则运行状况为 'DOWN'.
 * 如果无法建立与Minio的连接,则状态将为 'DOWN'.