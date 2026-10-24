import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Simple in-memory model of an Online Examination System.
 * Used by Q13 (positive) and Q14 (negative) test programs.
 */
public class ExamApp {

    public static class AppException extends RuntimeException {
        public AppException(String message) { super(message); }
    }

    public static class Question {
        public final int id, correct, marks;
        public final String text;
        public String currentText;
        public final List<String> options;
        Question(int id, String text, List<String> options, int correct, int marks) {
            this.id = id; this.text = text; this.currentText = text; this.options = options;
            this.correct = correct; this.marks = marks;
        }
    }

    /** What the browser receives: no correct answer is ever included. */
    public static class ClientQuestion {
        public final int id;
        public final String text;
        public final List<String> options;
        ClientQuestion(int id, String text, List<String> options) { this.id = id; this.text = text; this.options = options; }
    }

    public static class Exam {
        public final int id, durationMin, totalMarks;
        public final String title, subject;
        public final LocalDateTime start, end;
        public final List<Question> questions = new ArrayList<>();
        public final Set<String> assigned = new HashSet<>();
        Exam(int id, String title, String subject, int durationMin, int totalMarks, LocalDateTime start, LocalDateTime end) {
            this.id = id; this.title = title; this.subject = subject; this.durationMin = durationMin;
            this.totalMarks = totalMarks; this.start = start; this.end = end;
        }
    }

    public static class Attempt {
        public final String student;
        public final Exam exam;
        public final LocalDateTime startedAt;
        public final Map<Integer, Integer> answers = new HashMap<>();
        public final Set<Integer> review = new HashSet<>();
        public final List<Integer> order = new ArrayList<>();
        public boolean submitted;
        public int score, tabSwitches;
        Attempt(String student, Exam exam, LocalDateTime startedAt) { this.student = student; this.exam = exam; this.startedAt = startedAt; }
    }

    private static class User { String password, role; }

    public static final int MAX_TAB_SWITCHES = 3;

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, String> sessions = new HashMap<>();
    private final Map<Integer, Exam> exams = new LinkedHashMap<>();
    private final Map<String, Attempt> attempts = new HashMap<>();
    private final List<String> proctorLog = new ArrayList<>();
    private int examSeq = 1, questionSeq = 1;

    public void addUser(String id, String password, String role) {
        User u = new User(); u.password = password; u.role = role; users.put(id, u);
    }

    // ---------- Login ----------
    public void login(String id, String password, String device) {
        User u = users.get(id);
        if (u == null || !u.password.equals(password)) throw new AppException("Invalid credentials");
        String existing = sessions.get(id);
        if (existing != null && !existing.equals(device)) throw new AppException("Already logged in on another device");
        sessions.put(id, device);
    }
    public void logout(String id) { sessions.remove(id); }
    private User requireSession(String id) {
        if (!sessions.containsKey(id)) throw new AppException("Please login");
        return users.get(id);
    }
    private void requireStaff(String id) {
        String r = requireSession(id).role;
        if (!r.equals("ADMIN") && !r.equals("TEACHER")) throw new AppException("Access denied");
    }
    public void openAdminPage(String id) {
        if (!"ADMIN".equals(requireSession(id).role)) throw new AppException("Access denied");
    }

    // ---------- Exam and question management ----------
    public Exam createExam(String staffId, String title, String subject, int durationMin, int totalMarks,
                           LocalDateTime start, LocalDateTime end) {
        requireStaff(staffId);
        if (title == null || title.trim().isEmpty()) throw new AppException("Required field: title");
        if (durationMin <= 0) throw new AppException("Enter valid duration");
        if (totalMarks <= 0) throw new AppException("Enter valid marks");
        if (!end.isAfter(start)) throw new AppException("End time must be after start time");
        Exam e = new Exam(examSeq++, title, subject, durationMin, totalMarks, start, end);
        exams.put(e.id, e);
        return e;
    }
    public Exam exam(int id) { return exams.get(id); }

    public Question addQuestion(String staffId, int examId, String text, List<String> options, int correctIndex, int marks) {
        requireStaff(staffId);
        if (text == null || text.trim().isEmpty()) throw new AppException("Question text is required");
        if (options == null || options.size() < 2) throw new AppException("At least two options required");
        if (correctIndex < 0 || correctIndex >= options.size()) throw new AppException("Select the correct answer");
        Question q = new Question(questionSeq++, text, options, correctIndex, marks);
        exams.get(examId).questions.add(q);
        return q;
    }

    public void editQuestion(String staffId, int examId, int qid, String newText) {
        requireStaff(staffId);
        for (Question q : exams.get(examId).questions) if (q.id == qid) { q.currentText = newText; return; }
        throw new AppException("Question not found");
    }
    public String questionText(int examId, int qid) {
        for (Question q : exams.get(examId).questions) if (q.id == qid) return q.currentText;
        return null;
    }

    public void assign(int examId, String studentId) { exams.get(examId).assigned.add(studentId); }

    public List<Exam> upcomingExams(String studentId, LocalDateTime now) {
        requireSession(studentId);
        List<Exam> out = new ArrayList<>();
        for (Exam e : exams.values()) if (e.assigned.contains(studentId) && e.end.isAfter(now)) out.add(e);
        return out;
    }

