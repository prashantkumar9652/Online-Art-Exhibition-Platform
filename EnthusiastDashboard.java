package ui;

import model.Cart;
import model.User;

import javax.swing.*;

public class EnthusiastDashboard extends BaseDashboard {
    public EnthusiastDashboard(User user) {
        super(user);
        init();
    }

    @Override
    protected JComponent buildContent() {
        Cart cart = new Cart(); // is session ka cart, panels ke beech shared
        JTabbedPane tabs = createTabs();
        tabs.addTab("Galleries", new GalleryPanel(user, cart));
        tabs.addTab("Exhibitions", new EnthusiastExhibitionPanel(user));
        tabs.addTab("Purchase", new PurchasePanel(user, cart));
        tabs.addTab("My Collection", new CollectionPanel(user, cart));
        tabs.addTab("Feedback", new EnthusiastFeedbackPanel(user));
        return tabs;
    }
}
