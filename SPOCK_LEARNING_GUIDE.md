# Spock Testing Guide — From JUnit to Spock

> **Who this is for:** Developers who know JUnit 5 and want to understand Spock.
> Every concept is explained by comparing it to something you already know.
> All examples come directly from `StudentAndGradeServiceSpec.groovy` in this project.

---

## Table of Contents

1. [What is Spock and why bother?](#1-what-is-spock-and-why-bother)
2. [Project setup](#2-project-setup)
3. [Anatomy of a Spock test — the six blocks](#3-anatomy-of-a-spock-test--the-six-blocks)
4. [Full comparison tables](#4-full-comparison-tables)
5. [Real example A — `expect:` + `where:`](#5-real-example-a--expect--where)
6. [Real example B — `when:` / `then:`](#6-real-example-b--when--then)
7. [The single-quote `@Value` rule](#7-the-single-quote-value-rule)
8. [Power Assert — what failure output looks like](#8-power-assert--what-failure-output-looks-like)
9. [Running your tests](#9-running-your-tests)
10. [TODO exercises](#10-todo-exercises)
11. [Quick-reference card](#11-quick-reference-card)

---

## 1. What is Spock and why bother?

Spock is a testing framework for Java and Groovy applications.
You write tests in Groovy, and they run on JUnit Platform — so they plug straight into Gradle, Maven, and your IDE exactly like JUnit tests.

**Three things that make Spock stand out:**

### Test names are sentences
```groovy
// JUnit
@Test
public void checkIfStudentExists() { … }

// Spock — the name IS the requirement
def "student with id 1 exists, student with id 0 does not"() { … }
```
The test report reads like a list of requirements, not a list of method names.

### Parameterised tests with zero boilerplate
```groovy
// JUnit — needs @ParameterizedTest, @MethodSource, a separate method, and casting
// Spock — just a table at the bottom of the test
where:
studentId | expected
1         | true
0         | false
```
Spock runs the test once per row and automatically names each run using the column values.

### Failure messages show you what went wrong — automatically
When an assertion fails, Spock prints the actual value of every sub-expression without you having to write `"expected X but got Y"` messages.

---

## 2. Project setup

### build.gradle (the relevant parts)
```groovy
plugins {
    id 'groovy'               // enables Groovy compilation for test sources
}

dependencies {
    testImplementation 'org.codehaus.groovy:groovy:3.0.18'
    testImplementation 'org.spockframework:spock-core:2.4-M4-groovy-3.0'
    testImplementation 'org.spockframework:spock-spring:2.4-M4-groovy-3.0'
    testImplementation 'com.h2database:h2'    // in-memory DB for tests
}

tasks.named('test') {
    useJUnitPlatform()        // Spock 2.x runs on JUnit Platform
}
```

### Where test files live
```
src/
  test/
    groovy/                  ← Spock specs go here (.groovy files)
      com/natived/…/spock/
        StudentAndGradeServiceSpec.groovy
    java/                    ← JUnit tests stay here (.java files)
      com/natived/…/junit/
        StudentAndGradeServiceJUnitTest.java
```

### The minimum class skeleton
```groovy
import spock.lang.Specification

class MySpec extends Specification {
    // fields, lifecycle methods, and feature methods go here
}
```
The only structural requirement: **extend `Specification`**.
That base class provides all the blocks (`given:`, `when:`, `then:`, etc.) and the assertion engine.

---

## 3. Anatomy of a Spock test — the six blocks

A Spock test method is called a **feature method**. It is divided into labelled **blocks** that tell both Spock and the reader what each line is doing.

```groovy
def "full student information is returned for a known student"() {

    given: "a student id that was inserted by setup()"   // ← optional — describe preconditions
    int id = 1

    when: "we request the student's information"         // ← the action under test
    GradebookCollegeStudent student = studentService.studentInformation(id)

    then: "the student object is populated correctly"    // ← assertions
    student != null
    student.firstname == "Eric"
}
```

| Block | What goes in it | JUnit equivalent |
|---|---|---|
| `given:` | Arrange: set up variables and preconditions | Code before the "act" line |
| `when:` | Act: the **one** thing being tested | The single method call being tested |
| `then:` | Assert: boolean expressions — each line must be true | `assertEquals`, `assertTrue`, `assertNotNull` |
| `expect:` | Shorthand `when:` + `then:` for simple checks with no separate action | A single `assertTrue` |
| `where:` | Data table — Spock runs the method once per row | `@ParameterizedTest` + `@MethodSource` |
| `and:` | Continuation of the previous block (improves readability) | — |

### When to use `given/when/then` vs `expect:`

**Use `given/when/then`** when there is a meaningful *action* worth labelling:
```groovy
when: "we call studentInformation"
GradebookCollegeStudent student = studentService.studentInformation(1)

then: "the result is correct"
student.firstname == "Eric"
```

**Use `expect:`** when you are just asserting a fact with no action worth calling out:
```groovy
expect:
studentService.checkIfStudentIsNull(1) == true
```

---

## 4. Full comparison tables

### 4a — Lifecycle methods

| JUnit 5 annotation | Spock method | When it runs |
|---|---|---|
| `@BeforeEach` | `def setup()` | Before **every** feature method |
| `@AfterEach` | `def cleanup()` | After **every** feature method |
| `@BeforeAll` | `def setupSpec()` | Once before **all** feature methods in the class |
| `@AfterAll` | `def cleanupSpec()` | Once after **all** feature methods in the class |

This project uses `setup()` and `cleanup()` to insert and delete test data before and after each test:

```groovy
// StudentAndGradeServiceSpec.groovy  lines 69-81

def setup() {
    jdbc.execute(sqlAddStudent)       // insert student Eric Roby id=1
    jdbc.execute(sqlAddMathGrade)     // insert one math grade for id=1
    jdbc.execute(sqlAddScienceGrade)
    jdbc.execute(sqlAddHistoryGrade)
}

def cleanup() {
    jdbc.execute(sqlDeleteStudent)    // wipe data so tests stay independent
    jdbc.execute(sqlDeleteMathGrade)
    jdbc.execute(sqlDeleteScienceGrade)
    jdbc.execute(sqlDeleteHistoryGrade)
}
```

> **Why does this matter?** The H2 test database starts completely empty. Without `setup()`,
> there is no student with `id=1` and every assertion against that student will fail.
> Without `cleanup()`, leftover rows from one test can break another test.

### 4b — Class-level annotations

| JUnit 5 | Spock | Notes |
|---|---|---|
| `public class … {` | `class … extends Specification {` | Must extend `Specification` |
| `@SpringBootTest` | `@SpringBootTest` | Identical — this is a Spring annotation, not JUnit/Spock |
| `@TestPropertySource(…)` | `@TestPropertySource(…)` | Identical |
| `@Disabled` | `@Ignore` | Skips the test |
| `@ExtendWith(X.class)` | _not needed for Spring_ | `spock-spring` handles Spring integration automatically |

### 4c — Field injection

| JUnit 5 | Spock | Notes |
|---|---|---|
| `@Autowired private JdbcTemplate jdbc;` | `@Autowired JdbcTemplate jdbc` | No `private`, no semicolon — Groovy style |
| `@Value("${prop}")` | `@Value('${prop}')` | **Must use single quotes** in Groovy — see section 7 |

### 4d — Test methods

| JUnit 5 | Spock | Notes |
|---|---|---|
| `@Test public void myTest() { … }` | `def "description of test"() { … }` | No `@Test`, no return type, name is a string |
| `@Test @Disabled` | `@Ignore def "…"() { … }` | |

### 4e — Assertions

| JUnit 5 assertion | Spock `then:` / `expect:` expression | Notes |
|---|---|---|
| `assertEquals(expected, actual)` | `actual == expected` | Plain `==` — Groovy overloads it |
| `assertNotNull(x)` | `x != null` | |
| `assertNull(x)` | `x == null` | |
| `assertTrue(x)` | `x` or `x == true` | Any truthy expression works |
| `assertFalse(x)` | `!x` or `x == false` | |
| `assertThrows(Ex.class, () -> …)` | `thrown(Ex)` in `then:` | |
| `assertAll(…)` | Multiple lines in `then:` | Every line is independently checked |

---

## 5. Real example A — `expect:` + `where:`

**Location:** `StudentAndGradeServiceSpec.groovy` lines 95–103

**JUnit original** (two separate tests):
```java
@Test
public void isStudentNullCheck() {
    assertTrue(studentService.checkIfStudentIsNull(1));   // student id=1 exists
    assertFalse(studentService.checkIfStudentIsNull(0));  // student id=0 does not
}
```

**Spock version** (one parameterised feature method):
```groovy
def "checkIfStudentIsNull: id=#studentId should return #expected"() {  // (A)
    expect:                                                              // (B)
    studentService.checkIfStudentIsNull(studentId) == expected          // (C)

    where:                                                               // (D)
    studentId | expected                                                 // (E)
    1         | true     // student exists  → assertTrue in JUnit        // (F)
    0         | false    // student missing → assertFalse in JUnit
}
```

**Line-by-line:**

**(A)** The method name contains `#studentId` and `#expected`.
Spock replaces these placeholders with the actual values from the `where:` table when naming each test run.
In the report you see:
```
✓ checkIfStudentIsNull: id=1 should return true
✓ checkIfStudentIsNull: id=0 should return false
```

**(B)** `expect:` is used here because there is no meaningful "action" to put in a `when:` block — we are just asserting a fact about a query method.

**(C)** A plain Groovy `==` replaces both `assertTrue` and `assertFalse`. The single expression covers both cases because `expected` comes from the `where:` table.

**(D)** The `where:` block contains the test data. Spock creates a separate test run for each row.

**(E)** Column headers become variable names used in `(C)`.

**(F)** Each row is one test case. Row 1 replaces `assertTrue(…(1))`. Row 2 replaces `assertFalse(…(0))`. Two JUnit tests become two rows in one Spock method.

---

## 6. Real example B — `when:` / `then:`

**Location:** `StudentAndGradeServiceSpec.groovy` lines 137–177

**JUnit original:**
```java
@Test
public void studentInformation() {
    GradebookCollegeStudent g = studentService.studentInformation(1);
    assertNotNull(g);
    assertEquals(1, g.getId());
    assertEquals("Eric", g.getFirstname());
    assertEquals("Roby", g.getLastname());
    assertEquals("eric.roby@luv2code_school.com", g.getEmailAddress());
    assertTrue(g.getStudentGrades().getMathGradeResults().size() == 1);
    assertTrue(g.getStudentGrades().getScienceGradeResults().size() == 1);
    assertTrue(g.getStudentGrades().getHistoryGradeResults().size() == 1);
}
```

**Spock version:**
```groovy
def "studentInformation returns full details for student id 1"() {
    // given: — setup() already inserted student Eric Roby (id=1)
    // with one grade row per subject, so no extra setup is needed.

    when: "we ask the service for all information about student id 1"   // (A)
    GradebookCollegeStudent student = studentService.studentInformation(1)

    then: "the returned object contains the correct student data and all grades"
    student != null                                           // assertNotNull  (B)
    student.id == 1                                          // assertEquals   (C)
    student.firstname == "Eric"                              // (D)
    student.lastname == "Roby"
    student.emailAddress == "eric.roby@luv2code_school.com"
    student.studentGrades.mathGradeResults.size() == 1      // assertTrue      (E)
    student.studentGrades.scienceGradeResults.size() == 1
    student.studentGrades.historyGradeResults.size() == 1
}
```

**Line-by-line:**

**(A)** `when:` labels the single method call being tested.
Using `when/then` is the right choice here because there IS a meaningful action (`studentInformation()`)
that is worth naming separately from the assertions.

**(B)** `student != null` replaces `assertNotNull(g)`. No message needed — if it fails, power-assert shows:
```
student != null
|
null
```

**(C)** `student.id == 1` replaces `assertEquals(1, g.getId())`.
Note the argument order is reversed from JUnit (`expected, actual` → `actual == expected`).

**(D)** Groovy **property syntax**: `student.firstname` compiles to `student.getFirstname()`.
You can use either form — the shorter form is idiomatic Groovy.

**(E)** `student.studentGrades.mathGradeResults.size() == 1` replaces:
`assertTrue(g.getStudentGrades().getMathGradeResults().size() == 1)`.
Groovy property syntax makes the chain shorter without losing clarity.

> **Key insight:** In JUnit, every assertion is a separate method call.
> In Spock, every line in `then:` is an independent boolean expression.
> They are all checked, and all failures are reported — exactly like `assertAll()` in JUnit 5.

---

## 7. The single-quote `@Value` rule

In Groovy there are two kinds of string literals:

| Syntax | Name | Behaviour |
|---|---|---|
| `'hello ${name}'` | Plain string (`GString` disabled) | `${name}` kept literally |
| `"hello ${name}"` | GString | Groovy evaluates `name` as a variable at runtime |

Spring's `@Value` annotation needs the literal text `${sql.script.create.student}` so it can pass it to Spring's property resolver. If you use double quotes, Groovy evaluates the expression BEFORE Spring sees it.

```groovy
// CORRECT — single quotes preserve the placeholder for Spring
@Value('${sql.script.create.student}')
String sqlAddStudent

// WRONG — Groovy tries to evaluate sql.script.create.student as a variable
// Compile error: No such property: sql
@Value("${sql.script.create.student}")
String sqlAddStudent
```

**Rule: always use single quotes around `@Value` placeholders in Groovy.**

This affects lines 35–57 of `StudentAndGradeServiceSpec.groovy`.

---

## 8. Power Assert — what failure output looks like

When a `then:` expression is false, Spock does not just say "assertion failed".
It prints the actual value of every part of the expression:

**Example:** Suppose the student's first name was stored as `"John"` instead of `"Eric"`:
```
Condition not satisfied:

student.firstname == "Eric"
|       |         |
|       "John"    false
GradebookCollegeStudent(id=1, firstname=John, ...)
```

You see immediately: `student` exists, `student.firstname` is `"John"`, and `"John" == "Eric"` is `false`.
No need to write `assertEquals("Eric", student.getFirstname(), "First name did not match")`.

**Another example** — a size assertion fails:
```
Condition not satisfied:

student.studentGrades.mathGradeResults.size() == 1
|       |             |                |      |
|       |             []               0      false
GradebookCollegeStudent(id=1, ...)
```

The empty list `[]` and size `0` are shown automatically, pointing directly at the problem.

---

## 9. Running your tests

```bash
# Make gradlew executable (one-time setup on macOS/Linux)
chmod +x ./gradlew

# Run only the Spock spec
./gradlew test --tests "*.StudentAndGradeServiceSpec"

# Run all tests (JUnit + Spock)
./gradlew test

# Force a clean run (clears cache, re-downloads if needed)
./gradlew clean test
```

**View the HTML report** (renders in any browser):
```
build/reports/tests/test/index.html
```

The report shows each test name (including parameterised row names from `where:` tables),
pass/fail status, duration, and the full power-assert output for any failures.

---

## 10. TODO exercises

The JUnit tests in `StudentAndGradeServiceJUnitTest.java` are your reference.
Convert each one to Spock in `StudentAndGradeServiceSpec.groovy`.
The `setup()` and `cleanup()` lifecycle methods are already in place — you only need to add the feature methods.

---

### TODO 1 — Easy: delete a student (given/when/then)

**JUnit method to look at:** `deleteStudentService()` in `StudentAndGradeServiceJUnitTest.java`

**Spock pattern to use:** `given:` / `when:` / `then:`

**What to test:**
- Before deletion: student and all grade rows exist (`findById(1).isPresent() == true`)
- Call `studentService.deleteStudent(1)`
- After deletion: student and all grade rows no longer exist

**Hint:** You will need to `@Autowired` the four DAO beans (`StudentDAO`, `MahtGradesDAO`, `ScienceGradesDAO`, `HistoryGradesDAO`) into the spec — add them as fields alongside `studentService`.

```groovy
def "deleting student id 1 removes the student and all associated grades"() {
    given: "student and all grades exist"
    // assert precondition here with studentDao.findById(1).isPresent()

    when: "student is deleted"
    // call studentService.deleteStudent(1)

    then: "student and all grade rows are gone"
    // assert !studentDao.findById(1).isPresent() and same for each grade DAO
}
```

---

### TODO 2 — Easy: null result for missing student (expect:)

**JUnit method to look at:** `studentInformationServiceReturnNull()`

**Spock pattern to use:** `expect:` (no separate action needed)

**What to test:** `studentService.studentInformation(0)` returns `null` because id=0 doesn't exist.

**Hint:** A single `expect:` line is all you need.

```groovy
def "studentInformation returns null for a non-existent student"() {
    expect:
    // write a single assertion here
}
```

---

### TODO 3 — Medium: delete grade returns student id (expect: + where:)

**JUnit method to look at:** `deleteGradeService()`

**Spock pattern to use:** `expect:` + `where:` data table

**What to test:** Deleting grade id=1 for each subject returns student id=1.

**Hint:** The three JUnit assertions (`assertEquals(1, studentService.deleteGrade(1, "math"))` etc.)
become three rows in a `where:` table, with columns `gradeType` and `expectedStudentId`.

```groovy
def "deleteGrade for #gradeType returns student id #expectedStudentId"() {
    expect:
    studentService.deleteGrade(1, gradeType) == expectedStudentId

    where:
    gradeType  | expectedStudentId
    // fill in the three rows
}
```

---

### TODO 4 — Medium: invalid grade delete returns 0 (where:)

**JUnit method to look at:** `deleteGradeServiceReturnStudentIdOfZero()`

**Spock pattern to use:** `expect:` + `where:`

**What to test:** Deleting with a non-existent id or a bad grade type returns 0.

**Hint:** The two `assertEquals(0, …)` calls become two rows.

---

### TODO 5 — Medium: extend the existing where: table

**JUnit method to look at:** `createGradeServiceReturnFalse()`

**Spock test to extend:** `"create Grade Service should return false"` (lines 106–114)

The existing `where:` table already has three rows.
The JUnit test has a commented-out fourth case: `studentService.createGrade(80.50, 2, "math")` — a grade for a student that doesn't exist.

**What to do:** Add a fourth row to the `where:` table for that case and verify it also returns `false`.

**Hint:** Just add one line to the `where:` table — no other changes needed.

---

### TODO 6 — Challenge: write a NEW test not in the JUnit suite

The JUnit suite doesn't have a test that verifies the student is gone *after* deletion.
Write a test that does this end-to-end in Spock using `given/when/then`.

**What to test:**
1. `checkIfStudentIsNull(1)` returns `true` (student exists, inserted by `setup()`)
2. Call `deleteStudent(1)`
3. `checkIfStudentIsNull(1)` now returns `false` (student is gone)

**Hint:** This is a three-block test. The `given:` block just documents the precondition
(no code needed since `setup()` already inserted the student). The `when:` block calls `deleteStudent(1)`.
The `then:` block makes the final assertion.

---

## 11. Quick-reference card

### Minimum Spock class
```groovy
import spock.lang.Specification

class MySpec extends Specification {

    def setup()   { /* runs before each test */ }
    def cleanup() { /* runs after  each test */ }

    def "what this test verifies"() {
        given: "precondition"
        // arrange

        when: "action"
        // act

        then: "expected outcome"
        // assert (boolean expressions, one per line)
    }
}
```

### All six blocks
```groovy
given:  // preconditions
when:   // action under test
then:   // assertions (boolean lines)
expect: // given + when + then combined (for simple checks)
where:  // data table (runs test once per row)
and:    // continues the previous block
```

### Lifecycle
| JUnit 5 | Spock |
|---|---|
| `@BeforeEach` | `def setup()` |
| `@AfterEach` | `def cleanup()` |
| `@BeforeAll` | `def setupSpec()` |
| `@AfterAll` | `def cleanupSpec()` |

### Assertions
| JUnit 5 | Spock |
|---|---|
| `assertEquals(a, b)` | `a == b` |
| `assertNotNull(x)` | `x != null` |
| `assertNull(x)` | `x == null` |
| `assertTrue(x)` | `x` |
| `assertFalse(x)` | `!x` |
| `assertThrows(Ex, …)` | `thrown(Ex)` in `then:` |

### Spring Boot integration
```groovy
@SpringBootTest
@TestPropertySource("/application-test.properties")
class MySpec extends Specification {

    @Autowired SomeService service        // same as JUnit
    @Value('${my.property}') String prop  // single quotes — required in Groovy
}
```

### Data-driven test skeleton
```groovy
def "description with #variable interpolation"() {
    expect:
    someMethod(input) == expected

    where:
    input | expected
    1     | true
    0     | false
}
```

### Running tests
```bash
./gradlew test --tests "*.MySpec"        # one spec
./gradlew test                           # all tests
./gradlew clean test                     # force clean run
# Report: build/reports/tests/test/index.html
```
