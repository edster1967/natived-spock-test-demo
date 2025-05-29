package com.natived.spock.demo.nativedspocktestdemo.spock

import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestPropertySource

import com.natived.spock.demo.nativedspocktestdemo.model.GradebookCollegeStudent
import com.natived.spock.demo.nativedspocktestdemo.service.StudentAndGradeService

@SpringBootTest
@TestPropertySource("/application-test.properties")
class StudentAndGradeServiceSpec extends Specification {

    // -----------------------------------------------------------------------
    // STEP 1 — Inject the Spring beans we need (same as @Autowired in JUnit)
    // -----------------------------------------------------------------------

    @Autowired
    JdbcTemplate jdbc                           // runs raw SQL against the H2 test DB

    @Autowired
    StudentAndGradeService studentService       // the class under test

    // -----------------------------------------------------------------------
    // STEP 2 — Inject the SQL strings from application-test.properties
    //
    // IMPORTANT: use single quotes ' ' around the placeholder.
    // Double quotes would make Groovy try to evaluate ${...} as a variable.
    // -----------------------------------------------------------------------

    @Value('${sql.script.create.student}')
    String sqlAddStudent

    @Value('${sql.script.create.math.grade}')
    String sqlAddMathGrade

    @Value('${sql.script.create.science.grade}')
    String sqlAddScienceGrade

    @Value('${sql.script.create.history.grade}')
    String sqlAddHistoryGrade

    @Value('${sql.script.delete.student}')
    String sqlDeleteStudent

    @Value('${sql.script.delete.math.grade}')
    String sqlDeleteMathGrade

    @Value('${sql.script.delete.science.grade}')
    String sqlDeleteScienceGrade

    @Value('${sql.script.delete.history.grade}')
    String sqlDeleteHistoryGrade

    // -----------------------------------------------------------------------
    // STEP 3 — Lifecycle methods
    //
    // setup()   runs before EVERY feature method  → replaces @BeforeEach
    // cleanup() runs after  EVERY feature method  → replaces @AfterEach
    //
    // The H2 database starts empty, so we insert a known student row (id=1)
    // before each test and wipe it afterwards to keep tests independent.
    // -----------------------------------------------------------------------

    def setup() {
        jdbc.execute(sqlAddStudent)
        jdbc.execute(sqlAddMathGrade)
        jdbc.execute(sqlAddScienceGrade)
        jdbc.execute(sqlAddHistoryGrade)
    }

    def cleanup() {
        jdbc.execute(sqlDeleteStudent)
        jdbc.execute(sqlDeleteMathGrade)
        jdbc.execute(sqlDeleteScienceGrade)
        jdbc.execute(sqlDeleteHistoryGrade)
    }

    // -----------------------------------------------------------------------
    // STEP 4 — The test itself
    //
    // JUnit original:
    //   assertTrue(studentService.checkIfStudentIsNull(1));   // student exists
    //   assertFalse(studentService.checkIfStudentIsNull(0));  // student missing
    //
    // Spock version uses expect: + where: to run both assertions as one
    // parameterised test.  Spock runs the method once per row in the table.
    // #studentId and #expected in the method name are filled in per row.
    // -----------------------------------------------------------------------

    def "checkIfStudentIsNull: id=#studentId should return #expected"() {
        expect:
        studentService.checkIfStudentIsNull(studentId) == expected

        where:
        studentId | expected
        1         | true     // student exists  → assertTrue in JUnit
        0         | false    // student missing → assertFalse in JUnit
    }


    def "create Grade Service should return false"() {
        expect:
        studentService.createGrade(grade, studentId, gradeType) == expected

        where:
        grade | studentId | gradeType | expected
        105   | 1         | "math "   | false
        -5    | 1         | "math "   | false
        80.5  | 1         | "literature " | false
    }




    // -----------------------------------------------------------------------
    // studentInformation test
    //
    // JUnit original:
    //   GradebookCollegeStudent g = studentService.studentInformation(1);
    //   assertNotNull(g);
    //   assertEquals(1,       g.getId());
    //   assertEquals("Eric",  g.getFirstname());
    //   assertEquals("Roby",  g.getLastname());
    //   assertEquals("eric.roby@luv2code_school.com", g.getEmailAddress());
    //   assertTrue(g.getStudentGrades().getMathGradeResults().size()    == 1);
    //   assertTrue(g.getStudentGrades().getScienceGradeResults().size() == 1);
    //   assertTrue(g.getStudentGrades().getHistoryGradeResults().size() == 1);
    //
    // Spock uses when/then — one action followed by multiple assertions.
    // -----------------------------------------------------------------------

    def "studentInformation returns full details for student id 1"() {
        // given: — setup() has already inserted student Eric Roby (id=1) with
        // one grade row per subject, so no extra setup is needed here.

        when: "we ask the service for all information about student id 1"
        // when: holds the single action under test.
        // studentInformation() fetches the student + all grade rows from the DB
        // and bundles them into a GradebookCollegeStudent object.
        GradebookCollegeStudent student = studentService.studentInformation(1)

        then: "the returned object contains the correct student data and all grades"
        // Every line in then: must evaluate to true — no assert methods needed.
        // When a line is false, Spock's power-assert prints the actual value,
        // so you can see exactly what went wrong without adding a message.

        // JUnit: assertNotNull(gradebookCollegeStudent)
        student != null

        // JUnit: assertEquals(1, gradebookCollegeStudent.getId())
        student.id == 1

        // JUnit: assertEquals("Eric", gradebookCollegeStudent.getFirstname())
        // Groovy property syntax: student.firstname == student.getFirstname()
        student.firstname == "Eric"

        // JUnit: assertEquals("Roby", gradebookCollegeStudent.getLastname())
        student.lastname == "Roby"

        // JUnit: assertEquals("eric.roby@luv2code_school.com", gradebookCollegeStudent.getEmailAddress())
        student.emailAddress == "eric.roby@luv2code_school.com"

        // JUnit: assertTrue(gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size() == 1)
        // setup() inserted exactly one math grade row for student id=1
        student.studentGrades.mathGradeResults.size() == 1

        // JUnit: assertTrue(gradebookCollegeStudent.getStudentGrades().getScienceGradeResults().size() == 1)
        student.studentGrades.scienceGradeResults.size() == 1

        // JUnit: assertTrue(gradebookCollegeStudent.getStudentGrades().getHistoryGradeResults().size() == 1)
        student.studentGrades.historyGradeResults.size() == 1
    }


}
