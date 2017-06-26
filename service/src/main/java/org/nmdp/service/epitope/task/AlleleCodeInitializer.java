package org.nmdp.service.epitope.task;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import org.nmdp.service.epitope.allelecode.DbiAlleleCodeResolver;
import org.nmdp.service.epitope.db.AlleleCodeRow;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.guice.ConfigurationBindings.NmdpV3AlleleCodeUrls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton // required for datasetDate hack
public class AlleleCodeInitializer {

	static Logger logger = LoggerFactory.getLogger(DbiAlleleCodeResolver.class);

	private DbiManager dbi;
	private DbiAlleleCodeResolver resolver;
	Long datasetDate = 0L;

	private URL[] urls;
	
	@Inject
	public AlleleCodeInitializer(@NmdpV3AlleleCodeUrls URL[] urls, DbiManager dbi, DbiAlleleCodeResolver resolver) {
		this.urls = urls;
		this.resolver = resolver;
		this.dbi = dbi;
	}

	public void loadAlleleCodes() {
		logger.info("loading allele codes");
		Long datasetDate = dbi.getDatasetDate("allele_code");
		if (null == datasetDate) datasetDate = 0L;
		URLProcessor urlProcessor = new URLProcessor(urls, true);
		datasetDate = urlProcessor.process(is -> {
			loadFromStream(is);
		}, datasetDate);
		// reload every time
		// dbi.updateDatasetDate("allele_code", datasetDate);
		logger.debug("done loading allele codes");
	}
	
	void loadFromStream(final InputStream inputStream) {
		try (	InputStreamReader ir = new InputStreamReader(inputStream); 
				BufferedReader br = new BufferedReader(ir)) 
		{
			AtomicInteger ai = new AtomicInteger();
			Splitter splitter = Splitter.on("\t");
			while (br.readLine().length() != 0) continue;	// skip header
			String s = null;
        	Iterator<AlleleCodeRow> alleleCodeIter = br.lines()
	        		.map(line -> {
	        			int i = ai.incrementAndGet();
						List<String> parsed = splitter.splitToList(line);
						if (parsed.size() != 3) {
							throw new RuntimeException("failed to parse file on line " + i + ", expected 3 fields: " + s);
						}
						boolean generic = !("*".equals(parsed.get(0)));
						String code = parsed.get(1);
						String alleles = parsed.get(2);
						return StreamSupport.stream(Splitter.on("/").split(alleles).spliterator(), false)
								.map(allele -> new AlleleCodeRow(code, allele, generic));
	        		})
	        		.flatMap(r -> r)
	        		.iterator();
        	// dbi.loadAlleleCodes(alleleCodeIter, true);
        	resolver.buildAlleleCodeMap(alleleCodeIter);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("exception while refreshing allele codes", e);
		}
	}

}
