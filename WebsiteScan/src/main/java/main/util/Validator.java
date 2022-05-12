package main.util;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;

public class Validator {

    public static boolean isValidURL(String url){

        if(InetAddressValidator.getInstance().isValid(url) || url.matches("(((\\d{1,3}\\.){3})(\\d{1,3}))")){
            return true;
        }

        String[] shemes = {"http", "https"};
        if(new UrlValidator(shemes, UrlValidator.ALLOW_LOCAL_URLS).isValid(url)){
            return true;
        }

        return false;
    }
}
