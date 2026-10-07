package model;

/**
 * Represents one event. Plain data class (OOP: encapsulation via private
 * fields + public getters/setters).
 */
public class Event {

    private int eventId;
    private String eventName;
    private String eventDate; // stored as yyyy-MM-dd text for simplicity
    private String venue;
    private int capacity;

    public Event() {
    }

    public Event(String eventName, String eventDate, String venue, int capacity) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.venue = venue;
        this.capacity = capacity;
    }

    public Event(int eventId, String eventName, String eventDate, String venue, int capacity) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.venue = venue;
        this.capacity = capacity;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return eventName + " (" + eventDate + ", " + venue + ")";
    }
}
