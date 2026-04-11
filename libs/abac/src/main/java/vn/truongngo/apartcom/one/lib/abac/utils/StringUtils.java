package vn.truongngo.apartcom.one.lib.abac.utils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * String utility
 * @author Truong Ngo
 */
public class StringUtils {

    public static final String EMAIL_REGEX = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";

    public static final String ALPHANUMERIC_REGEX = "[a-zA-Z0-9]+";

    public static final String URL_REGEX_PREFIX = "^(?i)(%s):\\/\\/[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*$";

    public static final String COLLECTION_ACCESS_REGEX = "^+\\[(\\d+)]$";

    public static final String PATH_VARIABLE_REGEX = ".*\\{[^}]+}.*";

    public static final String PATH_VARIABLE_REGEX_W_GROUP = "\\{([^}]+)}";


    public static boolean isTrimEmpty(String s) {
        return Objects.isNull(s) || s.trim().isEmpty();
    }

    public static <T> String join(List<T> list, String delimiter, Function<T, String> mapper) {
        if (CollectionUtils.isNullOrEmpty(list)) return null;
        return list.stream().map(mapper).collect(Collectors.joining(delimiter));
    }

    public static <T> List<T> split(String s, String delimiter, Function<String, T> mapper) {
        if (isTrimEmpty(s)) return Collections.emptyList();
        return Stream.of(s.split(delimiter)).map(mapper).collect(Collectors.toList());
    }

    public static String nonEmptyValue(String s) {
        return isTrimEmpty(s) ? null : s;
    }

    public static String nvl(String s) {
        return isTrimEmpty(s) ? "" : s;
    }

    public static boolean isEmail(String s) {
        return Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE).matcher(s).matches();
    }

    public static boolean isAlphanumeric(String s) {
        return s.matches(ALPHANUMERIC_REGEX);
    }

    public static boolean isUrl(String s, List<String> schemes) {
        String scheme = String.join("|", schemes);
        String regex = String.format(URL_REGEX_PREFIX, scheme);
        return s.matches(regex);
    }

    public static String capitalizeFirstLetter(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static int extractIndex(String s) {
        Pattern pattern = Pattern.compile(COLLECTION_ACCESS_REGEX);
        Matcher matcher = pattern.matcher(s);
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    public static boolean matchUrlPath(String template, String actualPath) {
        String regex = template.replaceAll(PATH_VARIABLE_REGEX_W_GROUP, "(?<$1>[^/]+)");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(actualPath);
        return matcher.matches();
    }

    public static boolean hasPathVariable(String template) {
        Pattern pattern = Pattern.compile(PATH_VARIABLE_REGEX);
        Matcher matcher = pattern.matcher(template);
        return matcher.matches();
    }
}
