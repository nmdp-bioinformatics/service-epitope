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

import com.google.inject.Inject;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.db.GroupRow;
import org.nmdp.service.epitope.gl.transform.GlStringFunctions;
import org.nmdp.service.epitope.guice.ConfigurationBindings;
import org.nmdp.service.epitope.guice.ConfigurationBindings.ImgtHlaUrls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HlaGroupInitializer {

    static final String ns = "http://hla.alleles.org/xml";
    static final QName alleleQn = new QName(ns, "allele");
    static final QName locusQn = new QName(ns, "locus");
    static final QName gGroupQn = new QName(ns, "hla_g_group");
    static final QName pGroupQn = new QName(ns, "hla_p_group");
    Logger logger = LoggerFactory.getLogger(getClass());

    DbiManager dbiManager;
	private URL[] urls;
	XMLEventReader er;

    @Inject
    public HlaGroupInitializer(@ImgtHlaUrls URL[] urls, DbiManager dbiManager) {
		this.dbiManager = dbiManager;
		this.urls = urls;
    }

    public static QName qname(String name) {
        return new QName(ns, name);
    }
    public static QName name(String name) {
        return new QName(null, name);
    }
    public boolean isStartElement(XMLEvent e, QName name) {
        return e.isStartElement() && e.asStartElement().getName().equals(name);
    }

    public boolean isEndElement(XMLEvent e, QName name) {
        return e.isEndElement() && e.asEndElement().getName().equals(name);
    }

    public void loadGroups() {
    	logger.info("loading groups");
        Long datasetDate = dbiManager.getDatasetDate("hla_g_group");
        if (null == datasetDate) datasetDate = 0L;
        Long pGroupDate = dbiManager.getDatasetDate("hla_p_group");
        if (null == pGroupDate) pGroupDate = 0L;
        if (pGroupDate.compareTo(datasetDate) < 0) datasetDate = pGroupDate;
        URLProcessor urlProcessor = new URLProcessor(urls, true);
        LinkedList<GroupRow<String>> gGroupRows = new LinkedList<>();
        LinkedList<GroupRow<String>> pGroupRows = new LinkedList<>();
        datasetDate = urlProcessor.process(is -> {
        	XMLInputFactory xmlif = XMLInputFactory.newInstance();
            try {
                XMLEventReader xmler = xmlif.createXMLEventReader(is);
                XMLFilteredEventReader er = new XMLFilteredEventReader(xmler);
                while (er.withFilter(e -> isStartElement(e, qname("allele"))).hasNext()) {
                    StartElement se = er.nextEvent().asStartElement();
                    String allele = se.getAttributeByName(name("name")).getValue();
                    se = er.withFilter(e -> isStartElement(e, qname("locus"))).nextEvent().asStartElement();
                    String locus = se.getAttributeByName(name("locusname")).getValue();
                    if (locus.matches("(HLA-)?DPB1")) {
                        XMLEvent xe = er.withFilter(e ->
                                isEndElement(e, qname("allele")) || isStartElement(e, qname("hla_g_group"))).nextEvent();
                        if (xe.isStartElement()) {
                            String group = xe.asStartElement().getAttributeByName(name("status")).getValue();
                            group = stripPrefix(group);
                            if (!group.equals("None")) {
                                addGroupAllele(group, allele, gGroupRows, 3);
                            }
                        }
                        xe = er.withFilter(e ->
                                isEndElement(e, qname("allele")) || isStartElement(e, qname("hla_p_group"))).nextEvent();
                        if (xe.isStartElement()) {
                            String group = xe.asStartElement().getAttributeByName(name("status")).getValue();
                            group = stripPrefix(group);
                            if (!group.equals("None")) {
                                addGroupAllele(group, allele, pGroupRows, 2);
                            }
                        }
                    }
                }
                dbiManager.loadGGroups(gGroupRows.iterator(), true);
                dbiManager.loadPGroups(pGroupRows.iterator(), true);
            } catch (RuntimeException e) {
            	throw e;
            } catch (Exception e) {
                throw new RuntimeException("failed to load HLA groups", e);
            } finally {
            	try { 
            		is.close(); 
            	} catch (IOException e) { 
            		throw new RuntimeException("failed to close stream", e); 
            	} 
            }
        }, datasetDate);
        dbiManager.updateDatasetDate("hla_g_group", datasetDate);
        dbiManager.updateDatasetDate("hla_p_group", datasetDate);
        logger.debug("done loading HLA groups");
    }

    public static final Pattern ALLELE_STRIP_FIELD_PATTERN = Pattern.compile(
            "(?<stripped>(?:[^/~+|^*]*\\*)?\\d+(?::\\p{Alnum}+)+)(?<last>:\\p{Alnum}+)");

    public String stripLastField(String allele) {
        Matcher m = ALLELE_STRIP_FIELD_PATTERN.matcher(allele);
        if (!m.matches()) return allele;
        return m.group("stripped");
    }

    public String stripPrefix(String allele) {
        int i = allele.indexOf('*');
        if (i >= 0) allele = allele.substring(i+1);
        return allele;
    }

    public void addGroupAllele(String group, String allele, List<GroupRow<String>> groupList, int minFields) {
        if (!group.equals("None")) {
            String minAllele = GlStringFunctions.trimAllelesToFields(minFields).apply(allele);
            String a = allele;
            while (!a.equals(minAllele)) {
                groupList.add(new GroupRow<>(a, group));
                a = stripLastField(a);
            }
            groupList.add(new GroupRow<>(a, group));
        }

    }
}
