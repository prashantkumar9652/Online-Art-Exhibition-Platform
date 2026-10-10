package model;

public class Order implements Payable {
    private int id;
    private int userId;
    private int artworkId;
    private double amount;
    private String paymentMethod;
    private String date;
    private String artworkTitle;
    private String buyerName;

    public Order(int id, int userId, int artworkId, double amount, String paymentMethod,
                 String date, String artworkTitle, String buyerName) {
        this.id = id;
        this.userId = userId;
        this.artworkId = artworkId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.date = date;
        this.artworkTitle = artworkTitle;
        this.buyerName = buyerName;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getArtworkId() { return artworkId; }
    public String getDate() { return date; }
    public String getArtworkTitle() { return artworkTitle; }
    public String getBuyerName() { return buyerName; }

    @Override
    public double getPayableAmount() { return amount; }

    @Override
    public String getPaymentMethod() { return paymentMethod; }
}
