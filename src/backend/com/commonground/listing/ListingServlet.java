package com.commonground.listing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet({"/api/listings", "/api/listings/options"})
public class ListingServlet extends HttpServlet {

    private static final String DEFAULT_IMAGE_URL =
            "https://images.unsplash.com/photo-1612015900986-4c4d017d1648?w=800";
    private static final Pattern DATA_URL_PATTERN =
            Pattern.compile("^data:(image/(png|jpeg|jpg|gif|webp));base64,(.+)$");

    private final ListingService service = new ListingService();
    private final Gson gson = new Gson();

    @Override
    public void init() {
        service.initialize();
    }

    // GET /api/listings           - all available listings
    // GET /api/listings?id=X      - one listing
    // GET /api/listings?category=X - filter by category
    // GET /api/listings?search=X  - keyword search
    // GET /api/listings?clientId=X - my listings
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        if ("/api/listings/options".equals(req.getServletPath())) {
            out.print(gson.toJson(getListingOptions()));
            return;
        }

        String id       = req.getParameter("id");
        String category = req.getParameter("category");
        String search   = req.getParameter("search");
        String clientId = req.getParameter("clientId");

        if (id != null) {
            try {
                Listing listing = service.getListingById(Integer.parseInt(id));
                if (listing == null) {
                    res.setStatus(404);
                    out.print("{\"error\":\"Listing not found\"}");
                } else {
                    out.print(gson.toJson(toFrontend(listing)));
                }
            } catch (NumberFormatException e) {
                res.setStatus(400);
                out.print("{\"error\":\"Invalid id\"}");
            }
        } else if (clientId != null) {
            try {
                List<Listing> results = service.getMyListings(Integer.parseInt(clientId));
                out.print(gson.toJson(toFrontendList(results)));
            } catch (NumberFormatException e) {
                res.setStatus(400);
                out.print("{\"error\":\"Invalid clientId\"}");
            }
        } else if (category != null && !category.isBlank()) {
            List<Listing> results = service.getListingsByCategory(category);
            out.print(gson.toJson(toFrontendList(results)));
        } else if (search != null && !search.isBlank()) {
            List<Listing> results = service.search(search);
            out.print(gson.toJson(toFrontendList(results)));
        } else {
            List<Listing> listings = service.getAllAvailableListings();
            out.print(gson.toJson(toFrontendList(listings)));
        }
    }

    // POST /api/listings - create listing
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

            int    clientId    = body.get("clientId").getAsInt();
            int    locationId  = body.has("locationId") ? body.get("locationId").getAsInt() : 1;
            String category    = body.get("category").getAsString();
            String itemName    = body.get("title").getAsString();
            double price       = body.get("price").getAsDouble();
            String description = body.get("description").getAsString();
            String condition   = body.get("condition").getAsString();
            String paymentType = body.has("paymentType") ? body.get("paymentType").getAsString() : "Cash";
            String imageUrl    = body.has("imageUrl") ? body.get("imageUrl").getAsString().trim() : "";

            ServiceResult result = service.createListing(
                    clientId, locationId, category, itemName, price, description, condition, paymentType);

            JsonObject response = new JsonObject();
            response.addProperty("success", result.isSuccess());
            response.addProperty("message", result.getMessage());
            if (result.isSuccess()) {
                if (!imageUrl.isBlank()) {
                    String savedImageUrl = saveImageReference(imageUrl);
                    ServiceResult imageResult = service.addImage(result.getId(), "listing-image", savedImageUrl);
                    if (!imageResult.isSuccess()) {
                        res.setStatus(500);
                        response.addProperty("success", false);
                        response.addProperty("message", imageResult.getMessage());
                        out.print(gson.toJson(response));
                        return;
                    }
                }
                response.addProperty("id", result.getId());
            }

            res.setStatus(result.isSuccess() ? 201 : 400);
            out.print(gson.toJson(response));

        } catch (Exception e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // PUT /api/listings - edit listing
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

            int    listingId   = body.get("id").getAsInt();
            int    clientId    = body.get("clientId").getAsInt();
            String category    = body.get("category").getAsString();
            String itemName    = body.get("title").getAsString();
            double price       = body.get("price").getAsDouble();
            String description = body.get("description").getAsString();
            String condition   = body.get("condition").getAsString();
            String paymentType = body.has("paymentType") ? body.get("paymentType").getAsString() : "Cash";
            String imageUrl    = body.has("imageUrl") && !body.get("imageUrl").isJsonNull()
                    ? body.get("imageUrl").getAsString().trim()
                    : "";

            ServiceResult result = service.editListing(
                    listingId, clientId, category, itemName, price, description, condition, paymentType);
            if (result.isSuccess() && !imageUrl.isBlank()) {
                String savedImageUrl = saveImageReference(imageUrl);
                ServiceResult imageResult = service.replaceImages(listingId, "listing-image", savedImageUrl);
                if (!imageResult.isSuccess()) {
                    result = ServiceResult.fail(imageResult.getMessage());
                }
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

    // DELETE /api/listings?id=X&clientId=Y
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        try {
            int listingId = Integer.parseInt(req.getParameter("id"));
            int clientId  = Integer.parseInt(req.getParameter("clientId"));

            ServiceResult result = service.deleteListing(listingId, clientId);

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

    // maps the backend Listing fields to what the frontend expects
    private JsonObject toFrontend(Listing l) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id",            l.getListingId());
        obj.addProperty("title",         l.getItemName());
        obj.addProperty("price",         l.getPrice());
        obj.addProperty("category",      l.getCategoryName() != null ? l.getCategoryName() : "");
        obj.addProperty("condition",     l.getItemCondition());
        obj.addProperty("description",   l.getDescription());
        obj.addProperty("location",      "Houston, TX");
        obj.addProperty("listerName",    l.getSellerName() != null ? l.getSellerName() : "Seller #" + l.getClientId());
        obj.addProperty("listerEmail",   l.getSellerEmail() != null ? l.getSellerEmail() : "");
        obj.addProperty("listerJoinDate", l.getSellerJoinDate() != null ? l.getSellerJoinDate() : "");
        obj.addProperty("postedDate",    l.getCreatedAt() != null ? l.getCreatedAt() : "");
        JsonArray images = getFrontendImages(l.getListingId());
        obj.addProperty("image",         images.size() > 0 ? images.get(0).getAsString() : DEFAULT_IMAGE_URL);
        obj.addProperty("status",        l.getStatus());
        obj.addProperty("clientId",      l.getClientId());
        obj.add("images", images);
        return obj;
    }

    private JsonArray getFrontendImages(int listingId) {
        JsonArray images = new JsonArray();
        for (String[] image : service.getImages(listingId)) {
            if (image.length > 1 && image[1] != null && !image[1].isBlank()) {
                images.add(image[1]);
            }
        }
        if (images.size() == 0) {
            images.add(DEFAULT_IMAGE_URL);
        }
        return images;
    }

    private String saveImageReference(String imageValue) throws IOException {
        if (imageValue == null || imageValue.isBlank()) {
            return "";
        }

        String trimmed = imageValue.trim();
        if (!trimmed.startsWith("data:image/")) {
            return trimmed;
        }

        Matcher matcher = DATA_URL_PATTERN.matcher(trimmed);
        if (!matcher.matches()) {
            throw new IOException("Invalid image data.");
        }

        String extension = matcher.group(2).equals("jpeg") ? "jpg" : matcher.group(2);
        byte[] imageBytes = Base64.getDecoder().decode(matcher.group(3));

        Path uploadDir = getUploadDirectory();
        Files.createDirectories(uploadDir);

        String fileName = UUID.randomUUID() + "." + extension;
        Path outputPath = uploadDir.resolve(fileName);
        Files.write(outputPath, imageBytes);

        return "/uploads/listings/" + fileName;
    }

    private Path getUploadDirectory() {
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Path.of(catalinaBase, "common-ground-uploads", "listings");
        }
        return Path.of(System.getProperty("java.io.tmpdir"), "common-ground-uploads", "listings");
    }

    private JsonObject getListingOptions() {
        JsonObject options = new JsonObject();
        JsonArray categories = new JsonArray();
        JsonArray conditions = new JsonArray();
        JsonArray paymentTypes = new JsonArray();

        for (String category : ListingCategory.getAllDisplayNames()) {
            categories.add(category);
        }

        for (String condition : service.getAllowedConditions()) {
            conditions.add(condition);
        }

        for (String paymentType : service.getAllowedPaymentTypes()) {
            paymentTypes.add(paymentType);
        }

        options.add("categories", categories);
        options.add("conditions", conditions);
        options.add("paymentTypes", paymentTypes);
        return options;
    }

    private JsonArray toFrontendList(List<Listing> listings) {
        JsonArray arr = new JsonArray();
        for (Listing l : listings) arr.add(toFrontend(l));
        return arr;
    }
}
