package org.nmdp.service.epitope.gl.transform;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by Erik Pearson
 */
@RunWith(MockitoJUnitRunner.class)
public class GlStringFunctionsTest {

    @InjectMocks
    GlStringFunctions subject;

    @Test
    public void testApplyToAlleles() throws Exception {
        // erik: testApplyToAlleles
    }

    @Test
    public void testTrimAllelesToFields() throws Exception {
        assertThat(GlStringFunctions.trimAllelesToFields(2).apply("DPB1*01:01:01/DPB1*01:01:02"), Matchers.equalTo("DPB1*01:01/DPB1*01:01"));
    }

    @Test
    public void testExpandAlleleCodes() throws Exception {
        // erik: testExpandAlleleCodes
    }

    @Test
    public void testNormalizeGroups() throws Exception {
        // erik: testNormalizeGroups
    }

    @Test
    public void testNormalizePrefixes() throws Exception {
        String gl = "B*01:01:01+HLA-A*01:ABCD/02:01:01:02";
        String test = GlStringFunctions.normalizePrefixes("HLA-DPB1").apply(gl);
        assertThat(test, equalTo("HLA-B*01:01:01+HLA-A*01:ABCD/HLA-DPB1*02:01:01:02"));
    }

    @Test
    public void testNormalizePrefixes_DefaultLocus() throws Exception {
        String gl = "B*01:01:01+HLA-A*01:ABCD/02:01:01:02";
        String test = GlStringFunctions.normalizePrefixes("HLA-FOO").apply(gl);
        assertThat(test, equalTo("HLA-B*01:01:01+HLA-A*01:ABCD/HLA-FOO*02:01:01:02"));
    }

    @Test(expected=RuntimeException.class)
    public void testNormalizePrefixes_NoLocus() throws Exception {
        String gl = "B*01:01:01+HLA-A*01:ABCD/02:01:01:02";
        String test = GlStringFunctions.normalizePrefixes(null).apply(gl);
    }

}
