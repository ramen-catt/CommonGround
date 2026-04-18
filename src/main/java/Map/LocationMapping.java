/*
this is where the math/logic is for pulling the closest common ground
in our database that is closest to the Buyer and Seller midpoint using
coordinates acquired from esri geocoding in leaflet.js
 */

package Map;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class LocationMapping {

    private String url = "jdbc:mysql://localhost:3306/CommonGround_db";
    private String user = "cguser";
    private String password = "cgpass123";

    private List<CommonGroundLocation> getLocationsFromDB() {
        List<CommonGroundLocation> dbLocations = new ArrayList<>();
        String query = "SELECT name, address, latitude, longitude FROM location";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                dbLocations.add(new CommonGroundLocation(
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dbLocations;
    }
//math for finding closest common ground
    public CommonGroundLocation findClosestCG(double latB, double lonB, double latS, double lonS) {

        double midLat = (latB + latS) / 2;
        double midLon = (lonB + lonS) / 2;

        List<CommonGroundLocation> locations = getLocationsFromDB();

        CommonGroundLocation closestCG = null;
        double closestDist = Double.MAX_VALUE;

        for (CommonGroundLocation cg : locations) {
            double dist = Math.sqrt(Math.pow(cg.lat - midLat, 2) + Math.pow(cg.lon - midLon, 2));
            if (dist < closestDist) {
                closestDist = dist;
                closestCG = cg;
            }
        }
        return closestCG;
    }
//pushes TransactionMeetupLocation to database once the common ground is found
    public void updateTransactionMeetupLocation(int transactionID, String address) {
        String sql = "UPDATE transactions SET meetup_address = ? WHERE transaction_id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, address);
            pstmt.setInt(2, transactionID);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Success: Meetup address updated for transaction " + transactionID);
            } else {
                System.out.println("Warning: No transaction found with ID " + transactionID);
            }
        } catch (SQLException e) {
            System.err.println("Database error during update: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
