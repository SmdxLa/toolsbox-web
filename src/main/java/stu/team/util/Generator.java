package stu.team.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class Generator {


//    二维码生成
    public static ResponseEntity<byte[]> generateQRCode(String text) {
        // 设置二维码的宽度和高度
        int width = 500;
        int height = 500;

        // 创建QRCodeWriter对象
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            // 定义二维码的参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // 生成二维码的位矩阵
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            // 将位矩阵转换为字节数组
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", byteArrayOutputStream);

            // 创建字节数组
            byte[] qrImage = byteArrayOutputStream.toByteArray();

            // 返回二维码图像
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
        } catch (WriterException | IOException e) {
            // 捕获和处理异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    //    密码生成
    public static String generatePassword(int length, boolean includeUppercase, boolean includeLowercase,
                                          boolean includeNumbers, boolean includeSpecial) {
        // 定义密码字符集
        String upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseChars = "abcdefghijklmnopqrstuvwxyz";
        String numberChars = "0123456789";
        String specialChars = "!@#$%^&*()_+-=[]{}|;':\",.<>/?";

        // 根据用户选择构建字符集
        StringBuilder characterSet = new StringBuilder();
        if (includeUppercase) {
            characterSet.append(upperCaseChars);
        }
        if (includeLowercase) {
            characterSet.append(lowerCaseChars);
        }
        if (includeNumbers) {
            characterSet.append(numberChars);
        }
        if (includeSpecial) {
            characterSet.append(specialChars);
        }

        // 使用SecureRandom生成安全的随机密码
        SecureRandom random = new SecureRandom();
        if (characterSet.length() == 0) {
            throw new IllegalArgumentException("至少选择一种字符类型");
        }

        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characterSet.length());
            password.append(characterSet.charAt(randomIndex));
        }

        return password.toString();
    }


    /**
     * 生成像素风格的图像。
     *
     * 此方法通过修改原始图像的像素，生成具有指定像素尺寸的像素风格图像。
     *
     * @param originalImage 原始图像
     * @param pixelSize     像素的尺寸
     * @return 像素风格的图像
     */
    public static BufferedImage generatePixelArt(BufferedImage originalImage, int pixelSize) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 创建一个新的图像，用于存储像素风格的结果。
        BufferedImage pixelArtImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 遍历原始图像的每个像素。
        for (int x = 0; x < width; x += pixelSize) {
            for (int y = 0; y < height; y += pixelSize) {
                // 获取当前像素的颜色。
                int rgb = originalImage.getRGB(x, y);
                Color color = new Color(rgb);

                // 将当前像素的颜色应用到周围的像素上，以创建大块的像素效果。
                for (int i = 0; i < pixelSize; i++) {
                    for (int j = 0; j < pixelSize; j++) {
                        int newX = x + i;
                        int newY = y + j;

                        // 确保新的坐标在图像范围内。
                        if (newX < width && newY < height) {
                            pixelArtImage.setRGB(newX, newY, color.getRGB());
                        }
                    }
                }
            }
        }

        return pixelArtImage;
    }
}
