package model;

public class Artist extends User {
    public Artist(int id, String name, String email, String password, String status) {
        super(id, name, email, password, status);
    }

    @Override
    public String getRole() { return "ARTIST"; }

    @Override
    public String getDashboardTitle() { return "Artist Dashboard"; }
}
