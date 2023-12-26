package stu.team.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stu.team.constant.constant;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import stu.team.util.IpRegionUtil;
import stu.team.util.Generator;
import stu.team.util.TextComparator;
import stu.team.util.Converter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static stu.team.util.Generator.generatePixelArt;

/**
 * @author crc
 */

@RestController
public class ToolsController {


    /**
     * 上传文件到服务器。
     * @param file 要上传的文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        // 尝试执行文件上传的操作
        try {
            // 检查上传的文件大小是否超过预设的最大值constant.MaxFileSize 表示最大文件大小
            if (file.getSize() > constant.MaxFileSize) {
                // 如果文件大小超过限制，则返回HTTP状态码413（PAYLOAD_TOO_LARGE）
                // 并附带一条错误信息
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("文件大小超过限制: 512MB");
            }

            // 构建目标文件的存储路径
            // constant.BaseUrl 表示基础路径，file.getOriginalFilename() 获取原始文件名
            Path targetLocation = Paths.get(constant.BaseUrl).resolve(file.getOriginalFilename());

            // 将上传的文件内容复制到目标位置
            // file.getInputStream() 获取文件输入流，targetLocation 表示目标路径
            // StandardCopyOption.REPLACE_EXISTING 表示如果目标文件存在，则替换它
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 如果文件上传成功，返回HTTP状态码200（OK）和成功信息
            return ResponseEntity.ok("文件上传成功: " + file.getOriginalFilename());
        } catch (IOException ex) {
            // 捕获处理文件时可能发生的IOException
            // 返回HTTP状态码500（INTERNAL_SERVER_ERROR）和错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("无法上传文件: " + ex.getMessage());
        }
    }


    /**
     * 根据文件名下载文件。
     * @param fileName 要下载的文件名
     * @return 文件资源
     * @throws Exception 文件不存在或不可读时抛出异常
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws Exception {
        Path filePath = Paths.get(constant.BaseUrl).resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new Exception("无法读取文件: " + fileName);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


    /**
     * 通过URL显示图片。
     * @param fileName 要显示的图片文件名
     * @return 图片资源
     */
    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<Resource> viewImage(@PathVariable String fileName) {
        try {
            // 构建图片文件的存储路径
            Path filePath = Paths.get(constant.ImageUrl).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // 检查资源是否存在且可读
            if (!resource.exists() || !resource.isReadable()) {
                System.out.println("Requested file not found or not readable: " + fileName);
                return ResponseEntity.notFound().build();
            }

            // 根据文件扩展名推断内容类型
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
            }
            System.out.println("Serving file: " + fileName + " with content type: " + contentType);

            // 返回图片资源，以及适当的内容类型
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            // 记录异常信息
            System.out.println("Error serving file: " + fileName + "   " + e);
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * 转换温度单位。
     * @param value 要转换的温度值
     * @param unit 当前温度单位（C, F, K, Ra, Re）
     * @return 转换后的所有温度值
     */
    @GetMapping("/temperature/{unit}/{value}")
    public ResponseEntity<Map<String, String>> convertTemperature(@PathVariable("value") double value,
                                                                  @PathVariable("unit") String unit) {
        try {
            // 使用UnitConverter类进行温度单位转换
            Map<String, Double> conversionResult = Converter.convertTemperature(value, unit);

            // 将Double类型的值转换为String类型
            Map<String, String> convertedResult = new HashMap<>();
            for (Map.Entry<String, Double> entry : conversionResult.entrySet()) {
                convertedResult.put(entry.getKey(), String.valueOf(entry.getValue()));
            }

            // 返回转换后的温度值
            return ResponseEntity.ok(convertedResult);
        } catch (Exception ex) {
            // 捕获处理转换时可能发生的异常
            // 返回HTTP状态码500（INTERNAL_SERVER_ERROR）和错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", ex.getMessage()));
        }
    }


    /**
     * 转换长度单位。
     * @param value 要转换的长度值
     * @param unit 当前长度单位
     * @return 转换后的所有长度值
     */
    @GetMapping("/length/{unit}/{value}")
    public ResponseEntity<Map<String, String>> convertLength(@PathVariable("value") double value,
                                                             @PathVariable("unit") String unit) {
        try {
            // 使用UnitConverter类进行长度单位转换
            Map<String, Double> conversionResult = Converter.convertLength(value, unit);

            // 将Double类型的值转换为String类型
            Map<String, String> convertedResult = new HashMap<>();
            for (Map.Entry<String, Double> entry : conversionResult.entrySet()) {
                convertedResult.put(entry.getKey(), String.valueOf(entry.getValue()));
            }

            // 返回转换后的长度值
            return ResponseEntity.ok(convertedResult);
        } catch (Exception ex) {
            // 捕获处理转换时可能发生的异常
            // 返回HTTP状态码500（INTERNAL_SERVER_ERROR）和错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", ex.getMessage()));
        }
    }


    /**
     * 转换质量单位。
     *
     * @param value 要转换的质量值
     * @param unit  当前质量单位
     * @return 转换后的所有质量值
     */
    @GetMapping("/mass/{unit}/{value}")
    public ResponseEntity<Map<String, String>> convertMass(@PathVariable("value") double value,
                                                           @PathVariable("unit") String unit) {
        try {
            // 使用UnitConverter类进行质量单位转换
            Map<String, Double> conversionResult = Converter.convertMass(value, unit);

            // 将Double类型的值转换为String类型
            Map<String, String> convertedResult = new HashMap<>();
            for (Map.Entry<String, Double> entry : conversionResult.entrySet()) {
                convertedResult.put(entry.getKey(), String.valueOf(entry.getValue()));
            }

            // 返回转换后的质量值
            return ResponseEntity.ok(convertedResult);
        } catch (Exception ex) {
            // 捕获处理转换时可能发生的异常
            // 返回HTTP状态码500（INTERNAL_SERVER_ERROR）和错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", ex.getMessage()));
        }
    }


    /**
     * 转换单位。
     *
     * @param value 要转换的速度值
     * @param unit  当前速度单位
     * @return 转换后的所有速度值
     */
    @GetMapping("/speed/{unit}/{value}")
    public ResponseEntity<Map<String, String>> convertSpeed(@PathVariable("value") double value,
                                                            @PathVariable("unit") String unit) {
        try {
            // 使用UnitConverter类进行速度单位转换
            Map<String, Double> conversionResult = Converter.convertSpeed(value, unit);

            // 将Double类型的值转换为String类型
            Map<String, String> convertedResult = new HashMap<>();
            for (Map.Entry<String, Double> entry : conversionResult.entrySet()) {
                convertedResult.put(entry.getKey(), String.valueOf(entry.getValue()));
            }

            // 返回转换后的速度值
            return ResponseEntity.ok(convertedResult);
        } catch (Exception ex) {
            // 捕获处理转换时可能发生的异常
            // 返回HTTP状态码500（INTERNAL_SERVER_ERROR）和错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", ex.getMessage()));
        }
    }


    /**
     * 生成二维码并返回给客户端。
     * @param text 用户提供的文本或URL
     * @return 生成的二维码图像
     */
    @PostMapping(path = "/qrcode", produces = "application/json; charset=UTF-8")
    public ResponseEntity<byte[]> generateQRCode(@RequestParam(value = "text", required = true) String text) {
        // 使用Generator类生成二维码
        System.out.println(text);
        return Generator.generateQRCode(text);
    }




    /**
     * 生成随机、安全的密码。
     * @param length 密码长度
     * @param includeUppercase 是否包含大写字母
     * @param includeLowercase 是否包含小写字母
     * @param includeNumbers 是否包含数字
     * @param includeSpecial 是否包含特殊字符
     * @return 生成的随机密码
     */
    @PostMapping("/password")
    public ResponseEntity<String> generatePassword(
            @RequestParam(value = "length", defaultValue = "12") int length,
            @RequestParam(value = "includeUppercase", defaultValue = "true") boolean includeUppercase,
            @RequestParam(value = "includeLowercase", defaultValue = "true") boolean includeLowercase,
            @RequestParam(value = "includeNumbers", defaultValue = "true") boolean includeNumbers,
            @RequestParam(value = "includeSpecial", defaultValue = "true") boolean includeSpecial) {
        try {
            // 使用Generator类生成密码
            String password = Generator.generatePassword(
                    length, includeUppercase, includeLowercase, includeNumbers, includeSpecial);
            return ResponseEntity.ok(password);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /**
     * 生成像素风格的图像并返回给客户端。
     *
     * @param originalImage 原始图像的字节数组
     * @param pixelSize     像素的尺寸
     * @return 包含像素风格图像的字节数组
     */
    @PostMapping(path = "/pixel", produces = "image/png")
    public ResponseEntity<byte[]> generatePixelImage(@RequestParam("image") MultipartFile originalImage,
                                                     @RequestParam("pixelSize") int pixelSize) {
        try {
            // 检查上传的文件是否为空。如果文件为空，则返回400状态码和错误消息。
            if (originalImage.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No image file provided.".getBytes());
            }

            // 将上传的图像文件转换为字节数组，以便进一步处理。
            byte[] imageBytes = originalImage.getBytes();
            ByteArrayInputStream imageStream = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(imageStream);

            // 调用下面的方法生成像素风格的图像。
            BufferedImage pixelArtImage = generatePixelArt(bufferedImage, pixelSize);

            // 将生成的像素风格图像转换为字节数组，以便作为响应返回。
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(pixelArtImage, "png", outputStream);

            return ResponseEntity.ok().body(outputStream.toByteArray());
        } catch (IOException ex) {
            // 在处理图像或发送响应时捕获并处理IO异常。
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Error generating pixel art: " + ex.getMessage()).getBytes());
        }
    }


    /**
     * 计算两个日期之间的差异。  ISO日期格式  2023-01-15
     * @param startDate 起始日期
     * @param endDate 结束日期
     * @return 两个日期之间的差异，以天、小时和分钟表示
     */
    @GetMapping("/date")
    public ResponseEntity<String> calculateDateDifference(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (endDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().body("结束日期应该在开始日期之后。");
        }

        Period period = Period.between(startDate, endDate);
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

        return ResponseEntity.ok(String.format("总天数: %d, 年: %d, 月: %d, 日: %d",
                daysBetween, period.getYears(), period.getMonths(), period.getDays()));
    }

    /**
     * 在日期上添加或减去特定的时间量。
     * @param date 基准日期
     * @param yearsToAddOrSubtract 要添加或减去的年数
     * @param monthsToAddOrSubtract 要添加或减去的月数
     * @param daysToAddOrSubtract 要添加或减去的天数
     * @return 计算后的日期
     */
    @PostMapping("/date")
    public ResponseEntity<String> modifyDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "years", defaultValue = "0") int yearsToAddOrSubtract,
            @RequestParam(value = "months", defaultValue = "0") int monthsToAddOrSubtract,
            @RequestParam(value = "days", defaultValue = "0") int daysToAddOrSubtract) {

        LocalDate modifiedDate = date.plusYears(yearsToAddOrSubtract)
                .plusMonths(monthsToAddOrSubtract)
                .plusDays(daysToAddOrSubtract);

        return ResponseEntity.ok("新日期: " + modifiedDate.toString());
    }


    /**
     * 对比两段文本的差异。
     * @param originalText 原始文本
     * @param comparedText 要对比的文本
     * @return 对比结果
     */
    @PostMapping("/text")
    public ResponseEntity<String> compareText(
            @RequestParam("originalText") String originalText,
            @RequestParam("comparedText") String comparedText) {
        // 尝试执行文本对比操作
        try {
            // 使用某种算法或库来对比文本差异
            // 这里假设有一个TextComparator.compare方法可以使用
            String diff = TextComparator.compare(originalText, comparedText);

            // 如果没有差异，则返回HTTP状态码200（OK）和无差异的信息
            if (diff.isEmpty()) {
                return ResponseEntity.ok("文本之间没有差异。");
            }

            // 如果存在差异，返回HTTP状态码200（OK）和差异信息
            return ResponseEntity.ok("文本差异: " + diff);
        } catch (Exception ex) {
            // 捕获处理文本对比时可能发生的异常
            // 返回HTTP状态码500（INTERNAL_SERVER_ERROR）和错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("无法对比文本: " + ex.getMessage());
        }
    }


    /**
     * 查询用户的公网IP地址并提供地理位置查询功能。
     * @param request HTTP请求
     * @return IP地址和地理位置信息
     */
    @GetMapping("/ip")
    public ResponseEntity<String> getIpLocation(HttpServletRequest request) {
        try {
            // 获取用户的公网IP地址
            String ipAddress = request.getRemoteAddr();

            System.out.println(ipAddress);

            // 使用IpRegionUtil工具类查询IP地址的地理位置
            String location = IpRegionUtil.queryRegionByIp(ipAddress);

            // 构建并返回IP地址和地理位置信息
            String response = "用户IP地址: " + ipAddress + "\n地理位置: " + location;
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            // 捕获处理IP查询时可能发生的异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("无法获取IP地址和地理位置: " + ex.getMessage());
        }
    }



    /**
     * @description:对文件进行MD5、SHA-256和SHA-3哈希转换。
     * @param file 要转换的文件
     * @param file
     * @return 哈希转换结果
     */
    @PostMapping("/hash")
    public ResponseEntity<Map<String, String>> hashFile(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, String> hashes = new HashMap<>();
            byte[] fileContent = file.getBytes();

            hashes.put("MD5", Converter.calculateHash(fileContent, "MD5"));
            hashes.put("SHA-256", Converter.calculateHash(fileContent, "SHA-256"));
            hashes.put("SHA-3", Converter.calculateHash(fileContent, "SHA3-256"));

            return ResponseEntity.ok(hashes);
        } catch (IOException | NoSuchAlgorithmException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "无法计算哈希值: " + ex.getMessage()));
        }
    }

    
    
    /**
     * @description:对文本进行MD5、SHA-256和SHA-3哈希转换。
     * @param text
     * @return org.springframework.http.ResponseEntity<java.util.Map<java.lang.String,java.lang.String>>
     */
    @PostMapping("/hashText")
    public ResponseEntity<Map<String, String>> hashText(@RequestParam("text") String text) {
        try {
            Map<String, String> hashes = new HashMap<>();
            byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);

            hashes.put("MD5", Converter.calculateHash(textBytes, "MD5"));
            hashes.put("SHA-256", Converter.calculateHash(textBytes, "SHA-256"));
            hashes.put("SHA-3", Converter.calculateHash(textBytes, "SHA3-256"));

            return ResponseEntity.ok(hashes);
        } catch (NoSuchAlgorithmException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "无法计算哈希值: " + ex.getMessage()));
        }
    }



























    /**
     * 网速测试 - 检测用户的下载和上传速度。
     * @param file 要上传的文件
     * @return 速度测试结果
     */
