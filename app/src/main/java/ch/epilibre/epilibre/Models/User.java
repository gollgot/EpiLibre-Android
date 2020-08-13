package ch.epilibre.epilibre.Models;

public class User {

    private int id;
    private String firstname;
    private String lastname;
    private String email;
    private Role role;
    private String rolePretty;
    private String tokenAPI;

    /**
     * Empty constructor
     */
    public User(int id, String firstname, String lastname, String email, Role role, String rolePretty, String tokenAPI) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.role = role;
        this.rolePretty = rolePretty;
        this.tokenAPI = tokenAPI;
    }

    /**** GETTERS ****/
    public int getId() {
        return id;
    }
    public String getFirstname() {
        return firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public String getEmail() {
        return email;
    }
    public Role getRole() {
        return role;
    }
    public String getRolePretty() {
        return rolePretty;
    }
    public String getTokenAPI() {
        return tokenAPI;
    }

}
