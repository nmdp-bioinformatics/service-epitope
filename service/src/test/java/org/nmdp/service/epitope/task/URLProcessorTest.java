package org.nmdp.service.epitope.task;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.Test;

public class URLProcessorTest {

	@Test
	public void testGetUrls() throws Exception {
		URL[] urls = URLProcessor.getUrls(new String[]{ "/DPB1.db.3.22.0" });
		assertThat(urls[0], notNullValue());
	}

}
