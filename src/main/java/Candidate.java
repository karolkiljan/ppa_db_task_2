import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;

public class Candidate {
    private SimpleStringProperty IMIE;
    private SimpleStringProperty NAZWISKO;
    private SimpleFloatProperty WYNIK_1_TURA;
    private SimpleFloatProperty WYNIK_2_TURA;
    private SimpleBooleanProperty CZY_WYGRAL;
    Candidate() {
        this.IMIE = new SimpleStringProperty();
        this.NAZWISKO = new SimpleStringProperty();
        this.WYNIK_1_TURA = new SimpleFloatProperty();
        this.WYNIK_2_TURA = new SimpleFloatProperty();
        this.CZY_WYGRAL = new SimpleBooleanProperty();
    }

    public String getIMIE() {
        return IMIE.get();
    }

    public SimpleStringProperty nameProperty() {
        return IMIE;
    }

    public void setIMIE(String name) {
        this.IMIE.set(name);
    }

    public String getNAZWISKO() {
        return NAZWISKO.get();
    }

    public SimpleStringProperty surnameProperty() {
        return NAZWISKO;
    }

    public void setNAZWISKO(String surname) {
        this.NAZWISKO.set(surname);
    }

    public float getWYNIK_1_TURA() {
        return WYNIK_1_TURA.get();
    }

    public SimpleFloatProperty voteOneProperty() {
        return WYNIK_1_TURA;
    }

    public void setWYNIK_1_TURA(float voteOne) {
        this.WYNIK_1_TURA.set(voteOne);
    }

    public float getWYNIK_2_TURA() {
        return WYNIK_2_TURA.get();
    }

    public SimpleFloatProperty voteTwoProperty() {
        return WYNIK_2_TURA;
    }

    public void setWYNIK_2_TURA(float voteTwo) {
        this.WYNIK_2_TURA.set(voteTwo);
    }

    public boolean getCZY_WYGRAL() {
        return CZY_WYGRAL.get();
    }

    public SimpleBooleanProperty isWinnerProperty() {
        return CZY_WYGRAL;
    }

    public void setCZY_WYGRAL(boolean isWinner) {
        this.CZY_WYGRAL.set(isWinner);
    }
}
