package stu.team.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.lionsoul.ip2region.xdb.Searcher;

public class IpRegionUtil {

    private static Searcher searcher;

    static {
        try {
            // 使用InputStream读取资源
            InputStream is = IpRegionUtil.class.getResourceAsStream("/ip2region/ip2region.xdb");
            Path tempFilePath = Files.createTempFile("ip2region", ".xdb");
            Files.copy(is, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            searcher = Searcher.newWithFileOnly(tempFilePath.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据IP地址查询地理位置。
     * @param ip IP地址
     * @return 地理位置信息
     */
    public static String queryRegionByIp(String ip) {
        try {
            // 执行查询
            String region = searcher.search(ip);
            return region;
        } catch (Exception e) {
            e.printStackTrace();
            return "查询地理位置失败";
        }
    }
}