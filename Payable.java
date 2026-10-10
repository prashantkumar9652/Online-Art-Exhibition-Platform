package model;

// Interface: jo cheez payment se judi hai (jaise Order) use implement karti hai
public interface Payable {
    double getPayableAmount();
    String getPaymentMethod();
}
