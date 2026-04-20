package com.commonground.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Adds SameSite=None; Secure to JSESSIONID so session cookies work cross-origin (Vercel -> Railway)
@WebFilter("/*")
public class SameSiteCookieFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        chain.doFilter(req, new SameSiteResponseWrapper((HttpServletResponse) res));
    }

    private static class SameSiteResponseWrapper extends HttpServletResponseWrapper {

        SameSiteResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, patch(name, value));
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, patch(name, value));
        }

        private String patch(String name, String value) {
            if ("Set-Cookie".equalsIgnoreCase(name) && value != null
                    && value.contains("JSESSIONID")
                    && !value.contains("SameSite")) {
                return value + "; SameSite=None; Secure";
            }
            return value;
        }
    }
}
