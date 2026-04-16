package com.commonground.feedback;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet
public class FeedBackServlet extends HttpServlet{

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
        // Common fields
        int buyerId = Integer.parseInt(request.getParameter("buyerId"));
        int sellerId = Integer.parseInt(request.getParameter("sellerId"));
        int listingId = Integer.parseInt(request.getParameter("listingId"));
        int rating = Integer.parseInt(request.getParameter("rating"));
        String ratingDesc = request.getParameter("ratingDesc");

        // Detect if this is a report
        String reportDesc = request.getParameter("reportDesc");

        FeedBack fb;

        boolean isReport = reportDesc != null && !reportDesc.isEmpty();

        if (isReport) {
            // REPORT constructor
            fb = new FeedBack(buyerId, sellerId, listingId, rating, ratingDesc, reportDesc);
            FeedBackSystem.addReport(fb);
        } else {
            // REVIEW constructor
            fb = new FeedBack(buyerId, sellerId, listingId, rating, ratingDesc);
            FeedBackSystem.addReview(fb);
        }

        // Add to system
        //FeedBackSystem system = new FeedBackSystem();
        //system.addFeedback(fb);

        // Response
        response.setContentType("text/html");
        response.getWriter().println("<h2>Feedback submitted successfully!</h2>");
    }
}

