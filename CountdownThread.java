package util;

import model.Exhibition;

import javax.swing.*;
import java.time.Duration;
import java.time.LocalDateTime;

// THREAD: exhibition ka live countdown har second update karta hai
public class CountdownThread extends Thread {
    private final Exhibition exhibition;
    private final JLabel label;

    public CountdownThread(Exhibition exhibition, JLabel label) {
        super("countdown-" + exhibition.getId());
        this.exhibition = exhibition;
        this.label = label;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            String text = describe();
            SwingUtilities.invokeLater(() -> label.setText(text)); // UI update EDT par
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private String describe() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exhibition.getStartTime()))
            return "Starts in " + format(Duration.between(now, exhibition.getStartTime()));
        if (now.isBefore(exhibition.getEndTime()))
            return "LIVE now! Ends in " + format(Duration.between(now, exhibition.getEndTime()));
        return "Exhibition ended";
    }

    private String format(Duration d) {
        return String.format("%dd %02d:%02d:%02d", d.toDays(), d.toHours() % 24, d.toMinutes() % 60, d.getSeconds() % 60);
    }
}
