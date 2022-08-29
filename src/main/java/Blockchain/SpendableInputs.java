package Blockchain;

import Transaction.TXInput;

import java.util.List;

public class SpendableInputs {

    int amount;
    List<TXInput> inputs;

    public SpendableInputs(int amount, List<TXInput> inputs) {
        this.amount = amount;
        this.inputs = inputs;
    }

    public int getAmount() {
        return this.amount;
    }

    public List<TXInput> getInputList() {
        return this.inputs;
    }
}
