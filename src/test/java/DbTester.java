import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DbTester {
    @Test
    public void testConnection() {
        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "")) {
        } catch (SQLException e) {
            Assertions.fail(e);
        }
    }

    private void createAndPopulateTable(Connection c) throws SQLException {
        Statement s = c.createStatement();
        s.execute("DROP TABLE IF EXISTS KANDYDAT_WYBORY");
        s.execute("CREATE TABLE KANDYDAT_WYBORY (ID INT IDENTITY PRIMARY KEY, IMIE VARCHAR(255), NAZWISKO VARCHAR(255), WYNIK_1_TURA DECIMAL, WYNIK_2_TURA DECIMAL, CZY_WYGRAL BOOLEAN)");
        String[][] dane = {{"Donald", "Kaczor"}, {"Jackie", "Chan"}, {"Merida", "Waleczna"}, {"Bugs", "Krolik"}, {"Sknerus", "McKwacz"}};
        PreparedStatement prep = c.prepareStatement("INSERT INTO KANDYDAT_WYBORY (IMIE, NAZWISKO) VALUES (?, ?)");
        for (int i = 0; i < 5; i++) {
            prep.setString(1, dane[i][0]);
            prep.setString(2, dane[i][1]);
            prep.executeUpdate();
        }
    }

    private void makeFirstVoting(Connection c) throws SQLException {
        Statement s = c.createStatement();
        Random r = new Random();
        ArrayList<Float> votesPercentages = new ArrayList<>();;
        float random;
        float min = 0;
        float max = 100;
        for (int i = 0; i < 4; i++) {
            random = r.nextFloat() * (max - min);
            votesPercentages.add(random);
            max -= random;
        }
        votesPercentages.add(max);
        Collections.shuffle(votesPercentages);
        PreparedStatement prep = c.prepareStatement("UPDATE KANDYDAT_WYBORY SET WYNIK_1_TURA = ? WHERE ID = ?");
        for (int i = 0; i < 5; i++) {
            prep.setFloat(1, votesPercentages.get(i));
            prep.setInt(2, i);
            prep.executeUpdate();
        }
    }

    private void makeSecondVoting(Connection c, List<Object[]> winners) throws SQLException {
        Statement s = c.createStatement();
        Random r = new Random();
        ArrayList<Float> votesPercentages = new ArrayList<>();;
        float random;
        random = r.nextFloat() * 100;
        votesPercentages.add(random);
        votesPercentages.add(100 - random);
        Collections.shuffle(votesPercentages);
        PreparedStatement prep = c.prepareStatement("UPDATE KANDYDAT_WYBORY SET WYNIK_2_TURA = ? WHERE (IMIE = ? AND NAZWISKO = ?)");
        for (int i = 0; i < 2; i++) {
            prep.setFloat(1, votesPercentages.get(i));
//            System.out.println(winners.get(i)[0]);
            prep.setString(2, (String) winners.get(i)[0]);
            prep.setString(3, (String) winners.get(i)[1]);
            prep.executeUpdate();
        }
    }

    private List<Object[]> getWinners(Connection c) throws SQLException {
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM KANDYDAT_WYBORY ORDER BY WYNIK_1_TURA DESC");
        List<Object[]> results = new ArrayList<>();
        while (rs.next()) {
            Object[] row = new Object[2];
            row[0] = rs.getString("IMIE");
            row[1] = rs.getString("NAZWISKO");
            results.add(row);
        }
        return results;
    }

    public void showResults(Connection c) throws SQLException{
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM KANDYDAT_WYBORY");
        while (rs.next()) {
            System.out.println(rs.getString("IMIE") + "|" +
                    rs.getString("NAZWISKO") + "|" + rs.getFloat("WYNIK_1_TURA") + "|" +
                    rs.getFloat("WYNIK_2_TURA") + "|" + rs.getBoolean("CZY_WYGRAL"));
        }
        System.out.println("-----------------------------------------------------------------------");
    }

    public String[] setWinner(Connection c) throws SQLException {
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM KANDYDAT_WYBORY ORDER BY WYNIK_2_TURA DESC NULLS LAST");
        rs.next();
        Object[] winner = {rs.getString("IMIE"), rs.getString("NAZWISKO")};
        PreparedStatement prepWinner = c.prepareStatement("UPDATE KANDYDAT_WYBORY SET CZY_WYGRAL = ? WHERE (IMIE = ? AND NAZWISKO = ?)");
        prepWinner.setBoolean(1, true);
//            System.out.println(winners.get(i)[0]);
        prepWinner.setString(2, (String) winner[0]);
        prepWinner.setString(3, (String) winner[1]);
        prepWinner.executeUpdate();
        System.out.println("WYGRAL(A) " + ((String) winner[0]) + " " + ((String) winner[1]));
        return new String[]{((String) winner[0]), ((String) winner[1])};
    }

    @Test
    public void testVoting() {
        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "")) {
            createAndPopulateTable(c);
            System.out.println("BEFORE VOTING");
            showResults(c);
            makeFirstVoting(c);
            System.out.println("FIRST VOTING");
            showResults(c);
            List<Object[]> winners = getWinners(c);
            c.setAutoCommit(false);
            makeSecondVoting(c, winners);
            System.out.println("SECOND VOTING BEFORE ROLLBACK");
            showResults(c);
            c.rollback();
            System.out.println("SECOND VOTING AFTER ROLLBACK");
            showResults(c);
            makeSecondVoting(c,winners);
            c.commit();
            System.out.println("SECOND VOTING AFTER COMMIT");
            showResults(c);
            String[] winner = setWinner(c);
            System.out.println("RESULT WITH WINNER");
            showResults(c);
            PreparedStatement prep = c.prepareStatement("SELECT * FROM KANDYDAT_WYBORY WHERE (IMIE = ? AND NAZWISKO = ?)");
            boolean isTrueWinner;
            prep.setString(1, winner[0]);
            prep.setString(2, winner[1]);
            ResultSet res = prep.executeQuery();
            res.next();
            isTrueWinner = res.getBoolean("CZY_WYGRAL");
            Assertions.assertEquals(true, isTrueWinner);

        } catch (SQLException e) {
            Assertions.fail(e);
        }
    }
}
