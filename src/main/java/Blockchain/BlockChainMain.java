package Blockchain;

import java.util.Arrays;

public class BlockChainMain {
    public static void main(String[] args) {
        Blockchain blockchain = new Blockchain();
        blockchain.addBlock("Bob Payed to alice 1 coin");
        blockchain.addBlock("Boy payed to janice 1 coin");

        for (int i = 0;i<blockchain.blocks.size();i++){
            System.out.println("Data: " + new String(blockchain.blocks.get(i).data));
            System.out.println( "Hash: " + Arrays.toString(blockchain.blocks.get(i).hash));
            System.out.println( "pow: " + new ProofOfWork(blockchain.blocks.get(i)).validate());
        }
    }

////    public static void main(String[] args) {
////        BlockChainPersist blockchain = new BlockChainPersist();
////        blockchain.newGenesisBlock();
////        blockchain.addBlock("Bob payed to me");
////
//    }
}
