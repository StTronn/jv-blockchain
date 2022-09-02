package Blockchain;

import Transaction.Transaction;
import Transaction.TXOutput;

import java.security.PrivateKey;
import java.util.List;
import java.util.Map;

public interface BlockchainInterface {

    public void addBlock(Transaction[] txn);

    public Transaction findTransaction(byte[] ID);

    public void signTransaction(Transaction tx, PrivateKey privateKey);

    public boolean verifyTransaction(Transaction tx);

    public List<Transaction> findUnspentTransaction(String address);

    List<TXOutput> findUTXOAddress(String address);

    public Map<String, List<TXOutput>> findUTXO();

    public void mineBlock(Transaction[] transactions);

    public byte[] getHash();

    public Block getBlock(byte[] blockHash);
}
