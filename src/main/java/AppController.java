import java.lang.Math.*;
//import java.sql.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.sql.*;
import java.util.*;

public class AppController  {
    public Button showData;
    @FXML TableView<Candidate> tableView;
    @FXML TableColumn<Candidate, String> tableColumnName;
    @FXML TableColumn<Candidate, String> tableColumnSurname;
    @FXML TableColumn<Candidate, Float> tableColumnVotingOne;
    @FXML TableColumn<Candidate, Float> tableColumnVotingTwo;
    @FXML TableColumn<Candidate, Boolean> tableColumnIsWinner;
    @FXML Button createData;
    @FXML Button showDataButton;
    @FXML Button firstVoting;
    @FXML Button secondVoting;
    @FXML Button acceptSecondVoting;
    @FXML ObservableList<Candidate> candidates;
    @FXML Connection c;
    @FXML List<Object[]> winners;
    @FXML String[] winner;
    @FXML boolean isFirstAccepted = false;

    @FXML
    public void initialize() {
        showDataButton.setDisable(true);
        firstVoting.setDisable(true);
        secondVoting.setDisable(true);
        acceptSecondVoting.setDisable(true);
    }

    @FXML void createData() throws SQLException{
        prepareData();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM KANDYDAT_WYBORY");
        ObservableList<Candidate> items = FXCollections.observableArrayList();
        while (rs.next()) {
            Candidate candidate = new Candidate();
            candidate.setIMIE(rs.getString("IMIE"));
            candidate.setNAZWISKO(rs.getString("NAZWISKO"));
            candidate.setWYNIK_1_TURA(rs.getFloat("WYNIK_1_TURA"));
            candidate.setWYNIK_2_TURA(rs.getFloat("WYNIK_2_TURA"));
            candidate.setCZY_WYGRAL(rs.getBoolean("CZY_WYGRAL"));
            items.add(candidate);
        }
        candidates = items;
        showDataButton.setDisable(false);
    }

    @FXML public void showData() throws SQLException {
        loadData();
        firstVoting.setDisable(false);
    }

    @FXML public void acceptData() throws SQLException {
        c.commit();
        String[] winner = setWinner();
        PreparedStatement prep = c.prepareStatement("UPDATE KANDYDAT_WYBORY SET CZY_WYGRAL = ? WHERE (IMIE = ? AND NAZWISKO = ?)");
        prep.setBoolean(1, true);
        prep.setString(2, winner[0]);
        prep.setString(3, winner[1]);
        prep.executeUpdate();
        updateColumns();
        c.commit();
        firstVoting.setDisable(true);
        secondVoting.setDisable(true);
    }

    @FXML public void firstVoting() throws SQLException {
        secondVoting.setDisable(false);
        c.rollback();
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
        updateCandidates(s);
        winners = getWinners();

    }

    @FXML public void secondVoting() throws SQLException {
        firstVoting.setDisable(true);
        if (!isFirstAccepted) {
            c.commit();
        }
        c.rollback();
        isFirstAccepted = true;
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
            prep.setString(2, (String) winners.get(i)[0]);
            prep.setString(3, (String) winners.get(i)[1]);
            prep.executeUpdate();
        }
        updateColumns();
        winner = setWinner();
        acceptSecondVoting.setDisable(false);
    }

    void createAndPopulateTable() throws SQLException {
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
        firstVoting.setDisable(true);
        secondVoting.setDisable(true);
        acceptSecondVoting.setDisable(true);
    }

    public void prepareData() throws SQLException {
        try{
            c = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "");
            createAndPopulateTable();
            c.setAutoCommit(false);
        } catch (SQLException exc) {
            throw new SQLException(exc);
        }
    }

    List<Object[]> getWinners() throws SQLException {
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

    public String[] setWinner() throws SQLException {
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM KANDYDAT_WYBORY ORDER BY WYNIK_2_TURA DESC NULLS LAST");
        rs.next();
        Object[] winner = {rs.getString("IMIE"), rs.getString("NAZWISKO")};
        PreparedStatement prepWinner = c.prepareStatement("UPDATE KANDYDAT_WYBORY SET CZY_WYGRAL = ? WHERE (IMIE = ? AND NAZWISKO = ?)");
        prepWinner.setBoolean(1, true);
        prepWinner.setString(2, (String) winner[0]);
        prepWinner.setString(3, (String) winner[1]);
        prepWinner.executeUpdate();
        return new String[]{((String) winner[0]), ((String) winner[1])};
    }

    void updateColumns() throws SQLException {
        Statement s = c.createStatement();
        updateCandidates(s);
    }

    void loadData() throws SQLException {
        tableColumnName.setCellValueFactory(new PropertyValueFactory<>("IMIE"));
        tableColumnSurname.setCellValueFactory(new PropertyValueFactory<>("NAZWISKO"));
        tableColumnVotingOne.setCellValueFactory(new PropertyValueFactory<>("WYNIK_1_TURA"));
        tableColumnVotingTwo.setCellValueFactory(new PropertyValueFactory<>("WYNIK_2_TURA"));
        tableColumnIsWinner.setCellValueFactory(new PropertyValueFactory<>("CZY_WYGRAL"));
        tableView.setItems(candidates);
    }

    void updateCandidates(Statement s) throws SQLException {
        ResultSet rs = s.executeQuery("SELECT * FROM KANDYDAT_WYBORY");
        ObservableList<Candidate> items = FXCollections.observableArrayList();
        while (rs.next()) {
            Candidate candidate = new Candidate();
            candidate.setIMIE(rs.getString("IMIE"));
            candidate.setNAZWISKO(rs.getString("NAZWISKO"));
            candidate.setWYNIK_1_TURA(rs.getFloat("WYNIK_1_TURA"));
            candidate.setWYNIK_2_TURA(rs.getFloat("WYNIK_2_TURA"));
            candidate.setCZY_WYGRAL(rs.getBoolean("CZY_WYGRAL"));
            items.add(candidate);
        }
        candidates = items;
        loadData();
    }
}
