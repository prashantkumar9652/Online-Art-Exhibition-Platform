package model;

public class Admin extends User {
    public Admin(int id, String name, String email, String password, String status) {
        super(id, name, email, password, status);
    }

    @Override
    public String getRole() { return "ADMIN"; }

    @Override
    public String getDashboardTitle() { return "Admin Dashboard"; }
}
