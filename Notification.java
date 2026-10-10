package model;

public class Notification {
    private int id;
    private String message;
    private String createdAt;

    public Notification(int id, String message, String createdAt) {
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getMessage() { return message; }
    public String getCreatedAt() { return createdAt; }
}