    // ---------- Attempt ----------
    private static String key(String student, int examId) { return student + "#" + examId; }

    public Attempt start(String studentId, int examId, LocalDateTime now) {
        requireSession(studentId);
        Exam e = exams.get(examId);
        if (e == null || !e.assigned.contains(studentId)) throw new AppException("Access denied");
        Attempt existing = attempts.get(key(studentId, examId));
        if (existing != null && existing.submitted) throw new AppException("Exam already submitted");
        if (now.isBefore(e.start)) throw new AppException("Exam not yet started");
        if (now.isAfter(e.end)) throw new AppException("Exam has ended");
        if (existing != null) return existing; // resume
        Attempt a = new Attempt(studentId, e, now);
        for (Question q : e.questions) a.order.add(q.id);
        Collections.shuffle(a.order, new Random(studentId.hashCode()));
        attempts.put(key(studentId, examId), a);
        return a;
    }

    public List<ClientQuestion> questionsFor(Attempt a) {
        List<ClientQuestion> out = new ArrayList<>();
        for (int qid : a.order)
            for (Question q : a.exam.questions)
                if (q.id == qid) out.add(new ClientQuestion(q.id, q.currentText, new ArrayList<>(q.options)));
        return out;
    }

    /** Remaining seconds, computed on the server clock. */
    public long secondsLeft(Attempt a, LocalDateTime now) {
        long left = a.exam.durationMin * 60L - Duration.between(a.startedAt, now).getSeconds();
        return Math.max(0, left);
    }

    private Attempt liveAttempt(String studentId, int examId, LocalDateTime now) {
        requireSession(studentId);
        Attempt a = attempts.get(key(studentId, examId));
        if (a == null) throw new AppException("Exam not started");
        if (a.submitted) throw new AppException("Exam already submitted");
        if (secondsLeft(a, now) == 0) { finish(a); throw new AppException("Exam time over. Auto-submitted"); }
        return a;
    }

    public void answer(String studentId, int examId, int qid, int option, LocalDateTime now) {
        liveAttempt(studentId, examId, now).answers.put(qid, option);
    }
    public void markForReview(String studentId, int examId, int qid, LocalDateTime now) {
        liveAttempt(studentId, examId, now).review.add(qid);
    }

    private void finish(Attempt a) {
        if (a.submitted) return;
        int score = 0;
        for (Question q : a.exam.questions) {
            Integer given = a.answers.get(q.id);
            if (given != null && given == q.correct) score += q.marks;
        }
        a.score = score;
        a.submitted = true;
    }

    public void submit(String studentId, int examId, LocalDateTime now) {
        requireSession(studentId);
        Attempt a = attempts.get(key(studentId, examId));
        if (a == null) throw new AppException("Exam not started");
        if (a.submitted) throw new AppException("Exam already submitted");
        finish(a);
    }

    /** The client-supplied score is ignored: evaluation is always done on the server. */
    public void submit(String studentId, int examId, LocalDateTime now, int clientScore) { submit(studentId, examId, now); }

    /** Called by the server on a schedule: submits any attempt whose time is over. */
    public void autoSubmitExpired(LocalDateTime now) {
        for (Attempt a : attempts.values()) if (!a.submitted && secondsLeft(a, now) == 0) finish(a);
    }

    public Attempt attempt(String studentId, int examId) { return attempts.get(key(studentId, examId)); }

    public int result(String studentId, int examId) {
        Attempt a = attempts.get(key(studentId, examId));
        if (a == null || !a.submitted) throw new AppException("Result not available");
        return a.score;
    }

    public int rank(String studentId, int examId) {
        int myScore = result(studentId, examId);
        int rank = 1;
        for (Attempt a : attempts.values())
            if (a.exam.id == examId && a.submitted && a.score > myScore) rank++;
        return rank;
    }

    // ---------- Proctoring ----------
    public int tabSwitch(String studentId, int examId, LocalDateTime now) {
        Attempt a = liveAttempt(studentId, examId, now);
        a.tabSwitches++;
        proctorLog.add("TAB_SWITCH " + studentId + " #" + a.tabSwitches);
        if (a.tabSwitches >= MAX_TAB_SWITCHES) finish(a);
        return a.tabSwitches;
    }

    /** Returns true when the action is blocked. */
    public boolean shortcut(String studentId, String action) {
        boolean blocked = Arrays.asList("copy", "paste", "rightclick", "printscreen").contains(action);
        if (blocked) proctorLog.add("BLOCKED " + action + " by " + studentId);
        return blocked;
    }

    public void flagMultipleFaces(String studentId) { proctorLog.add("MULTIPLE_FACES " + studentId); }
    public List<String> proctorLog() { return proctorLog; }

    // ---------- Reports ----------
    public String attendanceReport(String staffId, int examId) {
        requireStaff(staffId);
        int count = 0, total = 0;
        for (Attempt a : attempts.values()) if (a.exam.id == examId) { count++; total += a.score; }
        return "participants=" + count + ",average=" + (count == 0 ? 0 : total / (double) count);
    }
}
