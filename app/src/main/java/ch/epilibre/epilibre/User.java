package ch.epilibre.epilibre;

public class User {

    private int id;
    private String firstname;
    private String lastname;
    private String email;
    private Role role;

    /**
     * Empty constructor
     */
    public User(int id, String firstname, String lastname, String email, Role role) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.role = role;
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

}
