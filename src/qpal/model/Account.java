package qpal.model;

public class Account {
    
    private int id;
    private String name;
    private String email;
    private String password;
    private String role;
    private String status;
    private String profileImage;

    public Account() {

    }

    public Account(int id, String name, String email, String password, String role, String status) {
        
        this.id = id;
        this.name = name; 
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        
    }

    public int getID() {
        return id;
    
    }

    public void setID(int id) {
        this.id = id;

    }

    public String getName() {
        return name;

    }

    public void setName(String name) {
        this.name = name;

    }


    public String getEmail() {
        return email;

    }

    public void setEmail(String email) {
        this.email = email;

    }

    public String getPassword() {
        return password;

    }
    
    public void setPassword(String password) {
        this.password = password;
        
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
        
    }

    public void setStatus(String status) {
        this.status = status;

    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}