package com.github.rpc.context.util;

/**
 * @author jianlei.shi
 * @date 2021/3/20 10:02 下午
 * @description StringUtils
 */

public class StringUtils {

    public static String[] split(String orginal) {
        String[] hostMeta = new String[2];
        final String[] s1 = orginal.split(":");
        final String[] s2 = s1[1].split("&");
        hostMeta[0] = s1[0];
        hostMeta[1] = s2[0];
        return hostMeta;
    }

    public static String getVersion(String orginal) {
        final String[] s1 = orginal.split(":");
        final String[] s2 = s1[1].split("&");
        return s2[1];
    }

    public static String cleanLastSymbol(String source){
        final int i = source.lastIndexOf(",");
        final String res = source.substring(0, i);
        return res;
    }

    public static void main(String[] args) {
        String s="123,44,";
        System.out.println(cleanLastSymbol(s));
    }
}
