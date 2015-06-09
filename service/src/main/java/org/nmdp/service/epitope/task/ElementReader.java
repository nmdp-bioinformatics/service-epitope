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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.util.EventReaderDelegate;

public class ElementReader extends EventReaderDelegate {

    private XMLEventReader startElementReader;
    private XMLEventReader endElementReader;

    public ElementReader(XMLEventReader xmlr) throws XMLStreamException {
        super(xmlr);
        XMLInputFactory factory = XMLInputFactory.newFactory();
        startElementReader = factory.createFilteredReader(xmlr, e -> e.isStartElement());
        endElementReader = factory.createFilteredReader(xmlr, e -> e.isEndElement());
    }

    public boolean hasNextStartElement() {
        return startElementReader.hasNext();
    }
    
    public StartElement nextStartElement() throws XMLStreamException {
        return (StartElement) startElementReader.nextEvent();
    }
    
    public boolean hasNextEndElement() {
        return endElementReader.hasNext();
    }
    
    public EndElement nextEndElement() throws XMLStreamException {
        return (EndElement) endElementReader.nextEvent();
    }
        
}
