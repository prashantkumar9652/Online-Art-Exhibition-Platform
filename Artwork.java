package model;

public class Artwork {
    private int id;
    private int artistId;
    private String title;
    private String description;
    private String imagePath;
    private double price;
    private String status; // PENDING, APPROVED, REJECTED, SOLD
    private String artistName;

    public Artwork(int id, int artistId, String title, String description,
                   String imagePath, double price, String status) {
        this.id = id;
        this.artistId = artistId;
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
        this.price = price;
        this.status = status;
    }

    public int getId() { return id; }
    public int getArtistId() { return artistId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getArtistName() { return artistName; }
    public void setStatus(String status) { this.status = status; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    @Override
    public String toString() {
        return id + " | " + title + " | Rs." + price + " | " + status;
    }
}
