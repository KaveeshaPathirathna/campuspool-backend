package backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {

    @Id
    private String studentId;
    private String name;
    private String campusMail;
    private String universityName;
    private String faculty;
    private String academicYear;
    private String gender;
    private String password;
    private Integer isVerified;

    // Default Constructor
    public Student() {}

    // Parameterized Constructor
    public Student(String studentId, String name, String campusMail, String universityName,
                   String faculty, String academicYear, String gender, String password, Integer isVerified) {
        this.studentId = studentId;
        this.name = name;
        this.campusMail = campusMail;
        this.universityName = universityName;
        this.faculty = faculty;
        this.academicYear = academicYear;
        this.gender = gender;
        this.password = password;
        this.isVerified = isVerified;
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCampusMail() {
        return campusMail;
    }

    public void setCampusMail(String campusMail) {
        this.campusMail = campusMail;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Integer isVerified) {
        this.isVerified = isVerified;
    }
}