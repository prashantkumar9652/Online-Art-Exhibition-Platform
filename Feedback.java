package model;

public class Feedback {
    private int id;
    private int senderId;
    private String senderName;
    private int artworkId;
    private String artworkTitle;
    private String message;
    private String sentAt;

    public Feedback(int id, int senderId, String senderName, int artworkId,
                    String artworkTitle, String message, String sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.artworkId = artworkId;
        this.artworkTitle = artworkTitle;
        this.message = message;
        this.sentAt = sentAt;
    }

    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public int getArtworkId() { return artworkId; }
    public String getArtworkTitle() { return artworkTitle == null ? "-" : artworkTitle; }
    public String getMessage() { return message; }
    public String getSentAt() { return sentAt; }
}
