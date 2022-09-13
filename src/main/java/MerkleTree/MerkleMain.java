package MerkleTree;

import Transaction.Transaction;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class MerkleMain {
    public static void main(String[] args){
        List<byte[]> inputs = new ArrayList<byte[]>();
        inputs.add("tx1".getBytes());
        inputs.add("tx2".getBytes());
        inputs.add("tx3".getBytes());
        inputs.add("tx4".getBytes());
        inputs.add("tx5".getBytes());
        inputs.add("tx6".getBytes());
//        inputs.add("tx7".getBytes());
//        inputs.add("tx8".getBytes());
//       byte[][] inputs = new byte[][]{ "tx1".getBytes(),"tx2".getBytes(),"tx3".getBytes(),"tx3".getBytes() };
       MerkleTree tree = new MerkleTree(inputs);
       byte[] testData ="tx4".getBytes();

       List<MerkleNode> proof = tree.generateProof(testData);
//       System.out.println(proof.size());

//        System.out.println(Arrays.toString("tx4".getBytes()));
//       MerkleNode testNode = new MerkleNode(null,null,"tx4".getBytes());

//        System.out.println(Arrays.toString(testNode.data));
//        System.out.println(tree.getIndexOfData("tx4".getBytes()));


       System.out.println(tree.verify(proof,tree.rootNode,testData));

    }
}
