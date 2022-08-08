package Blockchain;

import Transaction.Transaction;
import Transaction.TXOutput;
import Transaction.TXInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockChainMain {
    public static void main(String[] args) {
        Blockchain blockchain = new Blockchain();
//        blockchain.addBlock("Bob Payed to alice 1 coin");
//        blockchain.addBlock("Boy payed to janice 1 coin");

        System.out.printf("Balance: %d",getBalance("rishav",blockchain));
        send("rishav","bob",50,blockchain);

        System.out.printf("Rishav Balance: %d",getBalance("rishav",blockchain));
        System.out.printf("bob Balance: %d",getBalance("bob",blockchain));

        for (int i = 0;i<blockchain.blocks.size();i++){
            System.out.println("Data: " + new String(blockchain.blocks.get(i).transactions[0].ID));
            System.out.println("value: " + blockchain.blocks.get(i).transactions[0].vout.get(0).value);
            System.out.println( "Hash: " + Arrays.toString(blockchain.blocks.get(i).hash));
            System.out.println( "pow: " + new ProofOfWork(blockchain.blocks.get(i)).validate());
        }
    }

    public static int getBalance(String address,Blockchain b){
        List<TXOutput> utxo = b.findUTXO(address);
        int balance=0;
        for(TXOutput out:utxo){
            balance+=out.value;
        }
        return balance;
    }

    public static void send(String from,String to, int amount,Blockchain b){
        try {
            Transaction txn = newUTXOTransaction(from, to, amount, b);
            Transaction[] transactions = {txn};
            b.mineBlock(transactions);
        } catch (Exception e){
            System.out.println("Exception caught" +  e.toString());
        }
    }

    public static Transaction newUTXOTransaction(String from, String to,int amount,Blockchain b) throws Exception {
        List<TXOutput> outputs = new ArrayList<TXOutput>();
        int balance = getBalance(from,b);

        if(balance<amount) throw new Exception("Not sufficent funds");

        Pair pair = getRequriedInputs(from,amount,b);
        List<TXInput> inputs = pair.getInputList();

        int totalInputBalance = pair.getAmount();
        TXOutput outputToReciver = new TXOutput(amount,to);
        outputs.add(outputToReciver);
        if(totalInputBalance>amount){
            TXOutput outputToSender = new TXOutput(totalInputBalance-amount,from);
            outputs.add(outputToSender);
        }

        return new Transaction(inputs,outputs);


        //get utxo of from
        //if balance<amount return
        //append all utxo reference input in inputs

        //create output [to,amount] and [from,balance-amount](skip if 0)

    }

    private static Pair getRequriedInputs(String from,int amount,Blockchain b){

        List<TXInput> inputs = new ArrayList<TXInput>();
        List<Transaction> transactions = b.findUnspentTransaction(from);

        int balance = 0;
        for(Transaction txn:transactions){
            byte[] txId = txn.ID;
            for(int i=0;i<txn.vout.size();i++){
                TXOutput out=txn.vout.get(i);
                if(out.canBeUnlockedWithInput(from)){
                   TXInput in = new TXInput(txId,i,from);
                    inputs.add(in);
                }
                balance+=out.value;
                if(balance>=amount) return new Pair(balance,inputs);
            }
        }
        return new Pair(balance,inputs);
    }

    private static class Pair{
        int amount;
        List<TXInput> inputs;
        Pair(int amount,List<TXInput> inputs){
            this.amount=amount;
            this.inputs = inputs;
        }

        int getAmount(){
            return this.amount;
        }
        List<TXInput> getInputList(){
            return this.inputs;
        }
    }


//    public static void main(String[] args) {
//        BlockChainPersist blockchain = new BlockChainPersist();
//        blockchain.addBlock("Bob payed to me");
//
//    }
}

