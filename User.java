package model;

// Abstract base class: Admin, Artist, Enthusiast isse inherit karte hain
public abstract class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String status;

    public User(int id, String name, String email, String password, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = status;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getStatus() { return status; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setStatus(String status) { this.status = status; }

    // Polymorphism: har role apna role aur dashboard naam batayega
    public abstract String getRole();
    public abstract String getDashboardTitle();

    @Override
    public String toString() {
        return id + " | " + name + " | " + email + " | " + getRole() + " | " + status;
    }
}
