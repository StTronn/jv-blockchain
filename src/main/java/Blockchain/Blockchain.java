package Blockchain;

import Transaction.Transaction;
import Transaction.TXInput;
import Transaction.TXOutput;

import java.security.PrivateKey;
import java.util.*;

public class Blockchain implements BlockchainInterface {
    public List<Block> blocks;

    Blockchain(String genesisAddress) {
        Block genesisBlock = newGenesisBlock(genesisAddress);
        this.blocks = new ArrayList<Block>();
        this.blocks.add(genesisBlock);
    }

    public void addBlock(Transaction[] transactions) {
        if (this.blocks == null) {
            this.blocks = new ArrayList<Block>();
        }
        Block prevBlock = this.blocks.get(blocks.size() - 1);
        Block newBlock = new Block(transactions, prevBlock.prevBlockHash);
        this.blocks.add(newBlock);

    }


    private Block newGenesisBlock(String genesisAddress) {
        List<Transaction> txn = Arrays.asList(Transaction.newCoinbaseTxn(genesisAddress, "paid 100 to rishav"));
        return new Block(txn.toArray(new Transaction[txn.size()]), new byte[0]);
    }

    public Transaction findTransaction(byte[] ID){
        for(Block block:blocks){
            for(Transaction transaction: block.transactions){
                if(Arrays.equals(transaction.ID, ID)){
                    return  transaction;
                }
            }
        }
        return null;
    }

    public void signTransaction(Transaction tx,PrivateKey privateKey){
        Map<String,Transaction> prevTxns = new HashMap<String,Transaction>();
        for (TXInput input:tx.vin){
           Transaction prevTxn = this.findTransaction(input.txId) ;
           prevTxns.put(Arrays.toString(input.txId),prevTxn);
        }
        tx.sign(privateKey,prevTxns);

    }

    public boolean verifyTransaction(Transaction tx){
        Map<String,Transaction> prevTxns = new HashMap<String,Transaction>();
        for (TXInput input:tx.vin){
            Transaction prevTxn = this.findTransaction(input.txId) ;
            prevTxns.put(Arrays.toString(input.txId),prevTxn);
        }
        return tx.verify(prevTxns);
    }

    public List<Transaction> findUnspentTransaction(String address) {
        List<Transaction> unspentTxn = new ArrayList<Transaction>();

        Map<String, List<Integer>> spentTxn = getSpentTxn(address);

        for (Block block : blocks) {
            for (Transaction transaction : block.transactions) {
                for (int index = 0; index < transaction.vout.size(); index++) {
                    if (spentTxn.get(Arrays.toString(transaction.ID)) != null) {
                        ArrayList<Integer> arr = (ArrayList<Integer>) spentTxn.get(Arrays.toString(transaction.ID));
                        for (Integer spentIndex : arr) {
                            if (spentIndex != index && transaction.vout.get(index).isLockedWithKey(address))
                                unspentTxn.add(transaction);
                        }
                    } else {
                        if (transaction.vout.get(index).isLockedWithKey(address)) unspentTxn.add(transaction);
                    }
                }
            }
        }
        return unspentTxn;
    }

    public Map<byte[],List<TXOutput>> findUTXO(){
        Map<byte[],List<TXOutput>> UTXOsMap = new HashMap<byte[],List<TXOutput>>();

        //txId -> [] //list of indexes spent
        Map<String, List<Integer>> spentTxn = getSpentTxn();

        for (Block block : blocks) {
            for (Transaction transaction : block.transactions) {
                for (int index = 0; index < transaction.vout.size(); index++) {
                    TXOutput out = transaction.vout.get(index);
                    if (spentTxn.get(Arrays.toString(transaction.ID)) != null) {
                        ArrayList<Integer> arr = (ArrayList<Integer>) spentTxn.get(Arrays.toString(transaction.ID));
                        for(Integer spentIndex:arr){
                            if(spentIndex!=index){
                                if(!UTXOsMap.containsKey(transaction.ID)){
                                   UTXOsMap.put(transaction.ID,new ArrayList<TXOutput>());
                                }
                                UTXOsMap.get(transaction.ID).add(out);
                            }
                        }
                    }

                }
            }
        }

        return UTXOsMap;
    }

    public List<TXOutput> findUTXO(String address) {
        List<TXOutput> utxo = new ArrayList<TXOutput>();
        List<Transaction> transactions = findUnspentTransaction(address);
        for (Transaction transaction : transactions) {
            for (TXOutput out : transaction.vout) {
                if (out.isLockedWithKey(address)) {
                    utxo.add(out);
                }
            }
        }
        return utxo;
    }

    public void mineBlock(Transaction[] transactions) {
        for (Transaction transaction:transactions){
            if(!this.verifyTransaction(transaction)){
               System.out.println("Error!: Invalid Transactions, cannot verify txn");
               System.exit(1);
            }
        }
        addBlock(transactions);
    }


    private Map<String, List<Integer>> getSpentTxn(String address) {

        Map<String, List<Integer>> spentTxn = new HashMap<String, List<Integer>>();
        for (Block block : blocks) {
            for (Transaction transaction : block.transactions) {
                //TODO: check if coinbase txn
                for (TXInput in : transaction.vin) {
                    if (in.txId != null && in.canUnlockOutput(address)) {
                        if (spentTxn.get(Arrays.toString(in.txId)) == null) {
                            List<Integer> a = new ArrayList<Integer>();
                            a.add(in.voutIndex);
                            spentTxn.put(Arrays.toString(in.txId), a);
                        } else {
                            spentTxn.put(Arrays.toString(in.txId), new ArrayList<Integer>(in.voutIndex));
                        }
                    }
                }
            }
        }
        return spentTxn;
    }

    private Map<String, List<Integer>> getSpentTxn(){
        Map<String, List<Integer>> spentTxn = new HashMap<String, List<Integer>>();
        for (Block block : blocks) {
            for (Transaction transaction : block.transactions) {
                for (TXInput in : transaction.vin) {
                    if (in.txId != null) {
                        if (spentTxn.get(Arrays.toString(in.txId)) == null) {
                            List<Integer> a = new ArrayList<Integer>();
                            a.add(in.voutIndex);
                            spentTxn.put(Arrays.toString(in.txId), a);
                        } else {
                            spentTxn.put(Arrays.toString(in.txId), new ArrayList<Integer>(in.voutIndex));
                        }
                    }
                }
            }
        }
        return spentTxn;
    }

}
