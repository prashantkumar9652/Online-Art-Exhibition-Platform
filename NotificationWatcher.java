package util;

import dao.NotificationDAO;
import model.Notification;

import java.util.function.Consumer;

// THREAD: har 5 second mein DB check karta hai ki nayi notification aayi ya nahi
public class NotificationWatcher implements Runnable {
    private final Consumer<String> onMessage;
    private final NotificationDAO dao = new NotificationDAO();
    private volatile boolean running = true;
    private int lastId = -1;

    public NotificationWatcher(Consumer<String> onMessage) {
        this.onMessage = onMessage;
    }

    public void stop() { running = false; }

    @Override
    public void run() {
        while (running) {
            try {
                Notification n = dao.latest();
                if (n != null && n.getId() != lastId) {
                    String prefix = (lastId == -1) ? "Latest: " : "New: ";
                    lastId = n.getId();
                    onMessage.accept(prefix + n.getMessage());
                }
            } catch (Exception ignored) {
                // DB temporarily down ho to agli baar try karenge
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
