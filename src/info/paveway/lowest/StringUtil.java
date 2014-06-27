package info.paveway.lowest;

public class StringUtil {

    public static boolean isNullOrEmpty(String src) {
        return ((null == src) || "".equals(src)) ? true : false;
    }

    public static boolean isNotNullOrEmpty(String src) {
        return !isNullOrEmpty(src);
    }
}
