package models;

public class User {
    private String uid;
    private String fullName;
    private String contact;
    private String academicStanding;
    private String role;
    private String course_id;
    private String email;
    private boolean hasNewAppointment;

    public User() {}

    public User(String uid, String fullName, String contact, String academicStanding, String role, String course_id, String email, boolean hasNewAppointment) {
        this.uid = uid;
        this.fullName = fullName;
        this.contact = contact;
        this.academicStanding = academicStanding;
        this.role = role;
        this.course_id = course_id;
        this.email = email;
        this.hasNewAppointment = hasNewAppointment;
    }

    // Existing getters and setters
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

    public String getCourse_id() { return course_id; }
    public void setCourse_id(String course_id) { this.course_id = course_id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isHasNewAppointment() { return hasNewAppointment; }
    public void setHasNewAppointment(boolean hasNewAppointment) { this.hasNewAppointment = hasNewAppointment; }
}
