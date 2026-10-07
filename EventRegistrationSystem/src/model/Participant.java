package model;

/**
 * Represents one participant registered to an event.
 */
public class Participant {

    private int participantId;
    private int eventId;
    private String participantName;
    private String email;
    private String phone;

    public Participant() {
    }

    public Participant(int eventId, String participantName, String email, String phone) {
        this.eventId = eventId;
        this.participantName = participantName;
        this.email = email;
        this.phone = phone;
    }

    public Participant(int participantId, int eventId, String participantName, String email, String phone) {
        this.participantId = participantId;
        this.eventId = eventId;
        this.participantName = participantName;
        this.email = email;
        this.phone = phone;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return participantName + " <" + email + ">";
    }
}
