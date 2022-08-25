package UTXOSet;

import Blockchain.Blockchain;
import Blockchain.Block;
import Transaction.TXInput;
import Transaction.TXOutput;
import Transaction.Transaction;
import Wallet.Wallet;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UTXOSet {
    Blockchain blockchain;
    public final byte[] chainStateKey = "chainstate".getBytes(StandardCharsets.UTF_8);

    public UTXOSet(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    //reIndex
    //todo: use txId as key in redis as well
    public void reIndex(byte[] pubKeyHash) {
        Map<byte[], List<TXOutput>> UTXOsMap = blockchain.findUTXO();
        serialize(UTXOsMap);

    }

    //findSpendableOutputs
    //findUTXO
    public List<TXOutput> findUTXO(byte[] pubKeyHash) {
        List<TXOutput> UTXOs = new ArrayList<TXOutput>();
        Map<byte[], List<TXOutput>> UTXOsMap = deserialize();


        for (List<TXOutput> outputs : UTXOsMap.values()) {
            for (TXOutput out : outputs) {
                if (out.isLockedWithKey(Wallet.getAddressFromHashPubKey(pubKeyHash))) {
                    UTXOs.add(out);
                }
            }
        }

        return UTXOs;
    }


    //update
    public void update(Block block) {
        Map<byte[], List<TXOutput>> UTXOsMap = deserialize();

        for (Transaction tx : block.transactions) {
            if (tx.isCoinbase()) continue;

            for (TXInput in : tx.vin) {
                List<TXOutput> outputList = UTXOsMap.get(in.txId);
                List<TXOutput> updateOutputList = new ArrayList<TXOutput>();
                for (int index = 0; index < outputList.size(); index++) {
                    if (index != in.voutIndex) updateOutputList.add(outputList.get(index));
                }

                if (updateOutputList.size() == 0) {
                    UTXOsMap.remove(in.txId);
                } else {
                    UTXOsMap.put(in.txId, updateOutputList);
                }
            }

        }

    }

    private Map<byte[], List<TXOutput>> deserialize() {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);

        try (Jedis jedis = jedisPool.getResource()) {
            byte[] arrayStream = jedis.get(chainStateKey);
            InputStream in = new ByteArrayInputStream(arrayStream);
            ObjectInputStream objStream = new ObjectInputStream(in);
            Map<byte[], List<TXOutput>> outputs = (Map<byte[], List<TXOutput>>) objStream.readObject();
            return outputs;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void serialize(Map<byte[], List<TXOutput>> UTXOsMap) {

        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);

        try (Jedis jedis = jedisPool.getResource()) {
            if (!jedis.exists(chainStateKey)) {
                jedis.del(chainStateKey);
            }

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(UTXOsMap);
            jedis.set(chainStateKey, b.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
        jedisPool.close();
    }


}
