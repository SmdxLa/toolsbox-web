package stu.team.util;

import jakarta.xml.bind.DatatypeConverter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Converter {

    /**
     * 温度单位转换方法
     *
     * @param value
	 * @param fromUnit
     * @return java.util.Map<java.lang.String,java.lang.Double>
     */
    public static Map<String, Double> convertTemperature(double value, String fromUnit) {
        try {
            double celsius = 0;
            boolean isUnitValid = true;

            // 首先将输入温度转换为摄氏度
            switch (fromUnit.toUpperCase()) {
                case "C": // 摄氏度
                    celsius = value;
                    break;
                case "F": // 华氏度
                    celsius = (value - 32) * 5/9;
                    break;
                case "K": // 开氏度
                    celsius = value - 273.15;
                    break;
                case "RA": // 兰氏度
                    celsius = (value - 491.67) * 5/9;
                    break;
                case "RE": // 列氏度
                    celsius = value * 5/4;
                    break;
                default:
                    isUnitValid = false;
                    break;
            }

            if (!isUnitValid) {
                // 如果提供了无效的温度单位，返回空Map
                return Collections.emptyMap();
            }

            // 然后将摄氏度转换为所有其他单位
            Map<String, Double> result = new HashMap<>();
            result.put("Celsius", celsius);
            result.put("Fahrenheit", celsius * 9/5 + 32);
            result.put("Kelvin", celsius + 273.15);
            result.put("Rankine", celsius * 9/5 + 491.67);
            result.put("Reaumur", celsius * 4/5);

            return result;
        } catch (Exception ex) {
            // 捕获处理转换时可能发生的异常，返回空Map
            return Collections.emptyMap();
        }
    }


    // 长度单位转换方法
    /**
     * 转换单位。
     *
     * @param value    要转换的值
     * @param fromUnit 当前单位
     * @return 转换后的所有值
     */
    public static Map<String, Double> convertLength(double value, String fromUnit) {
        // 将输入单位转换为标准单位（米）
        double meters = convertToMeters(value, fromUnit);

        // 将标准单位（米）转换为其他长度单位
        Map<String, Double> result = new HashMap<>();
        result.put("Meters", meters);
        result.put("Kilometers", meters / 1000);
        result.put("Centimeters", meters * 100);
        result.put("Millimeters", meters * 1000);
        result.put("Micrometers", meters * 1_000_000);
        result.put("Nanometers", meters * 1_000_000_000);
        result.put("Inches", meters / 0.0254);
        result.put("Feet", meters / 0.3048);
        result.put("Yards", meters / 0.9144);
        result.put("Miles", meters / 1609.344);

        return result;
    }

    /**
     * 转换为米
     *
     * @param value
     * @param fromUnit
     * @return double
     */
    private static double convertToMeters(double value, String fromUnit) {
        switch (fromUnit.toUpperCase()) {
            case "M":
                return value;
            case "KM":
                return value * 1000;
            case "CM":
                return value / 100;
            case "MM":
                return value / 1000;
            case "UM":
                return value / 1_000_000;
            case "NM":
                return value / 1_000_000_000;
            case "INCHES":
                return value * 0.0254;
            case "FEET":
                return value * 0.3048;
            case "YARDS":
                return value * 0.9144;
            case "MILES":
                return value * 1609.344;
            default:
                throw new IllegalArgumentException("无效的长度单位: " + fromUnit);
        }
    }


    /**
     * 转换单位。
     *
     * @param value    要转换的值
     * @param fromUnit 当前单位
     * @return 转换后的所有值
     */
    public static Map<String, Double> convertMass(double value, String fromUnit) {
        // 将输入单位转换为标准单位（千克）
        double kilograms = convertToKilograms(value, fromUnit);

        // 将标准单位（千克）转换为其他质量单位
        Map<String, Double> result = new HashMap<>();
        result.put("Kilograms", kilograms);
        result.put("Grams", kilograms * 1000);
        result.put("Milligrams", kilograms * 1_000_000);
        result.put("Micrograms", kilograms * 1_000_000_000);
        result.put("Pounds", kilograms * 2.20462);
        result.put("Ounces", kilograms * 35.274);

        return result;
    }

    /**
     *转换单位为千克
     *
     * @param value
	 * @param fromUnit
     * @return double
     */
    private static double convertToKilograms(double value, String fromUnit) {
        switch (fromUnit.toUpperCase()) {
            case "KG":
                return value;
            case "G":
                return value / 1000;
            case "MG":
                return value / 1_000_000;
            case "UG":
                return value / 1_000_000_000;
            case "LB":
                return value * 0.453592;
            case "OZ":
                return value * 0.0283495;
            default:
                throw new IllegalArgumentException("无效的质量单位: " + fromUnit);
        }
    }


    /**
     * 转换单位。
     *
     * @param value    要转换的值
     * @param fromUnit 当前单位
     * @return 转换后的所有值
     */
    public static Map<String, Double> convertSpeed(double value, String fromUnit) {
        // 将输入单位转换为标准单位（米每秒）
        double metersPerSecond = convertToMetersPerSecond(value, fromUnit);

        // 将标准单位（米每秒）转换为其他速度单位
        Map<String, Double> result = new HashMap<>();
        result.put("MetersPerSecond", metersPerSecond);
        result.put("KilometersPerHour", metersPerSecond * 3.6);
        result.put("FeetPerSecond", metersPerSecond * 3.28084);
        result.put("MilesPerHour", metersPerSecond * 2.23694);
        result.put("Knots", metersPerSecond / 0.514444);

        return result;
    }


    /**
     * 转换单位为米每秒
     *
     * @param value
	 * @param fromUnit
     * @return double
     */
    private static double convertToMetersPerSecond(double value, String fromUnit) {
        switch (fromUnit.toUpperCase()) {
            case "MPS":
                return value;
            case "KMPH":
                return value / 3.6;
            case "FTPS":
                return value / 3.28084;
            case "MPH":
                return value / 2.23694;
            case "KNOTS":
                return value * 0.514444;
            default:
                throw new IllegalArgumentException("无效的速度单位: " + fromUnit);
        }
    }



    /**
     * @description:接受字节数组和哈希算法名称作为参数，并返回计算得到的哈希值。
     * @param inputBytes
	 * @param algorithm
     * @return java.lang.String
     */
    public static String calculateHash(byte[] inputBytes, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hashBytes = digest.digest(inputBytes);
        return DatatypeConverter.printHexBinary(hashBytes).toUpperCase();
    }


}
