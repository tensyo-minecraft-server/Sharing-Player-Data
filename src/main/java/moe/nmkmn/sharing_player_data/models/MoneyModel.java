package moe.nmkmn.sharing_player_data.models;

public class MoneyModel {
    private String UUID;
    private double balance;

    public MoneyModel(String UUID, double balance) {
        this.UUID = UUID;
        this.balance = balance;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
