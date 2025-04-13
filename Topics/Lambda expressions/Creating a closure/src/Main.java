import java.util.function.UnaryOperator;

class PrefixSuffixOperator {

    public static final String PREFIX = "__pref__";
    public static final String SUFFIX = "__suff__";

    public static UnaryOperator<String> operator = (x) -> {
        String removedSpaces = x.stripLeading().stripTrailing();

        return PREFIX + removedSpaces + SUFFIX;

    };// write your code here
}