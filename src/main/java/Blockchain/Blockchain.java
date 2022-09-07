package Blockchain;

import Transaction.Transaction;
import Transaction.TXInput;
import Transaction.TXOutput;
import UTXOSet.UTXOSet;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.*;

public class Blockchain implements BlockchainInterface {
//    private List<Block> blocks;
    private static final byte[] genesisKey = "genesis_key".getBytes(StandardCharsets.UTF_8);
    private byte[] lastHash;
    public UTXOSet utxoSet;

    Blockchain(String genesisAddress) {
        Block genesisBlock = getGenesisBlock(genesisAddress);
//        this.lastHash=genesisBlock.hash;
//        this.blocks = new ArrayList<Block>();
//        this.blocks.add(genesisBlock);
//        setBlock(genesisBlock.hash,genesisBlock);
        utxoSet = new UTXOSet(this);
        utxoSet.reIndex();
    }

    public void addBlock(Transaction[] transactions) {
        BlockchainIterator it = new BlockchainIterator(this);
        Block latestBlock = it.next();
//        if (latestBlock == null) {
//            this.blocks = new ArrayList<Block>();
//        }
//      Block prevBlock = this.blocks.get(blocks.size() - 1);
        Block newBlock = new Block(transactions, latestBlock.prevBlockHash);
        setBlock(newBlock.hash,newBlock);
        utxoSet.update(newBlock);
        this.lastHash=newBlock.hash;

    }


    private Block newGenesisBlock(String genesisAddress) {
        List<Transaction> txn = Arrays.asList(Transaction.newCoinbaseTxn(genesisAddress, "paid 100 to rishav"));
        return new Block(txn.toArray(new Transaction[txn.size()]), new byte[0]);
    }

    public Transaction findTransaction(byte[] ID) {
        BlockchainIterator it = new BlockchainIterator(this);
        while(it.hasNext()){
            Block block = it.next();
            for (Transaction transaction : block.transactions) {
                if (Arrays.equals(transaction.ID, ID)) {
                    return transaction;
                }
            }
        }
        return null;
    }

    public void signTransaction(Transaction tx, PrivateKey privateKey) {
        Map<String, Transaction> prevTxns = new HashMap<String, Transaction>();
        for (TXInput input : tx.vin) {
            Transaction prevTxn = this.findTransaction(input.txId);
            prevTxns.put(Arrays.toString(input.txId), prevTxn);
        }
        tx.sign(privateKey, prevTxns);

    }

    public boolean verifyTransaction(Transaction tx) {
        Map<String, Transaction> prevTxns = new HashMap<String, Transaction>();
        for (TXInput input : tx.vin) {
            Transaction prevTxn = this.findTransaction(input.txId);
            prevTxns.put(Arrays.toString(input.txId), prevTxn);
        }
        return tx.verify(prevTxns);
    }

    public List<Transaction> findUnspentTransaction(String address) {
        List<Transaction> unspentTxn = new ArrayList<Transaction>();

        Map<String, List<Integer>> spentTxn = getSpentTxn(address);

        BlockchainIterator it = new BlockchainIterator(this);
        while(it.hasNext()){
            Block block = it.next();
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

    public Map<String, List<TXOutput>> findUTXO() {
        Map<String, List<TXOutput>> UTXOsMap = new HashMap<String, List<TXOutput>>();

        //txId -> [] //list of indexes spent
        Map<String, List<Integer>> spentTxn = getSpentTxn();

        BlockchainIterator it = new BlockchainIterator(this);
        while(it.hasNext()){
            Block block = it.next();
            for (Transaction transaction : block.transactions) {
                for (int index = 0; index < transaction.vout.size(); index++) {
                    TXOutput out = transaction.vout.get(index);
                    String key = Base64.getEncoder().encodeToString(transaction.ID);
                    if (spentTxn.get(key) != null) {
                        ArrayList<Integer> arr = (ArrayList<Integer>) spentTxn.get(key);
                        for (Integer spentIndex : arr) {
                            if (spentIndex != index) {
                                if (!UTXOsMap.containsKey(key)) {
                                    UTXOsMap.put(key, new ArrayList<TXOutput>());
                                }
                                UTXOsMap.get(key).add(out);
                            }
                        }
                    } else {
                        if (!UTXOsMap.containsKey(Arrays.toString(transaction.ID))) {
                            UTXOsMap.put(key, new ArrayList<TXOutput>());
                        }
                        UTXOsMap.get(key).add(out);
                    }

                }
            }
        }

        return UTXOsMap;
    }

    public List<TXOutput> findUTXOAddress(String address) {
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
        for (Transaction transaction : transactions) {
            if (!this.verifyTransaction(transaction)) {
                System.out.println("Error!: Invalid Transactions, cannot verify txn");
                System.exit(1);
            }
        }
        addBlock(transactions);
    }


    private Map<String, List<Integer>> getSpentTxn(String address) {

        Map<String, List<Integer>> spentTxn = new HashMap<String, List<Integer>>();
        BlockchainIterator it = new BlockchainIterator(this);
        while(it.hasNext()){
            Block block = it.next();
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

    private Map<String, List<Integer>> getSpentTxn() {
        Map<String, List<Integer>> spentTxn = new HashMap<String, List<Integer>>();
        BlockchainIterator it = new BlockchainIterator(this);
        while(it.hasNext()){
            Block block = it.next();
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
    public byte[] getHash(){
        return this.lastHash;
    }

    public Block getBlock(byte[] blockHash){
        if(blockHash==null) return null;
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] blockArrayStream = jedis.get(blockHash);
            if (blockArrayStream == null) {
                System.out.println("could not find block for the given hash");
                return null;
            }
            return deserialize(blockArrayStream);
        }catch (Exception e) {
            e.printStackTrace();
            jedisPool.close();
        }
        return null;
    }

    private void setBlock(byte[] hash, Block block) {

        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(block.hash, serialize(block));
        }
        jedisPool.close();
    }


    private Block deserialize (byte[] blockArrayStream){
        try{
            InputStream in = new ByteArrayInputStream(blockArrayStream);
            ObjectInputStream objStream = new ObjectInputStream(in);
            Block block = (Block) objStream.readObject();
            return block;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    private byte[] serialize(Block block) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(block);
            return b.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Block getGenesisBlock(String genesisAddress){
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()) {
            if(jedis.exists(genesisKey)){
                byte[] blockHash =jedis.get(genesisKey);
                return deserialize(jedis.get(blockHash));
            }
            Block genesisBlock = newGenesisBlock(genesisAddress);
            jedis.set(genesisKey,genesisBlock.hash);
            setBlock(genesisBlock.hash,genesisBlock);
            this.lastHash=genesisBlock.hash;
        }
        jedisPool.close();
        return  newGenesisBlock(genesisAddress);
    }

}

