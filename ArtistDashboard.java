package ui;

import model.User;

import javax.swing.*;

public class ArtistDashboard extends BaseDashboard {
    public ArtistDashboard(User user) {
        super(user);
        init();
    }

    @Override
    protected JComponent buildContent() {
        JTabbedPane tabs = createTabs();
        tabs.addTab("Upload Artwork", new ArtistUploadPanel(user));
        tabs.addTab("Exhibitions", new ArtistExhibitionPanel(user));
        tabs.addTab("Feedback", new ArtistFeedbackPanel(user));
        tabs.addTab("Portfolio", new ArtistPortfolioPanel(user));
        tabs.addTab("Sales", new ArtistSalesPanel(user));
        return tabs;
    }
}
