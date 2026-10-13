"""
EXPERIMENT 2 : Case study - ARMS (Academic Result Management System) portal
               Requirement analysis + POSITIVE test scenarios & test cases.
"""
from arms_portal import ARMS, grade
from testlib import run_suite

CASE_STUDY = """
CASE STUDY : ARMS PORTAL - REQUIREMENT ANALYSIS
Actors      : Admin, Faculty, Student
Requirements:
  R1  Login with username & password (role based). Locks after 3 failed attempts.
  R2  Admin adds students (10-digit register no., name, valid email, department) - unique register number.
  R3  Faculty enters marks (0-100, numeric) per subject for a student; can edit until results are published.
  R4  Admin publishes results.
  R5  Student sees ONLY own result after publication: marks, grade, grade points, GPA (credit weighted).
  R6  Admin/Faculty can search a student by name / register no. (min 3 characters).
  R7  Any user can change password (strong password policy) and logout.
Grade table : 90-100 O(10) | 80-89 A+(9) | 70-79 A(8) | 60-69 B+(7) | 50-59 B(6) | <50 U(0, fail)

POSITIVE TESTING = valid inputs & expected usage; the system must accept them and give correct output.
"""

def admin():
    a = ARMS(); a.login("admin", "Admin@123"); return a

def with_student():
    a = admin(); a.add_student("2021123456", "Arun Kumar", "arun@college.edu", "CSE"); a.logout(); return a

def faculty_ready():
    a = with_student(); a.login("faculty1", "Faculty@123"); return a

def published():
    a = faculty_ready()
    a.enter_marks("2021123456", "CS101", 90); a.enter_marks("2021123456", "MA101", 75); a.logout()
    a.login("admin", "Admin@123"); a.publish_results(); a.logout()
    return a

def tc_student_view():
    a = published(); a.login("2021123456", "Welcome@123"); return a.view_result("2021123456")["gpa"]

def tc_overwrite():
    a = faculty_ready(); a.enter_marks("2021123456", "CS101", 40); a.enter_marks("2021123456", "CS101", 90)
    return a.marks["2021123456"]["CS101"]

def tc_fail_result():
    a = faculty_ready(); a.enter_marks("2021123456", "CS101", 49); a.logout()
    a.login("admin", "Admin@123"); a.publish_results(); a.logout(); a.login("2021123456", "Welcome@123")
    return a.view_result("2021123456")["result"]

def tc_pwd():
    a = admin(); r = a.change_password("Admin@123", "NewAdmin@456"); a.logout()
    return r + " | relogin: " + a.login("admin", "NewAdmin@456")

cases = [
 ("P01","Admin logs in with valid credentials","admin / Admin@123","Welcome admin (admin)", lambda: ARMS().login("admin","Admin@123")),
 ("P02","Faculty logs in with valid credentials","faculty1 / Faculty@123","Welcome faculty1 (faculty)", lambda: ARMS().login("faculty1","Faculty@123")),
 ("P03","Admin adds a student with valid details","2021123456, Arun Kumar, arun@college.edu, CSE","Student 2021123456 added",
        lambda: admin().add_student("2021123456","Arun Kumar","arun@college.edu","CSE")),
 ("P04","Student default login after creation","2021123456 / Welcome@123","Welcome 2021123456 (student)",
        lambda: with_student().login("2021123456","Welcome@123")),
 ("P05","Faculty enters valid marks","CS101 = 85","Marks saved: 2021123456 CS101 = 85",
        lambda: faculty_ready().enter_marks("2021123456","CS101",85)),
 ("P06","Boundary value - minimum mark 0 accepted","CS102 = 0","Marks saved: 2021123456 CS102 = 0",
        lambda: faculty_ready().enter_marks("2021123456","CS102",0)),
 ("P07","Boundary value - maximum mark 100 accepted","CS102 = 100","Marks saved: 2021123456 CS102 = 100",
        lambda: faculty_ready().enter_marks("2021123456","CS102",100)),
 ("P08","Faculty edits marks before publication (overwrite)","CS101 = 40 then 90","90", tc_overwrite),
 ("P09","Grade calculation - boundary 90","grade(90)","('O', 10)", lambda: grade(90)),
 ("P10","Grade calculation - boundary 50","grade(50)","('B', 6)", lambda: grade(50)),
 ("P11","Grade calculation - boundary 49 is a fail","grade(49)","('U', 0)", lambda: grade(49)),
 ("P12","Admin publishes results","publish","Results published", lambda: (lambda a: a.publish_results())(admin())),
 ("P13","Student views own GPA (CS101=90 O/4cr, MA101=75 A/4cr)","GPA=(4*10+4*8)/8","9.0", tc_student_view),
 ("P14","Overall result is FAIL when any subject < 50","CS101 = 49","FAIL", tc_fail_result),
 ("P15","Admin searches student by partial name","keyword 'Aru'","['2021123456 - Arun Kumar']",
        lambda: (lambda a: (a.login("admin","Admin@123"), a.search_student("Aru"))[1])(with_student())),
 ("P16","User changes password with strong new password","Admin@123 -> NewAdmin@456","Password changed successfully | relogin: Welcome admin (admin)", tc_pwd),
 ("P17","User logs out","logout","Logged out", lambda: admin().logout()),
]

if __name__ == "__main__":
    print(CASE_STUDY)
    run_suite("EXPERIMENT 2 - ARMS PORTAL : POSITIVE TEST CASES", cases)
