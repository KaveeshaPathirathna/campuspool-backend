package backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "students")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Student {

    @Id
    @Column(name = "student_id")
    private String studentId;

    private String name;

    @Column(name = "campus_mail")
    private String campusMail;

    @Column(name = "university_name")
    private String universityName;

    private String faculty;

    @Column(name = "academic_year")
    private String academicYear;

    private String gender;

    // 🔒 SECURITY FIX: Password එක JSON Requests වලට විතරක් (WRITE_ONLY) ගන්නවා,
    // API Response වලට (Read කරද්දී) Send වෙන්නෙ නැහැ.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "is_verified")
    private Integer isVerified = 0;

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

    // 🎯 FIX: Overriding equals() & hashCode() based on studentId for accurate Collection comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(studentId != null ? studentId.toUpperCase() : null,
                student.studentId != null ? student.studentId.toUpperCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId != null ? studentId.toUpperCase() : null);
    }
}