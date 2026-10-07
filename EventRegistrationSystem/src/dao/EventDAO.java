package dao;

import model.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    public void createEvent(Event event) throws SQLException {
        if (event.getEventDate() != null) {
            try {
                java.time.LocalDate d = java.time.LocalDate.parse(event.getEventDate().trim());
                if (d.isBefore(java.time.LocalDate.now())) {
                    throw new IllegalArgumentException("Event date cannot be in the past.");
                }
            } catch (java.time.format.DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd.");
            }
        }
        String sql = "INSERT INTO events (event_name, event_date, venue, capacity) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, event.getEventName());
            stmt.setString(2, event.getEventDate());
            stmt.setString(3, event.getVenue());
            stmt.setInt(4, event.getCapacity());
            stmt.executeUpdate();
        }
    }

    public List<Event> getAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY event_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                events.add(mapRow(rs));
            }
        }
        return events;
    }

    public List<Event> searchEvent(String keyword) throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE event_name LIKE ? OR venue LIKE ? ORDER BY event_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRow(rs));
                }
            }
        }
        return events;
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        return new Event(
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("event_date"),
                rs.getString("venue"),
                rs.getInt("capacity")
        );
    }
}
