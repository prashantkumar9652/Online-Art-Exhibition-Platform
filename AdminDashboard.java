package ui;

import model.User;

import javax.swing.*;

public class AdminDashboard extends BaseDashboard {
    public AdminDashboard(User user) {
        super(user);
        init();
    }

    @Override
    protected JComponent buildContent() {
        JTabbedPane tabs = createTabs();
        tabs.addTab("User Management", new UserManagementPanel(user));
        tabs.addTab("Exhibition Content", new AdminContentPanel());
        tabs.addTab("System Settings", new SettingsPanel());
        tabs.addTab("Analytics", new AnalyticsPanel());
        tabs.addTab("Notifications", new AdminNotificationPanel());
        return tabs;
    }
}
