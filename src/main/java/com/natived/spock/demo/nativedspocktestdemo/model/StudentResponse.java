package com.natived.spock.demo.nativedspocktestdemo.model;

public class StudentResponse {

    private int id;
    private String firstname;
    private String lastname;
    private String emailAddress;
    private StudentGrades studentGrades;
    private String fullName;

    public StudentResponse(int id, String firstname, String lastname, String emailAddress, StudentGrades studentGrades) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.emailAddress = emailAddress;
        this.studentGrades = studentGrades;
        this.fullName = firstname + " " + lastname;
    }

    public int getId() { return id; }
    public String getFirstname() { return firstname; }
    public String getLastname() { return lastname; }
    public String getEmailAddress() { return emailAddress; }
    public StudentGrades getStudentGrades() { return studentGrades; }
    public String getFullName() { return fullName; }
}
