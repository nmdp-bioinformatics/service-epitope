/*

    epitope-service  T-cell epitope group matching service for HLA-DPB1 locus.
    Copyright (c) 2014-2015 National Marrow Donor Program (NMDP)
    
    This library is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation; either version 3 of the License, or (at
    your option) any later version.
    
    This library is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
    License for more details.
    
    You should have received a copy of the GNU Lesser General Public License
    along with this library;  if not, write to the Free Software Foundation,
    Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA.
    
    > http://www.gnu.org/licenses/lgpl.html

*/

package org.nmdp.service.epitope.task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;

import org.nmdp.service.epitope.db.AlleleRow;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.guice.ConfigurationBindings.HlaAlleleUrls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class AlleleInitializer {

    DbiManager dbiManager;
	private URL[] urls;
	Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    public AlleleInitializer(@HlaAlleleUrls URL[] urls, DbiManager dbiManager) {
        this.dbiManager = dbiManager;
        this.urls = urls;
    }

    public void loadAlleles() {
    	logger.info("loading alleles");
        Long datasetDate = dbiManager.getDatasetDate("hla_allele");
        if (null == datasetDate) datasetDate = 0L;
        URLProcessor urlProcessor = new URLProcessor(urls, false);
        datasetDate = urlProcessor.process(is -> {
        	try (InputStreamReader isr = new InputStreamReader(is);
        			BufferedReader br = new BufferedReader(isr)) 
        	{
	        	Iterator<AlleleRow> alleleIter = br.lines()
	        		.map(s -> s.substring(s.indexOf(" ") + 1))
	        		.filter(s -> s.startsWith("DPB1*"))
	        		.map(s -> new AlleleRow(s.substring(0, s.indexOf("*")), s.substring(s.indexOf("*") + 1)))
	        		.iterator();
	        	dbiManager.loadAlleles(alleleIter, true);
    		} catch (RuntimeException e) {
    			throw (RuntimeException)e;
    		} catch (Exception e) {
    			throw new RuntimeException("failed to load allele file", e);
        	}
        }, datasetDate);
        dbiManager.updateDatasetDate("hla_allele", datasetDate);
    	logger.debug("done loading alleles");

    }
            
}
