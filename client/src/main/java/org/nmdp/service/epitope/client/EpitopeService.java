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

package org.nmdp.service.epitope.client;

import java.util.List;

import org.nmdp.service.epitope.resource.AlleleListRequest;
import org.nmdp.service.epitope.resource.AlleleView;
import org.nmdp.service.epitope.resource.GroupView;
import org.nmdp.service.epitope.resource.MatchRequest;
import org.nmdp.service.epitope.resource.MatchResponse;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Epitope service client.
 */
public interface EpitopeService {

    @POST("/alleles")
    //@Headers("Content-Encoding: gzip")
    List<AlleleView> getAlleles(@Body AlleleListRequest alleleListRequest);

    @GET("/alleles")
    //@Headers("Content-Encoding: gzip")
    List<AlleleView> getAlleles(@Query("allele") String allele,
                                @Query("alleles") String alleles,
                                @Query("alleleUri") String alleleUri,
                                @Query("alleleUris") String alleleUris,
                                @Query("group") String group,
                                @Query("groups") String groups);

    @GET("/alleles/{allele}")
    //@Headers("Content-Encoding: gzip")
    AlleleView getAllele(@Path("allele") String allele);

    @POST("/groups")
    //@Headers("Content-Encoding: gzip")
    List<GroupView> getGroups(@Body AlleleListRequest alleleListRequest);

    @GET("/groups")
    //@Headers("Content-Encoding: gzip")
    List<GroupView> getGroups(@Query("allele") String allele,
                              @Query("alleles") String alleles,
                              @Query("alleleUri") String alleleUri,
                              @Query("alleleUris") String alleleUris,
                              @Query("group") String group,
                              @Query("groups") String groups);

    @GET("/groups/{group}")
    //@Headers("Content-Encoding: gzip")
    GroupView getGroup(@Path("group") String group);

    @POST("/matches")
    //@Headers("Content-Encoding: gzip")
    List<MatchResponse> getMatches(@Body List<MatchRequest> matchRequest);

    @POST("/matches")
    //@Headers("Content-Encoding: gzip")
    Observable<List<MatchResponse>> getMatchesObservable(@Body List<MatchRequest> matchRequest);
}
