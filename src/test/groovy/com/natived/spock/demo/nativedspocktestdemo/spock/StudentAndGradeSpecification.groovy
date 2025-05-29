package com.natived.spock.demo.nativedspocktestdemo.spock

import com.natived.spock.demo.nativedspocktestdemo.model.*
import com.natived.spock.demo.nativedspocktestdemo.repository.HistoryGradesDAO
import com.natived.spock.demo.nativedspocktestdemo.repository.MahtGradesDAO
import com.natived.spock.demo.nativedspocktestdemo.repository.ScienceGradesDAO
import com.natived.spock.demo.nativedspocktestdemo.repository.StudentDAO
import com.natived.spock.demo.nativedspocktestdemo.service.StudentAndGradeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestPropertySource
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@TestPropertySource("/application-test.properties")
@SpringBootTest
class StudentAndGradeServiceSpockTest extends Specification {

    @Autowired
    JdbcTemplate jdbc

    @Autowired
    StudentAndGradeService studentService

    @Autowired
    StudentDAO studentDao

    @Autowired
    MahtGradesDAO mathGradeDao

    @Autowired
    ScienceGradesDAO scienceGradeDao

    @Autowired
    HistoryGradesDAO historyGradeDao

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

    @Subject
    StudentAndGradeService service = new StudentAndGradeService()

    @Shared
    def studentEmail = "chad.darby@luv2code_school.com"

    @Unroll
    def 'createStudentService - #studentFirstName, #studentLastName, #studentEmail'() {
        when:
        service.createStudent(studentFirstName, studentLastName, studentEmail)

        then:
        def student = studentDao.findByEmailAddress(studentEmail)
        student.emailAddress == studentEmail

        where:
        studentFirstName | studentLastName | studentEmail
        "Chad" | "Darby" | "chad.darby@luv2code_school.com"
    }

    def 'isStudentNullCheck'() {
        expect:
        service.checkIfStudentIsNull(1) == true
        service.checkIfStudentIsNull(0) == false
    }

    def 'deleteStudentService'() {
        given:
        def deletedCollegeStudent = studentDao.findById(1)
        def deletedMathGrade = mathGradeDao.findById(1)
        def deletedHistoryGrade = historyGradeDao.findById(1)
        def deletedScienceGrade = scienceGradeDao.findById(1)

        expect:
        deletedCollegeStudent.isPresent() == true

        when:
        service.deleteStudent(1)

        then:
        studentDao.findById(1).isPresent() == false
        mathGradeDao.findById(1).isPresent() == false
        scienceGradeDao.findById(1).isPresent() == false
        historyGradeDao.findById(1).isPresent() == false
    }

}