package Blockchain;

import java.util.*;

public class Blockchain implements BlockchainInterface {
    public List<Block> blocks;

     Blockchain(){
        Block genesisBlock = newGenesisBlock();
        this.blocks = new ArrayList<Block>();
        this.blocks.add(genesisBlock);
    }

    public void addBlock(Transaction[] transactions){
        if(this.blocks == null ){
            this.blocks = new ArrayList<Block>();
        }
        Block prevBlock = this.blocks.get(blocks.size() - 1);
        Block newBlock = new Block(transactions, prevBlock.prevBlockHash);
        this.blocks.add(newBlock);

    }


    private Block newGenesisBlock() {
         List<Transaction> txn = Arrays.asList(Transaction.newCoinbaseTxn("rishav","paid 100 to rishav"));
        return new Block(txn.toArray(new Transaction[txn.size()]),new byte[0]);
    }

    public List<Transaction> findUnspentTransaction(String address){
        List<Transaction> unspentTxn = new ArrayList<Transaction>();

        Map<String,List<Integer>> spentTxn = getSpentTxn(address);

        for(Block block:blocks){
            for(Transaction transaction:block.transactions){
                for(int index=0;index<transaction.vout.size();index++){
                    if( spentTxn.get(Arrays.toString(transaction.ID))!=null ){
                        ArrayList<Integer> arr = (ArrayList<Integer>) spentTxn.get(Arrays.toString(transaction.ID));
                        for(Integer spentIndex:arr){
                            if(spentIndex!=index && transaction.vout.get(index).canBeUnlockedWithInput(address)) unspentTxn.add(transaction);
                        }
                    } else{
                        if(transaction.vout.get(index).canBeUnlockedWithInput(address)) unspentTxn.add(transaction);
                    }
                }
            }
        }
       return unspentTxn;
    }

    public List<TXOutput> findUTXO(String address){
        List<TXOutput> utxo = new ArrayList<TXOutput>();
        List<Transaction> transactions = findUnspentTransaction(address);
         for(Transaction transaction:transactions){
             for(TXOutput out:transaction.vout){
                 if(out.canBeUnlockedWithInput(address)){
                    utxo.add(out);
                 }
             }
         }
         return  utxo;
    }

    public void mineBlock(Transaction[] transactions){
         addBlock(transactions);
    }


    private Map<String,List<Integer>> getSpentTxn(String address){

        Map<String,List<Integer>> spentTxn = new HashMap<String,List<Integer>>();
        for (Block block:blocks){
            for(Transaction transaction:block.transactions){
                //TODO: check if coinbase txn
                for(TXInput in:transaction.vin){
                    if( in.txId!=null && in.canUnlockOutput(address)) {
                        if(spentTxn.get(Arrays.toString(in.txId))==null){
                            List<Integer> a =  new ArrayList<Integer>();
                            a.add(in.voutIndex);
                            spentTxn.put(Arrays.toString(in.txId),a);
                        } else{
                            spentTxn.put(Arrays.toString(in.txId), new ArrayList<Integer>(in.voutIndex));
                        }
                    }
                }
            }
        }
        return spentTxn;
    }

}
