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

package org.nmdp.service.epitope.dropwizard;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.nmdp.service.epitope.guice.ConfigurationBindings.AlleleCodeCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.AlleleCodeCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.BaselineAlleleFrequency;
import org.nmdp.service.epitope.guice.ConfigurationBindings.FrequencyCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.FrequencyCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GGroupCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GGroupCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.Group1Suffix;
import org.nmdp.service.epitope.guice.ConfigurationBindings.Group2Suffix;
import org.nmdp.service.epitope.guice.ConfigurationBindings.Group3Suffix;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GroupCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.HlaAlleleUrls;
import org.nmdp.service.epitope.guice.ConfigurationBindings.HlaAmbigUrls;
import org.nmdp.service.epitope.guice.ConfigurationBindings.HlaProtUrls;
import org.nmdp.service.epitope.guice.ConfigurationBindings.LiftoverServiceUrl;
import org.nmdp.service.epitope.guice.ConfigurationBindings.MatchProbabilityPrecision;
import org.nmdp.service.epitope.guice.ConfigurationBindings.NamespaceUrl;
import org.nmdp.service.epitope.guice.ConfigurationBindings.NmdpV3AlleleCodeRefreshMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.NmdpV3AlleleCodeUrls;
import org.nmdp.service.epitope.guice.ConfigurationBindings.RefreshMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.ResolveCodes;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;

public class EpitopeServiceConfiguration extends Configuration {
	
    /** If null, use LocalGlClient.  If not null, use JsonGlClientModule and enable the following: 
     *  <ul>
     *  <li>GL Strings are created using the provided namespace URL, 
     *  <li>GL Service IDs are accepted as input,
     *  <li>Immunogenicity groups are retrieved from the provided namespace URL given suffixes ({@link #group1Suffix}, {@link #group2Suffix}, {@link #group3Suffix})
     *  <li>GL Service IDs are optionally lifted over to internal namespace (if {@link #liftoverServiceUrl} is provided).
     *  </ul>
     */
    private String namespaceUrl;

    /** if true, accept allele codes in GL string input (e.g. DPB1*ABCD+DPB1*EFGH) and resolve 
     */
    private boolean resolveCodes = false;

    /** if not null (and if namespace not null, enabling , lift over input GL service IDs to specified namespace, otherwise error 
     *  if provided namespace doesn't match internal
     */
    private String liftoverServiceUrl;

    /** https://bioinformatics.bethematchclinical.org/HLA/alpha.v3.zip
     */
    private String[] nmdpV3AlleleCodeUrls = { "https://bioinformatics.bethematchclinical.org/HLA/alpha.v3.zip" };
    
    /** IMGT ambiguous allele file locations 
     */
    private String[] hlaAmbigUrls = { "ftp://ftp.ebi.ac.uk/pub/databases/ipd/imgt/hla/xml/hla_ambigs.xml.zip" };
    
    /** IMGT allele name file locations
     */
    private String[] hlaAlleleUrls = { "ftp://ftp.ebi.ac.uk/pub/databases/ipd/imgt/hla/Allelelist.txt" };
    
    /** File containing protein descriptions for HLA alleles
     */
	// { "ftp://ftp.ebi.ac.uk/pub/databases/ipd/imgt/hla/hla_prot.fasta" };
	private String[] hlaProtUrls = { "/DPB1.db.3.22.0" };

	/** number of milliseconds between refreshes of the upstream data sources (alleles, g-groups, immune groups)
     */
	private long refreshMillis = 60 * 60 * 1000L;
	
	/** number of milliseconds the group cache should be kept before refreshing it from the underlying resolver
     */
	private long groupCacheMillis = 60 * 60 * 1000L; 
    
	/** number of milliseconds the g group cache should be kept before refreshing it from the underlying resolver
     */
	private long gGroupCacheMillis = 60 * 60 * 1000L; 

    /** size of the allele -> g group cache
     */
	private long gGroupCacheSize = 5000;

