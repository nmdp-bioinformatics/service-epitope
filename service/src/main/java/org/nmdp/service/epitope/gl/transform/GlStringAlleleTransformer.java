package org.nmdp.service.epitope.gl.transform;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * Created by Erik Pearson
 */
public class GlStringAlleleTransformer implements Function<String, String> {

    private static final Pattern ALLELE_PAT = Pattern.compile("(?<=^|[/~+|^])([^/~+|^]*)(?=$|[/~+|^])");

    private Function<String, String> alleleTransformer;

    public GlStringAlleleTransformer(Function<String, String> alleleTransformer) {
        this.alleleTransformer = alleleTransformer;
    }

    @Override
    public String apply(String glstring) {
        Matcher m = ALLELE_PAT.matcher(glstring);
        StringBuilder sb = new StringBuilder();
        int last = 0;
        while (m.find()) {
            if (m.start() > last) sb.append(glstring.substring(last, m.start()));
            String a = alleleTransformer.apply(m.group());
            if (null == a) {
                sb.append(m.group());
            } else {
                sb.append(a);
            }
            last = m.end();
        }
        if (last < glstring.length()) sb.append(glstring.substring(last, glstring.length()));
        return sb.toString();
    }
}
