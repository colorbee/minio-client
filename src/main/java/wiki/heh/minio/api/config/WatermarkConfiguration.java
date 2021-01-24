package wiki.heh.minio.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.awt.*;

/**
 * @author hehua
 * @date 2020/11/10
 */
@Configuration
@ConfigurationProperties("spring.minio.watermark")
public class WatermarkConfiguration {
    //压缩率
    private float   imageRatio  = 0.1f;
    //压缩最大宽度
    private int     imageWidth  = 800;
    // 水印透明度
    private float   alpha       = 0.3f;
    // 水印文字字体
    private int     fontSize    = 36;
    private String  fontName    = "PingFang SC Regular";
    private Font    font        = new Font(fontName, Font.PLAIN, fontSize);
    // 水印文字颜色"red", "green", "blue
    private int     colorRed    = 111;
    private int     colorGreen  = 111;
    private int     colorBlue   = 111;
    private Color   color       = new Color(colorRed, colorGreen, colorBlue);
    //水印文字内容
    private String  text        = "智慧式-伴置车";
    // 水印之间的间隔
    private int     xMove       = 80;
    // 水印之间的间隔
    private int     yMove       = 80;

    public float getImageRatio() {
        return imageRatio;
    }

    public void setImageRatio(float imageRatio) {
        this.imageRatio = imageRatio;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public Font getFont() {
        return font;
    }

//    public void setFont(Font font) {
//        this.font = font;
//    }

    public int getColorRed() {
        return colorRed;
    }

    public void setColorRed(int colorRed) {
        this.colorRed = colorRed;
    }

    public int getColorGreen() {
        return colorGreen;
    }

    public void setColorGreen(int colorGreen) {
        this.colorGreen = colorGreen;
    }

    public int getColorBlue() {
        return colorBlue;
    }

    public void setColorBlue(int colorBlue) {
        this.colorBlue = colorBlue;
    }

    public Color getColor() {
        return color;
    }

//    public void setColor(Color color) {
//        this.color = color;
//    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getxMove() {
        return xMove;
    }

    public void setxMove(int xMove) {
        this.xMove = xMove;
    }

    public int getyMove() {
        return yMove;
    }

    public void setyMove(int yMove) {
        this.yMove = yMove;
    }
}
