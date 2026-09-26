package qpal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import qpal.model.Route;
import qpal.util.DbConnection;

public class RouteDao {

    public List<Route> getAllRoutes() {

        List<Route> routes = new ArrayList<>();

        String sql = "SELECT * FROM routes ORDER BY origin, destination";

        try (
                Connection con = DbConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {

                Route route = new Route();

                route.setRouteID(rs.getInt("route_id"));
                route.setOrigin(rs.getString("origin"));
                route.setDestination(rs.getString("destination"));
                route.setFare(rs.getDouble("fare"));
                route.setStatus(rs.getString("status"));

                routes.add(route);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return routes;
    }

    public boolean addRoute(Route route) {

        String sql = "INSERT INTO routes (origin, destination, fare, status) VALUES (?, ?, ?, ?)";

        try (
                Connection con = DbConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, route.getOrigin());
            pst.setString(2, route.getDestination());
            pst.setDouble(3, route.getFare());
            pst.setString(4, route.getStatus());

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Route getRoute(int id) {

        String sql = "SELECT * FROM routes WHERE route_id=?";

        try (
                Connection con = DbConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, id);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                Route route = new Route();

                route.setRouteID(rs.getInt("route_id"));
                route.setOrigin(rs.getString("origin"));
                route.setDestination(rs.getString("destination"));
                route.setFare(rs.getDouble("fare"));
                route.setStatus(rs.getString("status"));

                return route;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}