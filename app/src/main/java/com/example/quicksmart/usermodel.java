package com.example.quicksmart;

public class usermodel {
    String name, email, phn_no, role, driverStatus, profilePic, uid, idProofBase64;
    boolean blocked;

    public usermodel() {
    }

    public usermodel(String name, String email, String phn_no) {
        this.name = name;
        this.email = email;
        this.phn_no = phn_no;
        this.role = "user";
        this.blocked = false;
        this.driverStatus = "unverified";
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhn_no() { return phn_no; }
    public void setPhn_no(String phn_no) { this.phn_no = phn_no; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public String getDriverStatus() { return driverStatus; }
    public void setDriverStatus(String driverStatus) { this.driverStatus = driverStatus; }

    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getIdProofBase64() { return idProofBase64; }
    public void setIdProofBase64(String idProofBase64) { this.idProofBase64 = idProofBase64; }
}
