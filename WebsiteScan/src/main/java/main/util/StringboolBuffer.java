package main.util;

public class StringboolBuffer {
    private String str;
    private Boolean bool;

    public void setBool(Boolean boolea){
        bool = boolea;
    }

    public void setString(String stri){
        str = stri;
    }

    public String getString(){
        return str;
    }

    public boolean getBoolean(){
        return bool;
    }

}
