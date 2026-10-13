"""
EXPERIMENT 3 : Case study - ARMS (Academic Result Management System) portal
               Requirement analysis + NEGATIVE test scenarios & test cases.
"""
from arms_portal import ARMS
from testlib import run_suite, attempt

CASE_STUDY = """
CASE STUDY : ARMS PORTAL - REQUIREMENT ANALYSIS (see Experiment 2 for the full requirement list R1-R7)

NEGATIVE TESTING = invalid / unexpected / unauthorised inputs. The system must REJECT them gracefully with a
clear error message, without crashing and without changing data.
Negative scenario categories used here:
  A. Authentication failures      B. Authorisation (role) violations   C. Input validation (format, range, type)
  D. Business-rule violations     E. Session handling
"""

def admin():
    a = ARMS(); a.login("admin", "Admin@123"); return a

def with_student():
    a = admin(); a.add_student("2021123456", "Arun Kumar", "arun@college.edu", "CSE"); a.logout(); return a

def faculty():
    a = with_student(); a.login("faculty1", "Faculty@123"); return a

def published():
    a = faculty(); a.enter_marks("2021123456", "CS101", 90); a.logout()
    a.login("admin", "Admin@123"); a.publish_results(); a.logout(); return a

def lock3():
    a = ARMS()
    r = [attempt(a.login, "admin", "bad") for _ in range(3)]
    return r[-1]

def lock_then_correct():
    a = ARMS()
    for _ in range(3): attempt(a.login, "admin", "bad")
    return attempt(a.login, "admin", "Admin@123")

def student_other():
    a = published(); a.add_student if False else None
    a2 = ARMS(); a2.users = a.users; a2.students = a.students; a2.marks = a.marks; a2.published = True
    a2.users["2022000001"] = {"pwd": "Welcome@123", "role": "student"}
    a2.login("2022000001", "Welcome@123"); return attempt(a2.view_result, "2021123456")

def student_unpublished():
    a = with_student(); a.login("2021123456", "Welcome@123"); return attempt(a.view_result, "2021123456")

def duplicate():
    a = admin(); a.add_student("2021123456", "Arun Kumar", "arun@college.edu", "CSE")
    return attempt(a.add_student, "2021123456", "Another Name", "x@college.edu", "ECE")

def after_publish():
    a = published(); a.login("faculty1", "Faculty@123")
    return attempt(a.enter_marks, "2021123456", "CS101", 10)

def faculty_add():
    a = ARMS(); a.login("faculty1", "Faculty@123")
    return attempt(a.add_student, "2021123457", "Bala Kumar", "bala@college.edu", "CSE")

def student_enter_marks():
    a = with_student(); a.login("2021123456", "Welcome@123"); return attempt(a.enter_marks, "2021123456", "CS101", 99)

A = lambda f, *x: (lambda: attempt(f(), *x))   # helper:  A(admin_factory, ...) is used with bound methods below

cases = [
 # --- A. authentication
 ("N01","Login with empty username and password","'' / ''","ERROR: Username and password are required", lambda: attempt(ARMS().login, "", "")),
 ("N02","Login with wrong password","admin / wrong","ERROR: Invalid credentials", lambda: attempt(ARMS().login, "admin", "wrong")),
 ("N03","Login with unregistered user","ghost / Pass@123","ERROR: Invalid credentials", lambda: attempt(ARMS().login, "ghost", "Pass@123")),
 ("N04","Account locks on 3rd consecutive wrong password","3 x wrong password","ERROR: Account locked after 3 failed attempts", lock3),
 ("N05","Correct password is rejected once account is locked","3 wrong + 1 correct","ERROR: Account locked. Contact administrator", lock_then_correct),
 # --- B. authorisation
 ("N06","Faculty tries to add a student","faculty1 -> add_student","ERROR: Access denied: admin only", faculty_add),
 ("N07","Student tries to enter marks","student -> enter_marks","ERROR: Access denied: faculty only", student_enter_marks),
 ("N08","Student tries to view another student's result","2022000001 views 2021123456","ERROR: Access denied: you can view only your own result", student_other),
 # --- C. input validation
 ("N09","Register number with 9 digits","202112345","ERROR: Register number must be 10 digits", lambda: attempt(admin().add_student,"202112345","Arun Kumar","a@b.com","CSE")),
 ("N10","Register number with letters","20211234AB","ERROR: Register number must be 10 digits", lambda: attempt(admin().add_student,"20211234AB","Arun Kumar","a@b.com","CSE")),
 ("N11","Student name containing digits","Arun123","ERROR: Name must be 2-50 letters", lambda: attempt(admin().add_student,"2021123456","Arun123","a@b.com","CSE")),
 ("N12","Invalid email format","arun@@college","ERROR: Invalid email format", lambda: attempt(admin().add_student,"2021123456","Arun Kumar","arun@@college","CSE")),
 ("N13","Invalid department","DEPT = XYZ","ERROR: Invalid department", lambda: attempt(admin().add_student,"2021123456","Arun Kumar","a@b.com","XYZ")),
 ("N14","Mark above maximum (boundary 101)","CS101 = 101","ERROR: Mark must be between 0 and 100", lambda: attempt(faculty().enter_marks,"2021123456","CS101",101)),
 ("N15","Mark below minimum (boundary -1)","CS101 = -1","ERROR: Mark must be between 0 and 100", lambda: attempt(faculty().enter_marks,"2021123456","CS101",-1)),
 ("N16","Non-numeric mark","CS101 = 'abc'","ERROR: Mark must be numeric", lambda: attempt(faculty().enter_marks,"2021123456","CS101","abc")),
 ("N17","Marks for non-existent student","reg 9999999999","ERROR: Student not found", lambda: attempt(faculty().enter_marks,"9999999999","CS101",50)),
 ("N18","Marks for invalid subject code","XX999","ERROR: Invalid subject code", lambda: attempt(faculty().enter_marks,"2021123456","XX999",50)),
 ("N19","Search with only 2 characters","keyword 'Ar'","ERROR: Enter at least 3 characters to search", lambda: attempt(admin().search_student,"Ar")),
 ("N20","Weak new password","'abc123'","ERROR: Password must be 8+ chars with upper, lower, digit and special character", lambda: attempt(admin().change_password,"Admin@123","abc123")),
 ("N21","New password same as old password","Admin@123 -> Admin@123","ERROR: New password must differ from old password", lambda: attempt(admin().change_password,"Admin@123","Admin@123")),
 ("N22","Wrong old password during password change","old = 'Wrong@123'","ERROR: Old password is incorrect", lambda: attempt(admin().change_password,"Wrong@123","NewPass@123")),
 # --- D. business rules
 ("N23","Duplicate register number","same reg added twice","ERROR: Student already exists", duplicate),
 ("N24","Student views result before publication","view before publish","ERROR: Results not yet published", student_unpublished),
 ("N25","Faculty edits marks after results published","CS101 = 10 after publish","ERROR: Results already published; marks cannot be modified", after_publish),
 # --- E. session
 ("N26","Operation without logging in","add_student with no session","ERROR: Please login first", lambda: attempt(ARMS().add_student,"2021123456","Arun Kumar","a@b.com","CSE")),
 ("N27","Logout when not logged in","logout","ERROR: Please login first", lambda: attempt(ARMS().logout)),
]

if __name__ == "__main__":
    print(CASE_STUDY)
    run_suite("EXPERIMENT 3 - ARMS PORTAL : NEGATIVE TEST CASES", cases)
