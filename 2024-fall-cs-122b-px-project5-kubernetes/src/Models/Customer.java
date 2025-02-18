package Models;

import com.google.gson.annotations.Expose;

public class Customer {
    @Expose
    private int id;
    @Expose
    private String firstName;
    @Expose
    private String lastName;
    private String ccId;
    private String address;
    private String email;
    private String password;
    public Customer() {
        // TODO Auto-generated constructor stub
    }
    public Customer(int id, String firstName, String lastName, String ccId, String address, String email,
                    String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.ccId = ccId;
        this.address = address;
        this.email = email;
        this.password = password;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getCcId() {
        return ccId;
    }
    public void setCcId(String ccId) {
        this.ccId = ccId;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
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


}