    /** number of milliseconds the gl cache should be kept before refreshing it from the  underlying resolver
     */
	private long glCacheMillis = 60 * 60 * 1000L;
    
    /** size of the gl cache
     */
	private long glCacheSize = 100000L;
    
    /** number of milliseconds the allele code cache should be kept before refreshing it from the  underlying resolver
     */
	private long alleleCodeCacheMillis = 60 * 60 * 1000L; 
    
    /** size of the allele code cache
     */
	private long alleleCodeCacheSize = 5000;
    
    /** number of milliseconds the frequency cache should be kept before refreshing it from the  underlying resolver
     */
	private long frequencyCacheMillis = 60 * 60 * 1000L; 
    
    /** size of the frequency cache
     */
	private long frequencyCacheSize = 1000000L;
    
    /** frequency to assume if no data is present
     */
    private double baselineAlleleFrequency = 1.0E-5; // from loren
    
    /** precision of match grade probabilities
     */
	private double matchProbabilityPrecision = 1.0E-5;

    /** jdbi data source factory, set by dropwizard
	 */
	@Valid
    @NotNull
    private DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
	/** flyway factory, set by dropwizard
	 */
	@Valid
	@NotNull
	private FlywayFactory flywayFactory = new FlywayFactory();
	
    @JsonProperty
    @NamespaceUrl
    public String getNamespaceUrl() {
		return namespaceUrl;
	}

    @JsonProperty
	public void setNamespaceUrl(String namespaceUrl) {
		this.namespaceUrl = namespaceUrl;
	}

    @JsonProperty
	public void setResolveCodes(boolean resolveCodes) {
		this.resolveCodes = resolveCodes;
	}

    @JsonProperty
    @LiftoverServiceUrl
	public String getLiftoverServiceUrl() {
		return liftoverServiceUrl;
	}

    @JsonProperty
	public void setLiftoverServiceUrl(String liftoverServiceUrl) {
		this.liftoverServiceUrl = liftoverServiceUrl;
	}

    @JsonProperty
	@GroupCacheMillis
	public long getGroupCacheMillis() { 
		return groupCacheMillis;
	} 
    
    @JsonProperty
    public void setGroupCacheMillis(long groupCacheMillis) {
    	this.groupCacheMillis = groupCacheMillis;
    }

    @JsonProperty
	@GGroupCacheMillis
	public long getGGroupCacheMillis() { 
		return gGroupCacheMillis;
	} 
    
    @JsonProperty
    public void setGGroupCacheMillis(long gGroupCacheMillis) {
    	this.gGroupCacheMillis = gGroupCacheMillis;
    }

    @JsonProperty
	@GGroupCacheSize
	public long getGGroupCacheSize() { 
		return gGroupCacheSize;
	} 
    
    @JsonProperty
    public void setGGroupCacheSize(long gGroupCacheSize) {
    	this.gGroupCacheSize = gGroupCacheSize;
    }

    @JsonProperty
	@GlCacheMillis
	public long getGlCacheMillis() { 
		return glCacheMillis;
	}
    
    @JsonProperty
    public void setGlCacheMillis(long glCacheMillis) {
    	this.glCacheMillis = glCacheMillis;
    }

    @JsonProperty
	@GlCacheSize
	public long getGlCacheSize() { 
		return glCacheSize; 
	}
    
    @JsonProperty
    public void setGlCacheSize(long glCacheSize) {
    	this.glCacheSize = glCacheSize;
    }
	
    @JsonProperty
	@AlleleCodeCacheMillis
	public long getAlleleCodeCacheMillis() { 
		return alleleCodeCacheMillis; 
	} 
	
    @JsonProperty
    public void setAlleleCodeCacheMillis(long alleleCodeCacheMillis) {
    	this.alleleCodeCacheMillis = alleleCodeCacheMillis;
    }
    
    @JsonProperty
	@AlleleCodeCacheSize
	public long getAlleleCodeCacheSize() { 
		return alleleCodeCacheSize; 
	} 
		
