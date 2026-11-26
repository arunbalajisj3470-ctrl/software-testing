// Q33: JUnit test case for a voting system, with assert statements, validated using white-box testing.
// White-box coverage targets: eligibility branch, duplicate-vote branch, tally logic, closed-poll branch.

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class VotingSystem {
    private final Map<String, Integer> tally = new HashMap<>();
    private final Set<String> voters = new HashSet<>();
    private final int minAge;
    private boolean open = true;

    VotingSystem(int minAge) { this.minAge = minAge; }

    // Returns true if the vote was accepted
    boolean castVote(String voterId, int age, String candidate) {
        if (!open) return false;                          // branch: poll closed
        if (age < minAge) return false;                   // branch: not eligible
        if (voters.contains(voterId)) return false;       // branch: duplicate vote
        if (candidate == null || candidate.isBlank()) return false; // branch: invalid candidate
        voters.add(voterId);
        tally.merge(candidate, 1, Integer::sum);
        return true;
    }

    int votesFor(String candidate) { return tally.getOrDefault(candidate, 0); }
    void close() { open = false; }
}

public class Q33_VotingSystemTest {

    private VotingSystem system;

    @BeforeEach
    void setUp() {
        system = new VotingSystem(18);
    }

    @Test
    void eligibleVoterIsAccepted() {
        assertTrue(system.castVote("V1", 25, "Alice"));
        assertEquals(1, system.votesFor("Alice"));
    }

    @Test
    void underageVoterIsRejected() {
        assertFalse(system.castVote("V2", 17, "Alice"));
        assertEquals(0, system.votesFor("Alice"));
    }

    @Test
    void boundaryAgeEighteenIsAccepted() {
        assertTrue(system.castVote("V3", 18, "Bob"));
    }

    @Test
    void duplicateVoteIsRejected() {
        assertTrue(system.castVote("V4", 30, "Alice"));
        assertFalse(system.castVote("V4", 30, "Alice"));
        assertEquals(1, system.votesFor("Alice"));
    }

    @Test
    void blankCandidateIsRejected() {
        assertFalse(system.castVote("V5", 30, "   "));
        assertFalse(system.castVote("V6", 30, null));
    }

    @Test
    void votesAfterClosingAreRejected() {
        system.close();
        assertFalse(system.castVote("V7", 40, "Alice"));
        assertEquals(0, system.votesFor("Alice"));
    }

    @Test
    void tallyCountsEachCandidateSeparately() {
        system.castVote("V8", 20, "Alice");
        system.castVote("V9", 21, "Bob");
        system.castVote("V10", 22, "Alice");
        assertEquals(2, system.votesFor("Alice"));
        assertEquals(1, system.votesFor("Bob"));
        assertEquals(0, system.votesFor("Carol"));
    }

    public static void main(String[] args) {
        System.out.println("Run with JUnit 5 to execute the white-box test cases.");
    }
}
