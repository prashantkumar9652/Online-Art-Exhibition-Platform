package ui;

import dao.ArtworkDAO;
import model.Cart;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

// Enthusiast: gallery browse, search, cart / wishlist mein add
public class GalleryPanel extends JPanel implements Refreshable {
    private final User user;
    private final Cart cart;
    private final ArtworkDAO dao = new ArtworkDAO();
    private final JTextField searchF = new JTextField(20);
    private JComponent content;

    public GalleryPanel(User user, Cart cart) {
        this.user = user;
        this.cart = cart;
        setLayout(new BorderLayout());
        JButton search = new JButton("Search");
        JButton all = new JButton("Show All");
        add(UIUtil.row(new JLabel("Title / Artist:"), searchF, search, all), BorderLayout.NORTH);
        search.addActionListener(e -> refresh());
        searchF.addActionListener(e -> refresh());
        all.addActionListener(e -> {
            searchF.setText("");
            refresh();
        });
        refresh();
    }

    @Override
    public void refresh() {
        if (content != null) remove(content);
        try {
            content = UIUtil.cardGrid(dao.searchApproved(searchF.getText().trim()), a -> {
                JButton cartBtn = new JButton("Add to Cart");
                JButton wishBtn = new JButton("Wishlist");
                JButton viewBtn = new JButton("View");
                cartBtn.addActionListener(e ->
                        UIUtil.info(this, cart.add(a) ? "Cart mein add ho gaya." : "Ye artwork pehle se cart mein hai."));
                wishBtn.addActionListener(e -> {
                    try {
                        UIUtil.info(this, dao.addWishlist(user.getId(), a.getId())
                                ? "Wishlist mein add ho gaya." : "Pehle se wishlist mein hai.");
                    } catch (SQLException ex) {
                        UIUtil.error(this, ex);
                    }
                });
                viewBtn.addActionListener(e -> UIUtil.showImage(this, a));
                return new JComponent[]{cartBtn, wishBtn, viewBtn};
            });
        } catch (SQLException e) {
            content = new JLabel("Error: " + e.getMessage());
        }
        add(content, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
