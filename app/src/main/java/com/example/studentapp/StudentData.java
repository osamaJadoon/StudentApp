package com.example.studentapp;

public class StudentData {
    private String studentName;
    private Integer studentRollNo;
    private String studentDepartment;
    private String studentEmail;
    private String studentPassword;
    private String status;

    public StudentData() {
    }

    public StudentData(String studentName, Integer studentRollNo, String studentDepartment, String studentEmail, String studentPassword, String status) {
        this.studentName = studentName;
        this.studentRollNo = studentRollNo;
        this.studentDepartment = studentDepartment;
        this.studentEmail = studentEmail;
        this.studentPassword = studentPassword;
        this.status = status;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Integer getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(Integer studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public String getStudentDepartment() {
        return studentDepartment;
    }

    public void setStudentDepartment(String studentDepartment) {
        this.studentDepartment = studentDepartment;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentPassword() {
        return studentPassword;
    }

    public void setStudentPassword(String studentPassword) {
        this.studentPassword = studentPassword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
