package team.javaee.common;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class Normal {
    public static String getUserIdByCookie(HttpServletRequest request) {
        Cookie[] cookies =  request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals("userId")){
                    return cookie.getValue();
                }
            }
        }
        return  null;
    }

    public static String getIsAutoLoginByCookie(HttpServletRequest request) {
        Cookie[] cookies =  request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals("isAutoLogin")){
                    return cookie.getValue();
                }
            }
        }
        return  null;
    }
}
