package Blockchain;

import Transaction.Transaction;

public interface BlockchainInterface {

    public void addBlock(Transaction[] txn);

}
