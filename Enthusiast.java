package model;

public class Enthusiast extends User {
    public Enthusiast(int id, String name, String email, String password, String status) {
        super(id, name, email, password, status);
    }

    @Override
    public String getRole() { return "ENTHUSIAST"; }

    @Override
    public String getDashboardTitle() { return "Art Enthusiast Dashboard"; }
}
