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

package org.nmdp.service.epitope.domain;

/**
 * Detail race groups
 */

public enum DetailRace {
	AAFA(BroadRace.AFA, "African American"), 
	AFA(BroadRace.AFA, "Unspecified"), 
	AFB(BroadRace.AFA, "African"), 
	CARB(BroadRace.AFA, "Black Caribbean"), 
	MAFA(BroadRace.AFA, "Multiple Black"), 
	NAMB(BroadRace.AFA, "North American Black"), 
	SCAMB(BroadRace.AFA, "Black South or Central America"), 
	AINDI(BroadRace.API, "South Asian"), 
	API(BroadRace.API, "Unspecified"), 
	FILII(BroadRace.API, "Filipino"), 
	JAPI(BroadRace.API, "Japanese"), 
	KORI(BroadRace.API, "Korean"), 
	MAPI(BroadRace.API, "Multiple Asian/Pacific Islndr"), 
	NCHI(BroadRace.API, "Chinese"), 
	SCSEAI(BroadRace.API, "Other Southeast Asian"), 
	VIET(BroadRace.API, "Vietnamese"), 
	CAU(BroadRace.CAU, "Unspecified"), 
	EEURO(BroadRace.CAU, "Eastern European"), 
	EURWRC(BroadRace.CAU, "North American or European"), 
	MCAU(BroadRace.CAU, "Multiple Caucasian"), 
	MEDIT(BroadRace.CAU, "Mediterranean"), 
	MENAFC(BroadRace.CAU, "MidEast/No. Coast of Africa"), 
	MIDEAS(BroadRace.CAU, "Middle Eastern"), 
	NAMER(BroadRace.CAU, "North American"), 
	NCAFRI(BroadRace.CAU, "North Coast of Africa"), 
	NEURO(BroadRace.CAU, "Northern European"), 
	WCARIB(BroadRace.CAU, "White Caribbean"), 
	WEURO(BroadRace.CAU, "Western European"), 
	WSCA(BroadRace.CAU, "White South or Central America"), 
	DEC(BroadRace.DEC, "Declines or No Race Selected"), 
	GUAMAN(BroadRace.HAWI, "Guamanian"), 
	HAWAII(BroadRace.HAWI, "Hawaiian"), 
	HAWI(BroadRace.HAWI, "Unspecified"), 
	MHAW(BroadRace.HAWI, "Multiple Nat. Hw/Oth Pa. Isln"), 
	OPI(BroadRace.HAWI, "Other Pacific Islander"), 
	SAMOAN(BroadRace.HAWI, "Samoan"), 
	CARHIS(BroadRace.HIS, "Caribbean Hispanic"), 
	HIS(BroadRace.HIS, "Unspecified Hispanic"), 
	MHIS(BroadRace.HIS, "Multiple Hispanic"), 
	MSWHIS(BroadRace.HIS, "Mexican or Chicano"), 
	SCAHIS(BroadRace.HIS, "South/Cntrl Amer. Hisp."), 
	MULTI(BroadRace.MULTI, "Multiple Race"), 
	AISC(BroadRace.NAM, "American Indian South or Centr"), 
	ALANAM(BroadRace.NAM, "Alaska Native or Aleut"), 
	AMIND(BroadRace.NAM, "North American Indian"), 
	CARIBI(BroadRace.NAM, "Caribbean Indian"), 
	MNAM(BroadRace.NAM, "Multiple Native American"), 
	NAM(BroadRace.NAM, "Unspecified"), 
	OTH(BroadRace.OTH, "Other"), 
	UNK(BroadRace.UNK, "Unknown/Question Not Asked");

	private BroadRace broadRace;
	private String description;

	DetailRace(BroadRace broadRace, String description) {
		this.broadRace = broadRace;
		this.description = description;
	}

	public BroadRace getBroadRace() {
		return broadRace;
	}

	public String getDescription() {
		return description;
	}

}
