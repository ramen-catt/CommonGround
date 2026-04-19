package com.commonground.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// allows the React app (local dev or Vercel) to talk to this backend
// set ALLOWED_ORIGIN env var on Railway to your Vercel URL, e.g. https://commonground.vercel.app
@WebFilter("/*")
public class CORSFilter implements Filter {

    private static final String ALLOWED_ORIGIN =
            System.getenv().getOrDefault("ALLOWED_ORIGIN", "http://localhost:5173");

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request   = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String origin = request.getHeader("Origin");
        if (origin != null && (origin.equals(ALLOWED_ORIGIN)
                || origin.equals("http://localhost:5173")
                || origin.equals("https://localhost:5173"))) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }
}
