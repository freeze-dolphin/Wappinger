package io.sn.wappinger.utils;

public class StringUtils {

    /**
     * @return java.lang.String
     * @author FeianLing
     * @date 2019/12/10
     * @desc 半角转全角
     */
    public static String toSBC(String val) {
        char[] chars = val.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ' ') {
                chars[i] = '\u3000';
            } else if (chars[i] < '\177') {
                chars[i] = (char) (chars[i] + 65248);
            }
        }
        return new String(chars);
    }

}
