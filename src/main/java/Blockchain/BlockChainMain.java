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
        Wallet genesisWallet = new Wallet();
        System.out.println("genesisWallet Address" + genesisWallet.getAddress());

        Wallet receiverWallet = new Wallet();
        System.out.println("receiverWallet Address" + receiverWallet.getAddress());
        List<Wallet> walletList = Wallet.getLoadedWallets();

        System.out.println(walletList.size());

        Blockchain blockchain = new Blockchain(genesisWallet.getAddress());
        int initialGenesisBalance = getBalance(genesisWallet.getAddress(),blockchain);
//        blockchain.addBlock("Bob Payed to alice 1 coin")
//        blockchain.addBlock("Boy payed to janice 1 coin");

        System.out.printf("Balance genesisWallet: %d\n",getBalance(genesisWallet.getAddress(),blockchain));
        send(genesisWallet,receiverWallet.getAddress(),40,blockchain);


        System.out.printf("Balance genesisWallet: %d\n",getBalance(genesisWallet.getAddress(),blockchain));
        System.out.printf("Balance receiverAddress: %d\n",getBalance(receiverWallet.getAddress(),blockchain));

//        for (int i = 0;i<blockchain.blocks.size();i++){
//            System.out.println("Data: " + new String(blockchain.blocks.get(i).transactions[0].ID));
//            System.out.println("value: " + blockchain.blocks.get(i).transactions[0].vout.get(0).value);
//            System.out.println( "Hash: " + Arrays.toString(blockchain.blocks.get(i).hash));
//            System.out.println( "pow: " + new ProofOfWork(blockchain.blocks.get(i)).validate());
//        }
    }

    public static int getBalance(String address,Blockchain b){
//        List<TXOutput> utxo = b.findUTXO(address);
        List<TXOutput> utxo = b.utxoSet.findUTXO(address);
        int balance=0;
        for(TXOutput out:utxo){
            balance+=out.value;
        }
        return balance;
    }

    public static void send(Wallet senderWallet,String receiverAddress, int amount,Blockchain b){
        System.out.printf("Sending txn of amount %d From address: %s To address: %s\n",amount,senderWallet.getAddress(),receiverAddress);
        try {
            Transaction txn = newUTXOTransaction(senderWallet, receiverAddress, amount, b);
            Transaction[] transactions = {txn};
            b.mineBlock(transactions);
        } catch (Exception e){
            System.out.println("Exception caught" +  e.toString());
        }
    }

    public static Transaction newUTXOTransaction(Wallet senderWallet, String receiverAddress,int amount,Blockchain b) throws Exception {
        String senderAddress = senderWallet.getAddress();
        List<TXOutput> outputs = new ArrayList<TXOutput>();
        int balance = getBalance(senderAddress,b);

        if(balance<amount) throw new Exception("Not sufficent funds");


        SpendableInputs spendableInputs = b.utxoSet.findSpendableInputs(senderAddress);
        List<TXInput> inputs = spendableInputs.getInputList();
        System.out.println(inputs.get(0));

        int totalInputBalance = spendableInputs.getAmount();
        TXOutput outputToReceiver = new TXOutput(amount,receiverAddress);
        outputs.add(outputToReceiver);
        if(totalInputBalance>amount){
            TXOutput outputToSender = new TXOutput(totalInputBalance-amount,senderAddress);
            outputs.add(outputToSender);
        }

        Transaction newTxn = new Transaction(inputs,outputs);
        b.signTransaction(newTxn,senderWallet.privateKey);
        return  newTxn;


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
                    System.out.println("sendTxId" + Arrays.toString(txId));
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

