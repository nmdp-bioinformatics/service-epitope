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

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.nmdp.service.epitope.guice.ConfigurationBindings.AlleleCodeCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.AlleleCodeCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.BaselineAlleleFrequency;
import org.nmdp.service.epitope.guice.ConfigurationBindings.FrequencyCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.FrequencyCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.Group1Suffix;
import org.nmdp.service.epitope.guice.ConfigurationBindings.Group2Suffix;
import org.nmdp.service.epitope.guice.ConfigurationBindings.Group3Suffix;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GroupCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.LiftoverServiceUrl;
import org.nmdp.service.epitope.guice.ConfigurationBindings.MatchGradeThreshold;
import org.nmdp.service.epitope.guice.ConfigurationBindings.NamespaceUrl;
import org.nmdp.service.epitope.guice.ConfigurationBindings.NmdpV3AlleleCodeRefreshMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.NmdpV3AlleleCodeUrls;
import org.nmdp.service.epitope.guice.ConfigurationBindings.ResolveCodes;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EpitopeServiceConfiguration extends Configuration {
	
    /**
     * If null, use LocalGlClient.  If not null, use JsonGlClientModule and enable the following: 
     * <ul>
     * <li>GL Strings are created using the provided namespace URL, 
     * <li>GL Service IDs are accepted as input,
     * <li>Immunogenicity groups are retrieved from the provided namespace URL given suffixes ({@link #group1Suffix}, {@link #group2Suffix}, {@link #group3Suffix})
     * <li>GL Service IDs are optionally lifted over to internal namespace (if {@link #liftoverServiceUrl} is provided).
     * </ul>
     */
    private String namespaceUrl;

	/**
     * if namespace is not null, use this url to retrieve group 1 allele list, 
     * otherwise retrieve from internal sqlite db.
     */
    private String group1Suffix; 
    
    /** 
     * if namespace is not null, use this url to retrieve group 2 allele list, 
     *  otherwise retrieve from internal sqlite db
     */
    private String group2Suffix;
    
    /**
     * if namespace is not null, use this url to retrieve group 3 allele list, 
     * otherwise retrieve from internal sqlite db
     */
    private String group3Suffix;

    /**
     * if true, accept allele codes in GL string input (e.g. DPB1*ABCD+DPB1*EFGH) and resolve 
     */
    private boolean resolveCodes;

    /**
     * if not null (and if namespace not null, enabling , lift over input GL service IDs to specified namespace, otherwise error 
     * if provided namespace doesn't match internal
     */
    private String liftoverServiceUrl;

    /**
     * https://bioinformatics.bethematchclinical.org/HLA/alpha.v3.zip
     */
    private String[] nmdpV3AlleleCodeUrls = { "https://bioinformatics.bethematchclinical.org/HLA/alpha.v3.zip" };
    
    /**
     * number of milliseconds the group cache should be kept before refreshing it from the underlying resolver
     */
	private long groupCacheMillis = 60 * 60 * 1000L; 
    
    /**
     * number of milliseconds the gl cache should be kept before refreshing it from the  underlying resolver
     */
	private long glCacheMillis = 60 * 60 * 1000L;
    
    /**
     * size of the gl cache
     */
	private long glCacheSize = 100000L;
    
    /**
     * number of milliseconds the allele code cache should be kept before refreshing it from the  underlying resolver
     */
	private long alleleCodeCacheMillis = 60 * 60 * 1000L; 
    
    /**
     * size of the allele code cache
     */
	private long alleleCodeCacheSize = 5000;
    
    /**
     * number of milliseconds the frequency cache should be kept before refreshing it from the  underlying resolver
     */
	private long frequencyCacheMillis = 60 * 60 * 1000L; 
    
    /**
     * size of the frequency cache
     */
	private long frequencyCacheSize = 1000000L;
    
    /**
     * number of seconds the nmdp v3 allele code file should be kept for before refreshing
     */
    private long nmdpV3AlleleCodeRefreshMillis = 60 * 60 * 1000L;
    
    /**
     * number of seconds the nmdp v3 allele code file should be kept for before refreshing
     */
    private double baselineAlleleFrequency = 1.0E-5; // from loren
    
    /**
     * threshold above which to consider possible outcomes as reported match grade
     */
    private double matchGradeThreshold = 0.01;
    
    /**
	 * jdbi data source factory, set by dropwizard
	 */
	@Valid
    @NotNull
    private DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
	/**
	 * flyway factory, set by dropwizard
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
    @Group1Suffix
	public String getGroup1Suffix() {
		return group1Suffix;
	}

    @JsonProperty
	public void setGroup1Suffix(String group1Suffix) {
		this.group1Suffix = group1Suffix;
	}

    @JsonProperty
    @Group2Suffix
	public String getGroup2Suffix() {
		return group2Suffix;
	}

    @JsonProperty
	public void setGroup2Suffix(String group2Suffix) {
		this.group2Suffix = group2Suffix;
	}

    @JsonProperty
    @Group3Suffix
	public String getGroup3Suffix() {
		return group3Suffix;
	}

    @JsonProperty
	public void setGroup3Suffix(String group3Suffix) {
		this.group3Suffix = group3Suffix;
	}

    @JsonProperty
    @ResolveCodes
	public boolean isResolveCodes() {
		return resolveCodes;
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
    @NmdpV3AlleleCodeRefreshMillis
	public long getNmdpV3AlleleCodeRefreshMillis() {
		return nmdpV3AlleleCodeRefreshMillis;
	}

    @JsonProperty
	public void setNmdpV3AlleleCodeRefreshMillis(
			long nmdpV3AlleleCodeRefreshMillis) {
		this.nmdpV3AlleleCodeRefreshMillis = nmdpV3AlleleCodeRefreshMillis;
	}

    @MatchGradeThreshold
    @JsonProperty
    public double getMatchGradeThreshold() {
        return matchGradeThreshold;
    }

    @JsonProperty
    public void setMatchGradeThreshold(double matchGradeThreshold) {
        this.matchGradeThreshold = matchGradeThreshold;
    }



}
