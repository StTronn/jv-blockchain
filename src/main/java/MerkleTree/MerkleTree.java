package MerkleTree;


import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MerkleTree implements Serializable {
    MerkleNode rootNode;
    List<MerkleNode> nodes;

    public MerkleTree(byte[][] arrayOfByteArray) {
        List<byte[]> listOfByteArray = new ArrayList<byte[]>();
        Collections.addAll(listOfByteArray, arrayOfByteArray);
        generateMerkleTree(listOfByteArray);

    }

    public MerkleTree(List<byte[]> listOfByteArray) {
        generateMerkleTree(listOfByteArray);
    }

    public MerkleNode getRoot(){
        return this.rootNode;
    }


    private void generateMerkleTree(List<byte[]> listOfByteArray) {
        nodes = new ArrayList<MerkleNode>();
        List<MerkleNode> currNodes;
//        if (transactions.length % 2 != 0) listOfByteArray.add(listOfByteArray.get(listOfByteArray.size() - 1));

        for (byte[] bytes : listOfByteArray) {
            MerkleNode merkleNode = new MerkleNode(null, null, bytes);
            nodes.add(merkleNode);
        }
        currNodes = nodes;

        for (int level = 0; level < listOfByteArray.size() / 2; level++) {
            List<MerkleNode> newLevel = new ArrayList<MerkleNode>();

            for (int j = 0; j < currNodes.size(); j += 2) {
                MerkleNode newNode = new MerkleNode(currNodes.get(j), j + 1 == currNodes.size() ? null : currNodes.get(j + 1), null);

                newLevel.add(newNode);
            }

//            if(newLevel.size()%2==1) nodes.add(new MerkleNode(null,null,"".getBytes()));
            nodes.addAll(0,newLevel);
            if(newLevel.size()!=1 && newLevel.size()%2==1) nodes.add(newLevel.size(),new MerkleNode(null,null,"".getBytes()));
            currNodes = newLevel;
        }

        this.rootNode = currNodes.get(0);
//        Collections.reverse(nodes);
    }

    //traversal

    //verify given a transaction
    //TODO: convert to byte[] array as parameter inputs
     public boolean verify(List<MerkleNode> proof, MerkleNode root,byte[] data ) {
        MerkleNode leaf = new MerkleNode(null,null,data);
         int index=getIndexOfData(data);

        MerkleNode finalNode = leaf;
        for (MerkleNode node : proof) {
            if (index % 2 == 0) {
                finalNode = new MerkleNode(node, finalNode, null);
            } else {
                finalNode = new MerkleNode(finalNode, node, null);
            }
            index = index%2==0? (index / 2)-1:index/2;
        }
        return Arrays.toString(finalNode.data).equals(Arrays.toString(root.data));

    }

    public int getIndexOfData(byte[] data){

        MerkleNode txNode = new MerkleNode(null, null, data);
        int txIndex = -1;
        //getIndex
        for (int i = 0; i < nodes.size(); i++) {
            if (Arrays.toString(txNode.data).equals(Arrays.toString(nodes.get(i).data))) txIndex = i;
        }

        return txIndex;
    }


    public List<MerkleNode> generateProof(byte[] data) {
        List<MerkleNode> ret = new ArrayList<MerkleNode>();

        MerkleNode txNode = new MerkleNode(null, null, data);

        //TODO: replace with getIndexOfData
        int txIndex = -1;
        //getIndex
        for (int i = 0; i < nodes.size(); i++) {
            if (Arrays.toString(txNode.data).equals(Arrays.toString(nodes.get(i).data))) txIndex = i;
        }


        while (txIndex > 0) {
            if (txIndex % 2 == 0) {
                System.out.println(txIndex-1);
                ret.add(nodes.get(txIndex - 1));
                txIndex = (txIndex - 1) / 2;
            } else {
                System.out.println(txIndex+1);
                ret.add(nodes.get(txIndex + 1));
                txIndex = txIndex / 2;
            }
        }

        return ret;
    }


}
