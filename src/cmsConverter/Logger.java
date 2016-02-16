package cmsConverter;

import java.io.PrintWriter;
import java.util.Calendar;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class Logger {
    private static final Calendar calendar = Calendar.getInstance();

    public enum Level {NONE, ERROR, WARNING, INFO, DEBUG, TRACE}

    private Level level = Level.NONE; 
    private PrintWriter stream = null;

    public void setLevel(Level l) {
        level = l;
    }

    public void setPrintWriter(PrintWriter f) {
        stream = f;
    }

    public void error(String m) {
            format("ERROR", m);
    }

    public void error(String m, Throwable e) {
            StringBuilder sb = new StringBuilder(1024);
            sb.append(m);
            sb.append(ExceptionUtils.getStackTrace(e));

            format("ERROR", sb.toString());
    }

    public void warn(String m) {
            format("WARNING", m);
    }

    public void warn(String m, Throwable e) {
            StringBuilder sb = new StringBuilder(1024);
            sb.append(m);
            sb.append(ExceptionUtils.getStackTrace(e));

            format("WARNING", sb.toString());
    }

    public void debug(String m) {
            format("DEBUG", m);
    }

    public void info(String m) {
            format("INFO", m);
    }

    public void trace(String m) {
            format("TRACE", m);
     
    }

    public void format(String type, String m) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("[");
        sb.append(type);
        sb.append("] ");
        sb.append(calendar.getTime().toString());
        sb.append(" - ");
        sb.append(m);

        String line = sb.toString();
        System.out.println(line);

        if (stream != null) {
            stream.println(line);
            stream.flush();
        } 
    }
}