package Map;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/get-meetup", "/calculate-closest"})
public class MapServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();

        if ("/get-meetup".equals(path)) {
            handleGetMeetup(req, resp);
        } else if ("/calculate-closest".equals(path)) {
            handleCalculateClosest(req, resp);
        }
    }

    private void handleGetMeetup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id parameter");
            return;
        }

        int transactionId = Integer.parseInt(idParam);
        TransactionInfoPulling puller = new TransactionInfoPulling();
        User[] users = puller.pullUsers(transactionId);

        if (users[0] != null && users[1] != null) {
            resp.setContentType("text/plain");
            PrintWriter out = resp.getWriter();
            out.print(users[0].address + "|" + users[1].address);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Transaction not found");
        }
    }

    private void handleCalculateClosest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String latBParam = req.getParameter("latB");
        String lonBParam = req.getParameter("lonB");
        String latSParam = req.getParameter("latS");
        String lonSParam = req.getParameter("lonS");

        if (idParam == null || latBParam == null || lonBParam == null || latSParam == null || lonSParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        int id = Integer.parseInt(idParam);
        double latB = Double.parseDouble(latBParam);
        double lonB = Double.parseDouble(lonBParam);
        double latS = Double.parseDouble(latSParam);
        double lonS = Double.parseDouble(lonSParam);

        LocationMapping mapping = new LocationMapping();
        CommonGroundLocation closest = mapping.findClosestCG(latB, lonB, latS, lonS);
        mapping.updateTransactionMeetupLocation(id, closest.address);

        resp.setContentType("text/plain");
        resp.getWriter().print(closest.name);
    }
}
