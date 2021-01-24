package wiki.heh.minio.api.enums;

/**
 * @author hehua
 * @date 2020/11/10
 */
public enum Image {
    WATERMARK("watermark"),
    ALL("all"),
    COMPRESSION("compression"),
    NULL("null");

    Image(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isWatermark() {
        return getValue().equals("watermark");
    }

    public boolean isCompression() {
        return getValue().equals("compression");
    }

    public boolean isAll() {
        return getValue().equals("all");
    }

    private String value;
}
