package dao;

import model.Participant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A "registration" is a participant's entry linked to an event, so this DAO
 * reads/updates/deletes rows in the participants table.
 */
public class RegistrationDAO {

    public Participant getParticipantById(int participantId) throws SQLException {
        String sql = "SELECT * FROM participants WHERE participant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, participantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Participant(
                            rs.getInt("participant_id"),
                            rs.getInt("event_id"),
                            rs.getString("participant_name"),
                            rs.getString("email"),
                            rs.getString("phone")
                    );
                }
            }
        }
        return null;
    }

    public boolean updateRegistration(Participant p) throws SQLException {
        String sql = "UPDATE participants SET participant_name = ?, email = ?, phone = ? WHERE participant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getParticipantName());
            stmt.setString(2, p.getEmail());
            stmt.setString(3, p.getPhone());
            stmt.setInt(4, p.getParticipantId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean cancelRegistration(int participantId) throws SQLException {
        String sql = "DELETE FROM participants WHERE participant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, participantId);
            return stmt.executeUpdate() > 0;
        }
    }
}
