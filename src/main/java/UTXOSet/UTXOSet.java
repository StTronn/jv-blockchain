package UTXOSet;

import Blockchain.Blockchain;
import Blockchain.Block;
import Blockchain.SpendableInputs;
import Transaction.TXInput;
import Transaction.TXOutput;
import Transaction.Transaction;
import Wallet.Wallet;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UTXOSet {
    Blockchain blockchain;
    public final byte[] chainStateKey = "chainstate".getBytes(StandardCharsets.UTF_8);

    public UTXOSet(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    //reIndex
    //todo: use different key than converting byte[] to string for txId
    public void reIndex() {
        Map<String, List<TXOutput>> UTXOsMap = blockchain.findUTXO();

        serialize(UTXOsMap);

    }

    //findSpendableOutputs
    public SpendableInputs findSpendableInputs(String address) {
        int amount = 0;
        List<TXInput> inputs = new ArrayList<TXInput>();
        Map<String, List<TXOutput>> UTXOsMap = deserialize();
        for (String txIdString : UTXOsMap.keySet()) {
            List<TXOutput> outputs = UTXOsMap.get(txIdString);
            for (int index = 0; index < outputs.size(); index++) {
                TXOutput output = outputs.get(index);
                if (output.isLockedWithKey(address)) {
                    amount += output.value;
                    byte[] txId = Base64.getDecoder().decode(txIdString);
                    inputs.add(new TXInput(txId,index,address));
                }
            }
        }
        return new SpendableInputs(amount,inputs);
    }

    //findUTXO
    public List<TXOutput> findUTXO(String address) {
        List<TXOutput> UTXOs = new ArrayList<TXOutput>();
        Map<String, List<TXOutput>> UTXOsMap = deserialize();


        for (List<TXOutput> outputs : UTXOsMap.values()) {
            for (TXOutput out : outputs) {
                if (out.isLockedWithKey(address)) {
                    UTXOs.add(out);
                }
            }
        }

        return UTXOs;
    }


    //update
    public void update(Block block) {
        Map<String, List<TXOutput>> UTXOsMap = deserialize();

        for (Transaction tx : block.transactions) {
            if (tx.isCoinbase()) continue;

            for (TXInput in : tx.vin) {
                String key = Base64.getEncoder().encodeToString(in.txId);

                List<TXOutput> outputList = UTXOsMap.get(key);
                List<TXOutput> updateOutputList = new ArrayList<TXOutput>();
                for (int index = 0; index < outputList.size(); index++) {
                    if (index != in.voutIndex) updateOutputList.add(outputList.get(index));
                }

                if (updateOutputList.size() == 0) {
                    UTXOsMap.remove(key);
                } else {
                    UTXOsMap.put(key, updateOutputList);
                }
            }
            List<TXOutput> newOutputs = new ArrayList<TXOutput>();
            newOutputs.addAll(tx.vout);
            String txKey = Base64.getEncoder().encodeToString(tx.ID);
            UTXOsMap.put(txKey,newOutputs);
        }

        serialize(UTXOsMap);

    }

    private Map<String, List<TXOutput>> deserialize() {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);

        try (Jedis jedis = jedisPool.getResource()) {
            byte[] arrayStream = jedis.get(chainStateKey);
            InputStream in = new ByteArrayInputStream(arrayStream);
            ObjectInputStream objStream = new ObjectInputStream(in);
            Map<String, List<TXOutput>> outputs = (Map<String, List<TXOutput>>) objStream.readObject();

            return outputs;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void serialize(Map<String, List<TXOutput>> UTXOsMap) {

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
