package com.commonground.listing;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/uploads/listings/*")
public class ListingImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String requestedName = req.getPathInfo();
        if (requestedName == null || requestedName.length() <= 1 || requestedName.contains("..")) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = requestedName.substring(1);
        Path imagePath = getUploadDirectory().resolve(fileName).normalize();
        if (!imagePath.startsWith(getUploadDirectory()) || !Files.isRegularFile(imagePath)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = Files.probeContentType(imagePath);
        res.setContentType(contentType != null ? contentType : "application/octet-stream");
        res.setHeader("Cache-Control", "public, max-age=86400");

        try (OutputStream out = res.getOutputStream()) {
            Files.copy(imagePath, out);
        }
    }

    private Path getUploadDirectory() {
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Path.of(catalinaBase, "common-ground-uploads", "listings");
        }
        return Path.of(System.getProperty("java.io.tmpdir"), "common-ground-uploads", "listings");
    }
}
