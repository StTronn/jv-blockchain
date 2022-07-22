package Blockchain;

import java.util.Arrays;
import java.util.List;

public class BlockChainMain {
    public static void main(String[] args) {
        Blockchain blockchain = new Blockchain();
//        blockchain.addBlock("Bob Payed to alice 1 coin");
//        blockchain.addBlock("Boy payed to janice 1 coin");

        List<TXOutput> utxo = blockchain.findUTXO("rishav");
        int balance=0;
        for(TXOutput out:utxo){
           balance+=out.value;
        }
        System.out.printf("Balance: %d",balance);


        for (int i = 0;i<blockchain.blocks.size();i++){
            System.out.println("Data: " + new String(blockchain.blocks.get(i).transactions[0].ID));
            System.out.println("value: " + blockchain.blocks.get(i).transactions[0].vout.get(0).value);
            System.out.println( "Hash: " + Arrays.toString(blockchain.blocks.get(i).hash));
            System.out.println( "pow: " + new ProofOfWork(blockchain.blocks.get(i)).validate());
        }
    }


//    public static void main(String[] args) {
//        BlockChainPersist blockchain = new BlockChainPersist();
//        blockchain.addBlock("Bob payed to me");
//
//    }
}
