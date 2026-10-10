package ui;

import dao.ArtworkDAO;
import model.Cart;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

// Enthusiast: purchased artworks + wishlist
public class CollectionPanel extends JPanel implements Refreshable {
    private final User user;
    private final Cart cart;
    private final ArtworkDAO dao = new ArtworkDAO();
    private final JTabbedPane inner = new JTabbedPane();

    public CollectionPanel(User user, Cart cart) {
        this.user = user;
        this.cart = cart;
        setLayout(new BorderLayout());
        add(inner, BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        int selected = Math.max(0, inner.getSelectedIndex());
        inner.removeAll();
        try {
            inner.addTab("Purchased", UIUtil.cardGrid(dao.getPurchased(user.getId()), null));
            inner.addTab("Wishlist", UIUtil.cardGrid(dao.getWishlist(user.getId()), a -> {
                JButton cartBtn = new JButton("Add to Cart");
                JButton removeBtn = new JButton("Remove");
                cartBtn.addActionListener(e -> {
                    if ("APPROVED".equals(a.getStatus()))
                        UIUtil.info(this, cart.add(a) ? "Cart mein add ho gaya." : "Pehle se cart mein hai.");
                    else
                        UIUtil.info(this, "Ye artwork ab available nahi hai.");
                });
                removeBtn.addActionListener(e -> {
                    try {
                        dao.removeWishlist(user.getId(), a.getId());
                        refresh();
                    } catch (SQLException ex) {
                        UIUtil.error(this, ex);
                    }
                });
                return new JComponent[]{cartBtn, removeBtn};
            }));
            inner.setSelectedIndex(Math.min(selected, inner.getTabCount() - 1));
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
        revalidate();
        repaint();
    }
}
