package Blockchain;

import Transaction.Transaction;
import Transaction.TXOutput;
import Transaction.TXInput;
import Wallet.Wallet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockChainMain {
    //TODO: cli for creating more scenarios
    public static void main(String[] args) {
        Wallet genesisAddress = new Wallet();
        System.out.println("genesisAddress Address" + genesisAddress.getAddress());

        Wallet walletReceiver = new Wallet();
        System.out.println("walletReceiver Address" + walletReceiver.getAddress());

        Blockchain blockchain = new Blockchain(genesisAddress.getAddress());
        int initialGenesisBalance = getBalance(genesisAddress.getAddress(),blockchain);
//        blockchain.addBlock("Bob Payed to alice 1 coin");
//        blockchain.addBlock("Boy payed to janice 1 coin");

        System.out.printf("Balance genesisAddress: %d\n",getBalance(genesisAddress.getAddress(),blockchain));
        send(genesisAddress.getAddress(),walletReceiver.getAddress(),40,blockchain);


        System.out.printf("Balance genesisAddress: %d\n",getBalance(genesisAddress.getAddress(),blockchain));
        System.out.printf("Balance receiverAddress: %d\n",getBalance(walletReceiver.getAddress(),blockchain));

//        for (int i = 0;i<blockchain.blocks.size();i++){
//            System.out.println("Data: " + new String(blockchain.blocks.get(i).transactions[0].ID));
//            System.out.println("value: " + blockchain.blocks.get(i).transactions[0].vout.get(0).value);
//            System.out.println( "Hash: " + Arrays.toString(blockchain.blocks.get(i).hash));
//            System.out.println( "pow: " + new ProofOfWork(blockchain.blocks.get(i)).validate());
//        }
    }

    public static int getBalance(String address,Blockchain b){
        List<TXOutput> utxo = b.findUTXO(address);
        int balance=0;
        for(TXOutput out:utxo){
            balance+=out.value;
        }
        return balance;
    }

    public static void send(String senderAddress,String receiverAddress, int amount,Blockchain b){
        System.out.printf("Sending txn of amount %d From address: %s To address: %s\n",amount,senderAddress,receiverAddress);
        try {
            Transaction txn = newUTXOTransaction(senderAddress, receiverAddress, amount, b);
            Transaction[] transactions = {txn};
            b.mineBlock(transactions);
        } catch (Exception e){
            System.out.println("Exception caught" +  e.toString());
        }
    }

    public static Transaction newUTXOTransaction(String senderAddress, String receiverAddress,int amount,Blockchain b) throws Exception {
        List<TXOutput> outputs = new ArrayList<TXOutput>();
        int balance = getBalance(senderAddress,b);

        if(balance<amount) throw new Exception("Not sufficent funds");

        Pair pair = getRequriedInputs(senderAddress,amount,b);
        List<TXInput> inputs = pair.getInputList();

        int totalInputBalance = pair.getAmount();
        TXOutput outputToReceiver = new TXOutput(amount,receiverAddress);
        outputs.add(outputToReceiver);
        if(totalInputBalance>amount){
            TXOutput outputToSender = new TXOutput(totalInputBalance-amount,senderAddress);
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
                if(out.isLockedWithKey(from)){
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

