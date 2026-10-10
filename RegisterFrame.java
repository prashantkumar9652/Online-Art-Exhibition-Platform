package ui;

import dao.UserDAO;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class RegisterFrame extends JFrame {
    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passField = new JPasswordField(20);
    private final JPasswordField confirmField = new JPasswordField(20);
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"Art Enthusiast", "Artist"});
    private final UserDAO userDAO = new UserDAO();

    public RegisterFrame() {
        setTitle("Online Art Exhibition - Register");
        setSize(440, 430);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 5, 8, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Create Account", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(heading, c);

        c.gridwidth = 1;
        addRow(panel, c, 1, "Name:", nameField);
        addRow(panel, c, 2, "Email:", emailField);
        addRow(panel, c, 3, "Password:", passField);
        addRow(panel, c, 4, "Confirm:", confirmField);
        addRow(panel, c, 5, "Role:", roleBox);

        JButton registerBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to Login");
        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.add(registerBtn);
        btns.add(backBtn);
        c.gridy = 6; c.gridx = 0; c.gridwidth = 2;
        panel.add(btns, c);

        registerBtn.addActionListener(e -> doRegister());
        backBtn.addActionListener(e -> goToLogin());

        add(panel);
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridy = row; c.gridx = 0; panel.add(new JLabel(label), c);
        c.gridx = 1; panel.add(field, c);
    }

    private void doRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword());
        String confirm = new String(confirmField.getPassword());

        // Validation
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showMsg("Saari fields bharo.");
            return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            showMsg("Sahi email daalo (jaise name@gmail.com).");
            return;
        }
        if (pass.length() < 6) {
            showMsg("Password kam se kam 6 characters ka hona chahiye.");
            return;
        }
        if (!pass.equals(confirm)) {
            showMsg("Dono password match nahi kar rahe.");
            return;
        }

        // Role ke hisaab se sahi subclass (Admin sirf DB se banega, yahan nahi)
        User newUser;
        if ("Artist".equals(roleBox.getSelectedItem()))
            newUser = new Artist(0, name, email, pass, "ACTIVE");
        else
            newUser = new Enthusiast(0, name, email, pass, "ACTIVE");

        try {
            userDAO.add(newUser);
            showMsg("Registration successful! Ab login karo.");
            goToLogin();
        } catch (SQLIntegrityConstraintViolationException ex) {
            showMsg("Ye email pehle se registered hai.");
        } catch (SQLException ex) {
            showMsg("Database error: " + ex.getMessage());
        }
    }

    private void goToLogin() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}
