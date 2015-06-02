package org.nmdp.service.epitope.trace;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Trace {

    List<String> list;
    String context;
    
    static final ThreadLocal<Trace> tl = new ThreadLocal<Trace>();
    
    public static void enable() {
        tl.set(new Trace());
    }

    public static boolean isEnabled() {
        return tl.get() != null;
    }
    
    public static void disable() {
        tl.remove();
    }

    public static void reset() {
        enable();
    }
    
    public static void setContext(String context) {
        tl.get().context = context;
    }
    
    public static String getContext() {
        return tl.get().context;
    }
    
    public static void add(Object trace) {
        Trace t = tl.get();
        if (null != t) t.list.add(t.context + trace.toString());
    }
    
    public static List<String> getTrace() {
        Trace t = tl.get();
        if (null == t) return null;
        return Collections.unmodifiableList(t.list);
//        StringBuilder sb = new StringBuilder("trace:\n");
//        for (String s : t.list) {
//            sb.append(s).append("\n");
//        }
//        return sb.toString(); 
    }

    private Trace() {
        list = new LinkedList<>();
    }
    
}
