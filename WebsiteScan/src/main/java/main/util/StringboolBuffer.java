package main.util;

public class StringboolBuffer {
    private String str;
    private Boolean bool;

    private long elapsedtime;

    public void setBool(Boolean boolea) {
        bool = boolea;
    }

    public void setString(String stri) {
        str = stri;
    }

    public void setTime(long time) {
        elapsedtime = time;
    }

    public String getString() {
        return str;
    }

    public boolean getBoolean() {
        return bool;
    }

    public long getElapsedtime() {
        return elapsedtime;
    }

}
