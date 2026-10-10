package model;

import java.time.LocalDateTime;

public class Exhibition {
    private int id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Exhibition(int id, String title, String description, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    // Time ke hisaab se status: UPCOMING / LIVE / ENDED
    public String getStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) return "UPCOMING";
        if (now.isAfter(endTime)) return "ENDED";
        return "LIVE";
    }

    @Override
    public String toString() { return title; }
}
