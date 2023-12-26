package stu.team.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import stu.team.constant.constant;

/**
 * @author crc
 */

@RestController
@RequestMapping("/api")
public class APIController {
    @Autowired

    private RestTemplate restTemplate;

    /**
     * 使用 SauceNAO API 搜索图像的来源。
     * @param imageUrl 要搜索的图像的URL
     * @return SauceNAO API 的响应
     */
    @GetMapping("/image")
    public ResponseEntity<?> searchImageWithSauceNAO(@RequestParam("url") String imageUrl) {
        try {
            // 将图像URL编码以包含在查询字符串中
//            String encodedImageUrl = URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.toString());

            // 构建SauceNAO API的URL
            String saucenaoUrl = "https://saucenao.com/search.php?db=999&output_type=2&testmode=1&numres=1&api_key="+ constant.SauceNAO_API+"&url=" + imageUrl;

            // 发送GET请求
            ResponseEntity<String> response = restTemplate.getForEntity(saucenaoUrl, String.class);

            // 返回SauceNAO API的响应
            return ResponseEntity.ok(response.getBody());
        } catch (Exception ex) {
            // 如果发生错误，返回错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error searching image with SauceNAO: " + ex.getMessage());
        }
    }


    /**
     * 使用 SauceNAO API 通过图片进行搜索。
     * @param imageFile 上传的图片文件
     * @return SauceNAO API 的响应
     */
    @PostMapping("/image")
    public ResponseEntity<?> searchImageWithSauceNAO(@RequestParam("image") MultipartFile imageFile) {
        try {
            // 检查上传的文件是否为空
            if (imageFile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No image file provided.");
            }

            // 将图片文件转换为字节数组
            byte[] imageBytes = imageFile.getBytes();

            // 构建用于发送POST请求的HttpHeaders和HttpEntity
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 构建MultiValueMap以包含请求所需的参数和文件
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("db", "999");
            body.add("output_type", "2");
            body.add("testmode", "1");
            body.add("numres", "1");
            body.add("api_key", constant.SauceNAO_API);
            body.add("file", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename(); // 使用原始文件名
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 构建SauceNAO API的URL
            String saucenaoUrl = "https://saucenao.com/search.php";

            // 发送POST请求
            ResponseEntity<String> response = restTemplate.postForEntity(saucenaoUrl, requestEntity, String.class);

            // 返回SauceNAO API的响应
            return ResponseEntity.ok(response.getBody());
        } catch (Exception ex) {
            // 如果发生错误，返回错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error searching image with SauceNAO: " + ex.getMessage());
        }
    }


//    无法使用的第三方api

//    /**
//     * 调用外部API以将图片URL转换为Base64编码。https://www.free-api.com/doc/619
//     * @param url 图片的网络链接地址
//     * @param lang 语言类型，例如 zh-cn、ru-ru（可选）
//     * @param cache 是否获取缓存数据（可选）
//     * @return 转换后的Base64图片编码
//     */
//    @GetMapping("/base64")
//    public ResponseEntity<String> convertImageToBase64(
//            @RequestParam("url") String url,
//            @RequestParam(value = "lang", required = false) String lang,
//            @RequestParam(value = "cache", required = false) Boolean cache) {
//
//        // 构建请求URL，包括必要的查询参数
//        String requestUrl = String.format("https://test.harumoe.cn/api/other/base64?url=%s", url);
//
//        // 如果提供了语言和缓存参数，添加到请求URL
//        if (lang != null) {
//            requestUrl += "&lang=" + lang;
//        }
//        if (cache != null) {
//            requestUrl += "&cache=" + cache;
//        }
//
//        try {
//            // 使用RestTemplate调用外部API
//            String response = restTemplate.getForObject(requestUrl, String.class);
//
//            // 如果API调用成功，返回Base64编码的图片
//            return ResponseEntity.ok(response);
//        } catch (Exception ex) {
//            // 捕获处理API调用时可能发生的异常
//            // 返回HTTP状态码500（INTERNAL_SERVER_ERROR）和错误信息
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("API调用失败: " + ex.getMessage());
//        }
//    }
//
//
//    /**
//     * 调用外部api检测域名是否被拦截。https://www.free-api.com/doc/535
//     * @param url 图片的网络链接地址
//     * @return 是否拦截
//     */
//    @GetMapping("/intercept")
//    public ResponseEntity<String> isIntercept(@RequestParam("url") String url){
//
//        // 构建请求URL，包括必要的查询参数
//        String requestUrl = String.format("https://test.harumoe.cn/api/other/base64?url=%s", url);
//
//        try {
//            // 使用RestTemplate调用外部API
//            String response = restTemplate.getForObject(requestUrl, String.class);
//
//            // 如果API调用成功，返回结果
//            return ResponseEntity.ok(response);
//        } catch (Exception ex) {
//            // 捕获处理API调用时可能发生的异常
//            // 返回HTTP状态码500（INTERNAL_SERVER_ERROR）和错误信息
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("API调用失败: " + ex.getMessage());
//        }
//    }
//
//    /**
//     * 使用植物识别API识别植物。
//     * @param file 用户上传的图片文件
//     * @return API返回的识别结果
//     */
//    @PostMapping("/recognize-plant")
//    public String recognizePlant(@RequestParam("file") MultipartFile file) {
//        try {
//            // 将文件转换为Base64编码
//            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
//            // 对Base64编码进行URL编码
//            String encodedImage = URLEncoder.encode(base64Image, StandardCharsets.UTF_8.toString());
//
//            // 构建请求URL和参数
//            String url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/plant?access_token=YOUR_ACCESS_TOKEN";
//            String body = "image=" + encodedImage;
//
//            // 发送POST请求
//            return restTemplate.postForObject(url, body, String.class);
//        } catch (Exception e) {
//            return "Error: " + e.getMessage();
//        }
//    }

}
