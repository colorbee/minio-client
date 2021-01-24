package wiki.heh.minio.api.util;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import wiki.heh.minio.api.config.WatermarkConfiguration;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

/**
 * 图像工具类
 *
 * @author hehua
 * @date 2020/11/10
 */
public class ImageUtil {
    //    private static final Logger log = LoggerFactory.getLogger(ImageUtil.class);
//    //压缩率
//    private static final transient float IMAGE_RATIO = 0.1f;
//    //压缩最大宽度
//    private static final transient int IMAGE_WIDTH = 800;
//    // 水印透明度
//    private static float alpha = 0.3f;
//    // 水印文字字体
//    private static Font font = new Font("PingFang SC Regular", Font.PLAIN, 36);
//    // 水印文字颜色
//    private static Color color = new Color(111, 111, 111);
//    //水印文字内容
//    private static final String text = "智慧式-伴置车";
//    // 水印之间的间隔
//    private static final int XMOVE = 80;
//    // 水印之间的间隔
//    private static final int YMOVE = 80;

    private final WatermarkConfiguration conf;

    public ImageUtil(WatermarkConfiguration conf) {
        this.conf = conf;
    }

    /**
     * 压缩图像
     *
     * @param image
     * @return
     * @throws IOException
     */
    public BufferedImage compress(BufferedImage image) throws IOException {
        Thumbnails.Builder<BufferedImage> imageBuilder = Thumbnails.of(image).outputQuality(conf.getImageRatio());
        if (image.getWidth() > conf.getImageWidth()) {
            return imageBuilder.width(conf.getImageWidth()).asBufferedImage();
        } else {
            return imageBuilder.scale(1).asBufferedImage();
        }
    }

    /**
     * 图像添加水印
     *
     * @param
     * @return
     */
    public BufferedImage setWatermark(BufferedImage image) throws IOException {
        return Thumbnails.of(image)
                .outputQuality(conf.getImageRatio())
                .scale(1)
                .watermark(Positions.BOTTOM_RIGHT
                        , createWatermark(conf.getText()
                                , image.getWidth()
                                , image.getHeight()
                        )
                        , conf.getAlpha())
                .asBufferedImage();
    }

    /**
     * 根据文件扩展名判断文件是否图片格式
     *
     * @return
     */
    public boolean isImage(String fileName) {
        String[] imageExtension = new String[]{"jpeg", "jpg", "gif", "bmp", "png"};

        for (String e : imageExtension) if (getFileExtention(fileName).toLowerCase().equals(e)) return true;

        return false;
    }

    /**
     * 获取文件后缀名称
     *
     * @param fileName
     * @return
     */
    public String getFileExtention(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return extension;
    }

    /**
     * 根据图片对象获取对应InputStream
     *
     * @param image
     * @param readImageFormat
     * @return
     * @throws IOException
     */
    public InputStream getInputStream(BufferedImage image, String readImageFormat) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, readImageFormat, os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        os.close();
        return is;
    }

    /**
     * 创建水印图片
     *
     * @param text   水印文字
     * @param width  图片宽
     * @param height 图片高
     * @return
     */
    public BufferedImage createWatermark(String text, int width, int height) {
        BufferedImage image = new BufferedImage(width
                , height, BufferedImage.TYPE_INT_RGB);
        // 2.获取图片画笔
        Graphics2D g = image.createGraphics();
        // ----------  增加下面的代码使得背景透明  -----------------
        image = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        g.dispose();
        g = image.createGraphics();
        // ----------  背景透明代码结束  -----------------
        // 6、处理文字
        AttributedString ats = new AttributedString(text);
        ats.addAttribute(TextAttribute.FONT, conf.getFont(), 0, text.length());
        AttributedCharacterIterator iter = ats.getIterator();
        // 7、设置对线段的锯齿状边缘处理
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 8、设置水印旋转
        g.rotate(Math.toRadians(-30));
        // 9、设置水印文字颜色
        g.setColor(conf.getColor());
        // 10、设置水印文字Font
        g.setFont(conf.getFont());
        // 11、设置水印文字透明度
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, conf.getAlpha()));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        /**
         * 水印铺满图片
         * 计算水印位置
         */
        int x = -width / 2;
        int y = -height / 2;
        int[] arr = getWidthAndHeight(text, conf.getFont());
        int markWidth = arr[0];// 字体长度
        int markHeight = arr[1];// 字体高度
        // 循环添加水印
        while (x < width * 1.5) {
            y = -height / 2;
            while (y < height * 1.5) {
                g.drawString(text, x, y);

                y += markHeight + conf.getyMove();
            }
            x += markWidth + conf.getxMove();
        }
        // 13、释放资源
        g.dispose();
        return image;
    }

    /**
     * 计算字体宽度及高度
     *
     * @param text
     * @param font
     * @return
     */
    private int[] getWidthAndHeight(String text, Font font) {
        Rectangle2D r = font.getStringBounds(text, new FontRenderContext(
                AffineTransform.getScaleInstance(1, 1), false, false));
        int unitHeight = (int) Math.floor(r.getHeight());//
        // 获取整个str用了font样式的宽度这里用四舍五入后+1保证宽度绝对能容纳这个字符串作为图片的宽度
        int width = (int) Math.round(r.getWidth()) + 1;
        // 把单个字符的高度+3保证高度绝对能容纳字符串作为图片的高度
        int height = unitHeight + 3;
        return new int[]{width, height};
    }
}
