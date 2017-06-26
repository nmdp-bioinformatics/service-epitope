package org.nmdp.service.epitope.task;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.function.Consumer;

/**
 * Created by Erik Pearson
 */
public class XMLFilteredEventReader implements XMLEventReader {
    private XMLEventReader fr;
    private EventFilter f;
    public XMLFilteredEventReader(XMLEventReader reader) throws XMLStreamException {
        this.fr = XMLInputFactory.newFactory().createFilteredReader(reader, e -> (null == f || f.accept(e)));
    }
    public XMLFilteredEventReader withFilter(EventFilter filter) {
        this.f = filter;
        return this;
    }
    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        return fr.nextEvent();
    }

    @Override
    public boolean hasNext() {
        return fr.hasNext();
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        return fr.peek();
    }

    @Override
    public String getElementText() throws XMLStreamException {
        return fr.getElementText();
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        return fr.nextTag();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return fr.getProperty(name);
    }

    @Override
    public void close() throws XMLStreamException {
        fr.close();
    }

    @Override
    public Object next() {
        return fr.next();
    }

    @Override
    public void remove() {
        fr.remove();
    }

    @Override
    public void forEachRemaining(Consumer action) {
        fr.forEachRemaining(action);
    }
}
