package ui;

import model.User;
import util.NotificationWatcher;

import javax.swing.*;
import java.awt.*;

// Teeno dashboards ka common base: top bar (welcome + logout), neeche notification bar.
// Har dashboard apna content buildContent() mein deta hai (polymorphism).
public abstract class BaseDashboard extends JFrame {
    protected final User user;
    private final JLabel statusLabel = new JLabel(" ");
    private NotificationWatcher watcher;

    public BaseDashboard(User user) {
        this.user = user;
        setTitle(user.getDashboardTitle() + " - " + user.getName());
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    // Subclass ke constructor ke end mein call hota hai (taaki subclass fields ready hon)
    protected final void init() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel welcome = new JLabel("Welcome, " + user.getName() + " (" + user.getRole() + ")");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        top.add(welcome, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        add(top, BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // THREAD: background mein notifications check hoti rehti hain
        watcher = new NotificationWatcher(msg -> SwingUtilities.invokeLater(() -> statusLabel.setText("Notification - " + msg)));
        Thread t = new Thread(watcher, "notification-watcher");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void dispose() {
        if (watcher != null) watcher.stop();
        super.dispose();
    }

    // Tab badalne par Refreshable panel apna data reload kar leta hai
    protected JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof Refreshable) ((Refreshable) c).refresh();
        });
        return tabs;
    }

    protected abstract JComponent buildContent();
}
