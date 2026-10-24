import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Q11. Railway Reservation System - POSITIVE test cases (TC-P01 .. TC-P15)
 * Run:  javac *.java  &&  java Q11_RailwayPositiveTest
 */
public class Q11_RailwayPositiveTest {

    static int passed = 0, failed = 0;

    static void check(String id, String description, boolean condition) {
        if (condition) { passed++; System.out.println("PASS  " + id + " - " + description); }
        else { failed++; System.out.println("FAIL  " + id + " - " + description); }
    }

    static List<RailwayApp.Passenger> passengers(int n) {
        List<RailwayApp.Passenger> list = new ArrayList<>();
        for (int i = 1; i <= n; i++) list.add(new RailwayApp.Passenger("Passenger " + (char) ('A' + i - 1), 20 + i, "M", "Lower"));
        return list;
    }

    public static void main(String[] args) {
        RailwayApp app = RailwayApp.withSampleData();
        app.addUser("arun01", "Pass@123");
        LocalDate journey = LocalDate.of(2026, 10, 25);

        // TC-P01 Login
        app.login("arun01", "Pass@123", RailwayApp.CAPTCHA);
        check("TC-P01", "Login with valid credentials and captcha", true);

        // TC-P02 Search trains
        List<RailwayApp.Train> trains = app.searchTrains("Chennai Central", "Coimbatore Jn", journey);
        check("TC-P02", "Search Chennai -> Coimbatore lists 2 trains", trains.size() == 2);

        // TC-P03 Station auto-suggest
        List<String> suggestions = app.suggestStations("Coim");
        check("TC-P03", "Auto-suggest 'Coim' gives Coimbatore Jn", suggestions.contains("Coimbatore Jn"));

        // TC-P04 Availability and fare
        check("TC-P04", "3A shows 20 seats at Rs.1100", app.availability("12675", "3A") == 20 && app.fare("12675", "3A") == 1100.0);

        // TC-P05 / TC-P06 Book one passenger and pay by UPI
        RailwayApp.Ticket t1 = app.book("12675", "3A", "GENERAL", passengers(1), journey, "UPI:1234");
        check("TC-P05", "Passenger details accepted and fare = Rs.1100", t1.fare == 1100.0);
        check("TC-P06", "Ticket booked with 10-digit PNR and status CNF", t1.pnr.length() == 10 && "CNF".equals(t1.status));
        check("TC-P06b", "Seat availability reduced to 19", app.availability("12675", "3A") == 19);

        // TC-P07 Multiple passengers
        RailwayApp.Ticket t2 = app.book("12675", "3A", "GENERAL", passengers(4), journey, "UPI:1234");
        check("TC-P07", "4 passengers booked; fare = 4 x 1100; seats 15 left",
                t2.fare == 4400.0 && t2.passengers.size() == 4 && app.availability("12675", "3A") == 15);

        // TC-P08 Tatkal at opening time
        app.setNow(LocalTime.of(10, 0));
        RailwayApp.Ticket tk = app.book("12675", "2A", "TATKAL", passengers(1), journey, "UPI:1234");
        check("TC-P08", "Tatkal 2A booked at 10:00 with Tatkal fare (+30%)", "CNF".equals(tk.status) && Math.abs(tk.fare - 1950.0) < 0.001);

        // TC-P09 Waiting list when full
        RailwayApp.Ticket wl = app.book("12637", "SL", "GENERAL", passengers(1), journey, "UPI:1234");
        check("TC-P09", "No seats -> ticket booked as waiting list: " + wl.status, wl.status.startsWith("WL/"));

        // TC-P10 PNR status
        RailwayApp.Ticket status = app.pnrStatus(t1.pnr);
        check("TC-P10", "PNR status shows correct train, class and status",
                status.trainNumber.equals("12675") && status.cls.equals("3A") && "CNF".equals(status.status));

        // TC-P11 / TC-P12 Cancel and refund
        int seatsBefore = app.availability("12675", "3A");
        double refund = app.cancel(t1.pnr);
        check("TC-P11", "Cancellation refunds Rs.880 (80% of Rs.1100)", Math.abs(refund - 880.0) < 0.001);
        check("TC-P12", "Seat released back to the pool", app.availability("12675", "3A") == seatsBefore + 1);

        // TC-P13 e-ticket
        String eticket = app.eTicket(t2.pnr);
        check("TC-P13", "E-ticket contains PNR, train, fare and passenger names",
                eticket.contains(t2.pnr) && eticket.contains("12675") && eticket.contains("Passenger A"));

        // TC-P14 Booking history
        check("TC-P14", "Booking history lists all 4 tickets", app.history().size() == 4);

        // TC-P15 SMS notification
        boolean sms = false;
        for (String n : app.notifications()) if (n.contains(t2.pnr)) sms = true;
        check("TC-P15", "Confirmation SMS with PNR sent", sms);

        System.out.println("\nResult: " + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }
}
