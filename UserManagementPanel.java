package ui;

import dao.NotificationDAO;
import dao.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

// Admin: user CRUD, block/unblock, password reset
public class UserManagementPanel extends JPanel implements Refreshable {
    private final User admin;
    private final UserDAO userDAO = new UserDAO();
    private final DefaultTableModel model = UIUtil.model("ID", "Name", "Email", "Role", "Status");
    private final JTable table = UIUtil.table(model);

    public UserManagementPanel(User admin) {
        this.admin = admin;
        setLayout(new BorderLayout());
        JButton addBtn = new JButton("Add User");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton blockBtn = new JButton("Block / Unblock");
        JButton resetBtn = new JButton("Reset Password");
        add(UIUtil.row(addBtn, editBtn, deleteBtn, blockBtn, resetBtn), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> addUser());
        editBtn.addActionListener(e -> editUser());
        deleteBtn.addActionListener(e -> deleteUser());
        blockBtn.addActionListener(e -> toggleBlock());
        resetBtn.addActionListener(e -> resetPassword());
        refresh();
    }

    @Override
    public void refresh() {
        try {
            List<User> users = userDAO.getAll();
            model.setRowCount(0);
            for (User u : users)
                model.addRow(new Object[]{u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getStatus()});
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private User selectedUser() throws SQLException {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.info(this, "Pehle table se ek user select karo.");
            return null;
        }
        return userDAO.getById((int) model.getValueAt(row, 0));
    }

    private void addUser() {
        User u = showUserDialog(null);
        if (u == null) return;
        try {
            userDAO.add(u);
            new NotificationDAO().add("New user added by admin: " + u.getName() + " (" + u.getRole() + ")");
            refresh();
        } catch (SQLIntegrityConstraintViolationException e) {
            UIUtil.info(this, "Ye email pehle se registered hai.");
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void editUser() {
        try {
            User existing = selectedUser();
            if (existing == null) return;
            User updated = showUserDialog(existing);
            if (updated == null) return;
            if (existing.getId() == admin.getId() && !"ADMIN".equals(updated.getRole())) {
                UIUtil.info(this, "Apna khud ka role change nahi kar sakte.");
                return;
            }
            userDAO.update(updated);
            UIUtil.info(this, "User update ho gaya.");
            refresh();
        } catch (SQLIntegrityConstraintViolationException e) {
            UIUtil.info(this, "Ye email kisi aur user ke paas hai.");
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void deleteUser() {
        try {
            User u = selectedUser();
            if (u == null) return;
            if (u.getId() == admin.getId()) {
                UIUtil.info(this, "Apna khud ka account delete nahi kar sakte.");
                return;
            }
            if (UIUtil.confirm(this, u.getName() + " ko delete karna hai? Uske artworks bhi hat jayenge.")) {
                userDAO.delete(u.getId());
                refresh();
            }
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void toggleBlock() {
        try {
            User u = selectedUser();
            if (u == null) return;
            if (u.getId() == admin.getId()) {
                UIUtil.info(this, "Apne aap ko block nahi kar sakte.");
                return;
            }
            u.setStatus("ACTIVE".equals(u.getStatus()) ? "BLOCKED" : "ACTIVE");
            userDAO.update(u);
            refresh();
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void resetPassword() {
        try {
            User u = selectedUser();
            if (u == null) return;
            String p = JOptionPane.showInputDialog(this, "Naya password for " + u.getName() + " (min 6 chars):");
            if (p == null) return;
            if (p.length() < 6) {
                UIUtil.info(this, "Password kam se kam 6 characters ka ho.");
                return;
            }
            userDAO.resetPassword(u.getId(), p);
            UIUtil.info(this, "Password reset ho gaya.");
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    // existing == null => Add mode, warna Edit mode
    private User showUserDialog(User existing) {
        JTextField name = new JTextField(existing == null ? "" : existing.getName(), 18);
        JTextField email = new JTextField(existing == null ? "" : existing.getEmail(), 18);
        JPasswordField pass = new JPasswordField(18);
        JComboBox<String> role = new JComboBox<>(new String[]{"ADMIN", "ARTIST", "ENTHUSIAST"});
        JComboBox<String> status = new JComboBox<>(new String[]{"ACTIVE", "BLOCKED"});
        if (existing != null) {
            role.setSelectedItem(existing.getRole());
            status.setSelectedItem(existing.getStatus());
        }
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Name:")); p.add(name);
        p.add(new JLabel("Email:")); p.add(email);
        if (existing == null) { p.add(new JLabel("Password:")); p.add(pass); }
        p.add(new JLabel("Role:")); p.add(role);
        p.add(new JLabel("Status:")); p.add(status);

        int r = JOptionPane.showConfirmDialog(this, p, existing == null ? "Add User" : "Edit User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return null;

        String n = name.getText().trim(), em = email.getText().trim(), pw = new String(pass.getPassword());
        if (n.isEmpty() || !em.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            UIUtil.info(this, "Sahi name aur email daalo.");
            return null;
        }
        if (existing == null && pw.length() < 6) {
            UIUtil.info(this, "Password kam se kam 6 characters ka ho.");
            return null;
        }
        return UserDAO.create((String) role.getSelectedItem(), existing == null ? 0 : existing.getId(),
                n, em, pw, (String) status.getSelectedItem());
    }
}
