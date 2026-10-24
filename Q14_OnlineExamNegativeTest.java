import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Q14. Online Examination System - NEGATIVE test cases (TC-N01 .. TC-N22)
 * Run:  javac *.java  &&  java Q14_OnlineExamNegativeTest
 */
public class Q14_OnlineExamNegativeTest {

    static int passed = 0, failed = 0;

    static void check(String id, String description, boolean condition) {
        if (condition) { passed++; System.out.println("PASS  " + id + " - " + description); }
        else { failed++; System.out.println("FAIL  " + id + " - " + description); }
    }

    static void expectError(String id, String description, String expectedText, Runnable action) {
        try {
            action.run();
            check(id, description + " (expected an error, none thrown)", false);
        } catch (ExamApp.AppException e) {
            check(id, description + " -> \"" + e.getMessage() + "\"",
                    e.getMessage().toLowerCase().contains(expectedText.toLowerCase()));
        }
    }

    public static void main(String[] args) {
        final ExamApp app = new ExamApp();
        app.addUser("stu1", "pw1", "STUDENT");
        app.addUser("stu2", "pw2", "STUDENT");
        app.addUser("stu3", "pw3", "STUDENT");
        app.addUser("teacher1", "tp", "TEACHER");
        app.addUser("admin1", "ap", "ADMIN");

        final LocalDateTime start = LocalDateTime.of(2026, 10, 22, 10, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 10, 22, 12, 0);

        // ---- Login ----
        expectError("TC-N01", "Wrong password rejected", "invalid credentials", () -> app.login("stu1", "bad", "d1"));
        app.login("stu1", "pw1", "laptop-1");
        expectError("TC-N02", "Second device login blocked while first session is active", "another device",
                () -> app.login("stu1", "pw1", "phone-2"));
        app.login("stu2", "pw2", "laptop-2");
        app.login("stu3", "pw3", "laptop-3");
        app.login("teacher1", "tp", "pc-t");
        app.login("admin1", "ap", "pc-a");

        // ---- Exam creation ----
        expectError("TC-N03a", "Zero duration rejected", "valid duration",
                () -> app.createExam("admin1", "T", "S", 0, 50, start, end));
        expectError("TC-N03b", "Negative duration rejected", "valid duration",
                () -> app.createExam("admin1", "T", "S", -30, 50, start, end));
        expectError("TC-N04", "End time before start time rejected", "after start time",
                () -> app.createExam("admin1", "T", "S", 60, 50, end, start));
        expectError("TC-N05", "Blank title rejected", "title",
                () -> app.createExam("admin1", "", "S", 60, 50, start, end));

        final ExamApp.Exam exam = app.createExam("admin1", "Java Unit Test 1", "Java", 60, 50, start, end);
        final List<String> options = Arrays.asList("A", "B", "C", "D");

        // ---- Question validation ----
        expectError("TC-N06", "MCQ without a correct option rejected", "correct answer",
                () -> app.addQuestion("teacher1", exam.id, "Q?", options, -1, 2));
        expectError("TC-N07", "Blank question text rejected", "required",
                () -> app.addQuestion("teacher1", exam.id, "", options, 0, 2));
        expectError("TC-N07b", "Single option rejected", "two options",
                () -> app.addQuestion("teacher1", exam.id, "Q?", Arrays.asList("A"), 0, 2));
        for (int i = 1; i <= 5; i++) app.addQuestion("teacher1", exam.id, "Question " + i, options, 0, 10);

        app.assign(exam.id, "stu1");
        app.assign(exam.id, "stu2");
        // stu3 is NOT assigned

        // ---- Time window and assignment ----
        expectError("TC-N08", "Start before the exam opens", "not yet started",
                () -> app.start("stu1", exam.id, start.minusMinutes(5)));
        expectError("TC-N09", "Start after the exam window closed", "has ended",
                () -> app.start("stu1", exam.id, end.plusMinutes(1)));
        expectError("TC-N10", "Unassigned student cannot open the exam", "access denied",
                () -> app.start("stu3", exam.id, start.plusMinutes(1)));

        // ---- Network failure / browser closed / timer tampering ----
        final LocalDateTime t0 = start.plusMinutes(1);
        final ExamApp.Attempt a1 = app.start("stu1", exam.id, t0);
        final List<ExamApp.ClientQuestion> qs = app.questionsFor(a1);
        app.answer("stu1", exam.id, qs.get(0).id, 0, t0.plusMinutes(2));    // auto-saved before disconnect
        ExamApp.Attempt back = app.start("stu1", exam.id, t0.plusMinutes(4)); // reconnect after 2 minutes
        check("TC-N11", "After network loss the saved answer is retained", back.answers.get(qs.get(0).id) == 0);
        check("TC-N12", "Reopening does not grant extra time (remaining = 56 min)", app.secondsLeft(back, t0.plusMinutes(4)) == 56 * 60);
        // (Server clock based: started at t0, now t0+4min -> 56 minutes left.)
        check("TC-N13", "Timer is computed on the server, not by the client clock",
                app.secondsLeft(back, t0.plusMinutes(10)) == 50 * 60);

        // ---- Resubmission, blank submission ----
        app.submit("stu1", exam.id, t0.plusMinutes(20));
        expectError("TC-N14", "Re-attempt after submission blocked", "already submitted",
                () -> app.start("stu1", exam.id, t0.plusMinutes(21)));

        final ExamApp.Attempt a2 = app.start("stu2", exam.id, t0);
        app.submit("stu2", exam.id, t0.plusMinutes(5));
        check("TC-N15", "Submit with no answers allowed and scores 0", a2.submitted && app.result("stu2", exam.id) == 0);

        // ---- Proctoring (use a fresh exam so attempts are live) ----
        final ExamApp.Exam exam2 = app.createExam("admin1", "Proctored Test", "Java", 60, 50, start, end);
        app.addQuestion("teacher1", exam2.id, "Q1", options, 0, 10);
        app.assign(exam2.id, "stu1");
        final ExamApp.Attempt p = app.start("stu1", exam2.id, t0);
        check("TC-N16a", "1st tab switch gives a warning (count 1)", app.tabSwitch("stu1", exam2.id, t0.plusMinutes(1)) == 1);
        app.tabSwitch("stu1", exam2.id, t0.plusMinutes(2));
        app.tabSwitch("stu1", exam2.id, t0.plusMinutes(3));
        check("TC-N16b", "Exam auto-submitted after 3 tab switches", p.submitted);
        check("TC-N16c", "Tab-switch events logged for the proctor", app.proctorLog().size() >= 3);

        check("TC-N17a", "Right-click blocked", app.shortcut("stu1", "rightclick"));
        check("TC-N17b", "Copy blocked", app.shortcut("stu1", "copy"));
        check("TC-N17c", "Paste blocked", app.shortcut("stu1", "paste"));
        check("TC-N17d", "Print-screen blocked", app.shortcut("stu1", "printscreen"));

        app.flagMultipleFaces("stu1");
        check("TC-N18", "Multiple faces in webcam flagged for review", app.proctorLog().contains("MULTIPLE_FACES stu1"));

        // ---- Access control ----
        expectError("TC-N19", "Student cannot open admin pages", "access denied", () -> app.openAdminPage("stu1"));
        expectError("TC-N19b", "Student cannot create exams", "access denied",
                () -> app.createExam("stu1", "Hack", "S", 60, 50, start, end));
        expectError("TC-N19c", "Student cannot see reports", "access denied", () -> app.attendanceReport("stu1", exam.id));

        boolean leaks = false;
        for (Field f : ExamApp.ClientQuestion.class.getDeclaredFields())
            if (f.getName().toLowerCase().contains("correct") || f.getName().toLowerCase().contains("answer")) leaks = true;
        check("TC-N20", "Question data sent to the browser contains no correct-answer field", !leaks);

        // ---- Security ----
        expectError("TC-N21", "SQL-injection text treated as ordinary invalid credentials", "invalid credentials",
                () -> app.login("' OR '1'='1", "x", "d9"));

        final ExamApp.Exam exam3 = app.createExam("admin1", "Score Test", "Java", 60, 10, start, end);
        app.addQuestion("teacher1", exam3.id, "Q1", options, 0, 10);
        app.assign(exam3.id, "stu2");
        app.start("stu2", exam3.id, t0);
        app.submit("stu2", exam3.id, t0.plusMinutes(5), 10);    // client claims full marks, answered nothing
        check("TC-N22", "Client-supplied score ignored; server evaluates (score = 0)", app.result("stu2", exam3.id) == 0);

        System.out.println("\nResult: " + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }
}