    @JsonProperty
    public void setAlleleCodeCacheSize(long alleleCodeCacheSize) {
    	this.alleleCodeCacheSize = alleleCodeCacheSize;
    }
    
    @JsonProperty
    @FrequencyCacheMillis
    public long getFrequencyCacheMillis() {
    	return frequencyCacheMillis;
    }
    
    @JsonProperty
    public void setFrequencyCacheMillis(long frequencyCacheMillis) {
    	this.frequencyCacheMillis = frequencyCacheMillis;
    }
    
    @JsonProperty
    @FrequencyCacheSize
    public long getFrequencyCacheSize() {
    	return frequencyCacheSize;
    }
    
    @JsonProperty
    public void setFrequencyCacheSize(long frequencyCacheSize) {
    	this.frequencyCacheSize = frequencyCacheSize;
    }

    @JsonProperty
    @BaselineAlleleFrequency
    public double getBaselineAlleleFrequency() {
        return baselineAlleleFrequency;
    }

    @JsonProperty
    public void setBaselineAlleleFrequency(double baselineAlleleFrequency) {
        this.baselineAlleleFrequency = baselineAlleleFrequency;
    }


    @JsonProperty
	public DataSourceFactory getDataSourceFactory() {
		return dataSourceFactory;
	}

    public FlywayFactory getFlywayFactory() {
		return flywayFactory;
	}

	@JsonProperty
    @NmdpV3AlleleCodeUrls
	public String[] getNmdpV3AlleleCodeUrls() {
		return nmdpV3AlleleCodeUrls;
	}

    @JsonProperty
    public void setNmdpV3AlleleCodeUrls(String[] nmdpV3AlleleCodeUrls) {
        this.nmdpV3AlleleCodeUrls = nmdpV3AlleleCodeUrls;
    }

    @JsonProperty
    @HlaAmbigUrls
    public String[] getHlaAmbigUrls() {
        return hlaAmbigUrls;
    }

    @JsonProperty
    public void setHlaAmbigUrls(String[] hlaAmbigUrls) {
        this.hlaAmbigUrls = hlaAmbigUrls;
    }

    @JsonProperty
    @HlaAlleleUrls
    public String[] getHlaAlleleUrls() {
        return hlaAlleleUrls;
    }
    
    @JsonProperty
    public void setHlaAlleleUrls(String[] hlaAlleleUrls) {
        this.hlaAlleleUrls = hlaAlleleUrls;
    }

    @JsonProperty
    @HlaProtUrls
    public String[] getHlaProtUrls() {
        return hlaProtUrls;
    }
    
    @JsonProperty
    public void setHlaProtUrls(String[] hlaProtUrls) {
        this.hlaProtUrls  = hlaProtUrls;
    }

    @MatchProbabilityPrecision
    @JsonProperty
    public double getMatchProbabilityPrecision() {
        return matchProbabilityPrecision;
    }
    
    @JsonProperty
    public void setMatchProbabilityPrecision(double matchProbabilityPrecision) {
        this.matchProbabilityPrecision= matchProbabilityPrecision;
    }
    
    @RefreshMillis
    @JsonProperty
	public long getRefreshMillis() {
		return refreshMillis;
	}

    @JsonProperty
	public void setRefreshMillis(long refreshMillis) {
		this.refreshMillis = refreshMillis;
	}

	@GGroupCacheMillis
    @JsonProperty
	public long getgGroupCacheMillis() {
		return gGroupCacheMillis;
	}

    @JsonProperty
	public void setgGroupCacheMillis(long gGroupCacheMillis) {
		this.gGroupCacheMillis = gGroupCacheMillis;
	}

	@GGroupCacheSize
    @JsonProperty
	public long getgGroupCacheSize() {
		return gGroupCacheSize;
	}

    @JsonProperty
	public void setgGroupCacheSize(long gGroupCacheSize) {
		this.gGroupCacheSize = gGroupCacheSize;
	}

	@ResolveCodes
    @JsonProperty
	public boolean isResolveCodes() {
		return resolveCodes;
	}
    
}
