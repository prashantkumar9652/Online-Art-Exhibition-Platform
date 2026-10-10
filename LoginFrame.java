package ui;

import dao.UserDAO;
import exception.InvalidLoginException;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passField = new JPasswordField(20);
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Online Art Exhibition - Login");
        setSize(420, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 5, 8, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Art Exhibition Platform", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(heading, c);

        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0; panel.add(new JLabel("Email:"), c);
        c.gridx = 1; panel.add(emailField, c);
        c.gridy = 2; c.gridx = 0; panel.add(new JLabel("Password:"), c);
        c.gridx = 1; panel.add(passField, c);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.add(loginBtn);
        btns.add(registerBtn);
        c.gridy = 3; c.gridx = 0; c.gridwidth = 2;
        panel.add(btns, c);

        loginBtn.addActionListener(e -> doLogin());
        passField.addActionListener(e -> doLogin()); // Enter dabane par bhi login
        registerBtn.addActionListener(e -> {
            dispose();
            new RegisterFrame().setVisible(true);
        });

        add(panel);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email aur password dono daalo.");
            return;
        }

        try {
            User user = userDAO.login(email, password);
            openDashboard(user);
        } catch (InvalidLoginException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Role ke hisaab se sahi dashboard
    private void openDashboard(User user) {
        BaseDashboard dashboard;
        if (user instanceof Admin) dashboard = new AdminDashboard(user);
        else if (user instanceof Artist) dashboard = new ArtistDashboard(user);
        else dashboard = new EnthusiastDashboard(user);

        dispose();
        dashboard.setVisible(true);
    }
}
