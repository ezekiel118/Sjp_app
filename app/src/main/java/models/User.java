package models;

public class User {
    private String uid;
    private String fullName;
    private String contact;
    private String academicStanding;
    private String role;

    public User() {}



    public User(String uid, String fullName, String contact, String academicStanding, String role) {
        this.uid = uid;
        this.fullName = fullName;
        this.contact = contact;
        this.academicStanding = academicStanding;
        this.role = role;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getAcademicStanding() { return academicStanding; }
    public void setAcademicStanding(String academicStanding) { this.academicStanding = academicStanding; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }


}
