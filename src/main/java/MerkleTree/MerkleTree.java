package MerkleTree;

import Transaction.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MerkleTree {
    MerkleNode rootNode;

    MerkleTree(Transaction[] transactions) {
        List<MerkleNode> nodes = new ArrayList<MerkleNode>();
        List<Transaction> transactionList = Arrays.asList(transactions);

        //make the size even
        if (transactions.length % 2 != 0) transactionList.add(transactionList.get(transactionList.size() - 1));

        //childNodes
        for (Transaction transaction : transactionList) {
            MerkleNode merkleNode = new MerkleNode(null, null, transaction.hashTx());
            nodes.add(merkleNode);
        }

        for (int level = 0; level < transactionList.size() / 2; level++) {
            List<MerkleNode> newLevel = new ArrayList<MerkleNode>();

            for (int j = 0; j < nodes.size(); j += 2) {
                MerkleNode newNode = new MerkleNode(nodes.get(j),nodes.get(j+1),null);
                newLevel.add(newNode);
            }
            nodes = newLevel;
        }
        this.rootNode = nodes.get(0);
    }

}
