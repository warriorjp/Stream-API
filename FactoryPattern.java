Instead of creating objects using new in your code, the Factory Pattern provides a central method to return objects based on some input.
    
public class PaymentFactory {

    public static Payment getPayment(String type) {
        if (type == null) {
            return null;
        }

        switch (type.toUpperCase()) {
            case "CREDITCARD":
                return new CreditCard();
            case "DEBITCARD":
                return new DebitCard();
            case "UPI":
                return new UPI();
            case "EMI":
                return new EMI();
            default:
                return null;
        }
    }
}


public interface Payment {
    void pay();
}

public class CreditCard implements Payment {
    @Override
    public void pay() {
        System.out.println("Paid using Credit Card.");
    }
}

public class DebitCard implements Payment {
    @Override
    public void pay() {
        System.out.println("Paid using Debit Card.");
    }
}

public class UPI implements Payment {
    @Override
    public void pay() {
        System.out.println("Paid using UPI.");
    }
}

public class EMI implements Payment {
    @Override
    public void pay() {
        System.out.println("Paid using EMI.");
    }
}

public class Main {
    public static void main(String[] args) {
        Payment payment1 = PaymentFactory.getPayment("creditcard");
        payment1.pay();  // Output: Paid using Credit Card.

        Payment payment2 = PaymentFactory.getPayment("upi");
        payment2.pay();  // Output: Paid using UPI.

        Payment payment3 = PaymentFactory.getPayment("emi");
        payment3.pay();  // Output: Paid using EMI.
    }
}

