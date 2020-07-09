package ch.epilibre.epilibre.user;

public class User {

    private String firstname;
    private String lastname;
    private String email;
    private Role role;

    /**
     * Empty constructor
     */
    public User(String firstname, String lastname, String email, Role role) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.role = role;
    }

    /**** GETTERS ****/
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

}
