package org.nmdp.service.epitope.gl.transform;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Erik Pearson
 */
public class GlStringFunctions {

//    public static final Pattern ALLELE_PATTERN = Pattern.compile(
//            //"(?:(?<prefix>[^/~+|^*]*)\\*)?(?<allele>\\d+:\\d+(?::[A-Z0-9]+)*)(?=$|[/~+|^])"
//            "(?:(?<prefix>[^/~+|^*]*)\\*)?(?<allele>(?<protein>\\d+:\\d+)(?<synonym>(?::[A-Z0-9]+)*))");

    public static final Pattern ALLELE_PROTEIN_PATTERN = Pattern.compile(
            "(?<protein>(?:[^/~+|^*]*\\*)?\\d+:\\d+)(?<pgroup>P?)(?<synonym>(?::\\p{Alnum}+)*)");

    public static final Pattern ALLELE_CODE_PATTERN = Pattern.compile(
            "(?<prefix>[^/~+|^*]+\\*)(?<allele>\\d+:\\p{Alpha}+)");

    private static final Pattern ALLELE_PATTERN = Pattern.compile(
            "(?<prefix>[^/~+|^*]+\\*)(?<allele>\\d+(?::\\p{Alnum}+)+)");

    private static final Pattern ALLELE_PREFIX_PATTERN = Pattern.compile(
            "(?:(HLA-)?(?<shortPrefix>[^/~+|^*]+\\*))?(?<allele>\\d+(?::\\p{Alnum}+)*)");

    private static final Pattern ALLELE_FIELD_PATTERN = Pattern.compile(
            "(?<field>[*:]\\d+)(?:\\p{Alpha}?)");

    public static Function<String, String> applyToAlleles(Function<String, String> alleleTransformer) {
        return new GlStringAlleleTransformer(alleleTransformer);
    }

//    private static final Pattern ALLELE_SUFFIX_PATTERN = Pattern.compile("\\p{Alpha}?$");
//
//    private static String stripSuffix(String allele) {
//        Matcher m = ALLELE_SUFFIX_PATTERN.matcher(allele);
//        if (m.matches()) {
//            allele = allele.substring(0, m.start());
//        }
//        return allele;
//    }

//    public static Function<String, String> trimAllelesToProtein() {
//        return applyToAlleles(a -> {
//            Matcher m = ALLELE_PROTEIN_PATTERN.matcher(a);
//            return m.group("protein");
//        });
//    }

    public static Function<String, String> trimAllelesToFields(int numFields) {
        return applyToAlleles(a -> {
            Matcher m = ALLELE_FIELD_PATTERN.matcher(a);
            StringBuffer sb = new StringBuffer();
            if (!m.find()) return a;
            // todo: clean up special handling for null alleles
            boolean nullAllele = a.endsWith("N");
            sb.append(a.substring(0, m.start()));
            for (int i = 0; i < numFields; i++) {
                sb.append(m.group("field"));
                if (!m.find()) break;
            }
            if (nullAllele) sb.append("N");
            return sb.toString();
        });
    }

//    public static Function<String, String> conditionalFunction(Predicate<String> predicate, Function<String, String> function) {
//        return s -> {
//            if (predicate.test(s)) return s;
//            String applied = function.apply(s);
//            return (null != applied) ? applied : null;
//        };
//    }

    public static Function<String, String> expandAlleleCodes(Function<String, String> alleleCodeResolver) {
        return applyToAlleles(a -> {
            if (!ALLELE_CODE_PATTERN.matcher(a).matches()) return a;
            String expanded = alleleCodeResolver.apply(a);
            return (expanded == null) ? a : expanded;
        });
    }

    public static Function<String, String> normalizeGroups(Function<String, String> groupResolver) {
        return applyToAlleles(a -> {
            if (!ALLELE_PATTERN.matcher(a).matches()) return a;
            String normalized = groupResolver.apply(a);
            return (normalized == null) ? a : normalized;
        });
    }

    public static Function<String, String> normalizePrefixes(String defaultLocus) {
        return applyToAlleles(a -> {
            Matcher m = ALLELE_PREFIX_PATTERN.matcher(a);
            StringBuilder sb = new StringBuilder();
            if (!m.matches()) return a;
            if (m.group("shortPrefix") == null) {
                if (defaultLocus == null) {
                    throw new RuntimeException("default locus in GL string not supported");
                } else {
                    sb.append(defaultLocus).append("*");
                }
            } else {
                sb.append("HLA-").append(m.group("shortPrefix"));
            }
            sb.append(m.group("allele"));
            return sb.toString();
        });
    }

}
