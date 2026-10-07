package dao;

import model.Participant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParticipantDAO {

    public void registerParticipant(Participant p) throws SQLException {
        String sql = "INSERT INTO participants (event_id, participant_name, email, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, p.getEventId());
            stmt.setString(2, p.getParticipantName());
            stmt.setString(3, p.getEmail());
            stmt.setString(4, p.getPhone());
            stmt.executeUpdate();
        }
    }

    public List<Participant> getAllParticipants() throws SQLException {
        List<Participant> list = new ArrayList<>();
        String sql = "SELECT * FROM participants ORDER BY participant_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Participant> getParticipantsByEvent(int eventId) throws SQLException {
        List<Participant> list = new ArrayList<>();
        String sql = "SELECT * FROM participants WHERE event_id = ? ORDER BY participant_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Participant mapRow(ResultSet rs) throws SQLException {
        return new Participant(
                rs.getInt("participant_id"),
                rs.getInt("event_id"),
                rs.getString("participant_name"),
                rs.getString("email"),
                rs.getString("phone")
        );
    }
}