//    @PostMapping("/speed_test")
//    public ResponseEntity<String> performSpeedTest(@RequestParam("file") MultipartFile file) {
//        try {
//            // 记录开始时间
//            long startTime = System.nanoTime();
//
//            // 将上传的文件内容复制到目标位置
//            Path targetLocation = Paths.get(constant.BaseUrl).resolve(file.getOriginalFilename());
//            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
//
//            // 记录结束时间
//            long endTime = System.nanoTime();
//            long elapsedTime = endTime - startTime;
//
//            // 获取文件大小（字节）
//            long fileSize = Files.size(targetLocation);
//
//            // 将纳秒转换为毫秒，并计算速度（字节/毫秒）
//            double speedBytesPerMillis = (double) fileSize / TimeUnit.NANOSECONDS.toMillis(elapsedTime);
//
//            // 将速度转换为兆字节/秒
//            double speedMegabytesPerSecond = speedBytesPerMillis / (1024 * 1024);
//
//            // 返回速度测试结果
//            return ResponseEntity.ok("上传成功，速度：" + speedMegabytesPerSecond + " MB/s");
//        } catch (IOException ex) {
//            // 捕获处理文件时可能发生的IOException
//            // 返回HTTP状态码500（INTERNAL_SERVER_ERROR）和错误信息
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("无法上传文件: " + ex.getMessage());
//        }
//    }

}
