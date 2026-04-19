package com.commonground.admin;

import com.commonground.listing.ServiceResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;

// admin dashboard endpoints - only works if the session user is_admin = true
@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        Integer adminId = getAdminId(req);
        if (adminId == null) {
            res.setStatus(403);
            out.print("{\"error\":\"Admin access required\"}");
            return;
        }

        String path = req.getPathInfo();

        try {
            if ("/users".equals(path)) {
                out.print(gson.toJson(adminService.viewAllAccounts(adminId)));
            } else if ("/reports".equals(path)) {
                out.print(gson.toJson(adminService.viewReports(adminId)));
            } else if ("/transactions".equals(path)) {
                out.print(gson.toJson(adminService.viewTransactionLog(adminId)));
            } else {
                res.setStatus(404);
                out.print("{\"error\":\"Unknown admin endpoint\"}");
            }
        } catch (Exception e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        Integer adminId = getAdminId(req);
        if (adminId == null) {
            res.setStatus(403);
            out.print("{\"error\":\"Admin access required\"}");
            return;
        }

        String path = req.getPathInfo();

        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

            ServiceResult result;
            if ("/suspend".equals(path)) {
                int targetId = body.get("targetId").getAsInt();
                result = adminService.suspendUser(adminId, targetId);
            } else if ("/unsuspend".equals(path)) {
                int targetId = body.get("targetId").getAsInt();
                result = adminService.unsuspendUser(adminId, targetId);
            } else if ("/remove-listing".equals(path)) {
                int listingId = body.get("listingId").getAsInt();
                result = adminService.removeListing(adminId, listingId);
            } else if ("/remove-review".equals(path)) {
                int reviewId = body.get("reviewId").getAsInt();
                result = adminService.removeReview(adminId, reviewId);
            } else {
                res.setStatus(404);
                out.print("{\"error\":\"Unknown endpoint\"}");
                return;
            }

            JsonObject response = new JsonObject();
            response.addProperty("success", result.isSuccess());
            response.addProperty("message", result.getMessage());
            res.setStatus(result.isSuccess() ? 200 : 400);
            out.print(gson.toJson(response));

        } catch (Exception e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // checks session to see if the user is an admin
    private Integer getAdminId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object isAdmin  = session.getAttribute("isAdmin");
        Object accountId = session.getAttribute("accountId");
        if (isAdmin == null || !(boolean) isAdmin) return null;
        return (Integer) accountId;
    }
}
