package banking;

public class Loan extends Transaction {
    private boolean isApproved;
    private boolean isPaidOff;
    private double amountPaid;
    private final String linkedAccountNumber;

    public Loan(String accountNumber, double amount, String description) {
        super(amount, description);
        this.linkedAccountNumber = accountNumber;
        this.isApproved = false;
        this.isPaidOff = false;
        this.amountPaid = 0.0;
    }

    public Loan(String accountNumber, double amount, String description, String transactionID) {
        super(amount, description, transactionID);
        this.linkedAccountNumber = accountNumber;
        this.isApproved = false;
        this.isPaidOff = false;
        this.amountPaid = 0.0;
    }

    public Transaction makePayment(double payment) {
        amountPaid += payment;
        if(isPaidOff()) System.out.println("Congrats on paying back your loan, enjoy the debt free life!");
        return new Transaction(payment,"Loan payment");
    }

    public boolean isApproved() {
        return isApproved;
    }

    public boolean isPaidOff() {
        isPaidOff = amountPaid == super.getAmount();
        return isPaidOff;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void approve(Database datahandler) {
        this.isApproved = true;
        datahandler.getUserByAccountNumber(linkedAccountNumber).recieveLoanApproval(this);
    }

    public String getLinkedAccountNumber() {
        return this.linkedAccountNumber;
    }

    public boolean makeRepayment(double amount) {
        if (!isApproved || isPaidOff || amount <= 0) return false;
    
        amountPaid += amount;
        if (amountPaid >= getAmount()) {
            isPaidOff = true;
        }
        return true;
    }
    
}
