package Blockchain;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import redis.clients.jedis.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BlockChainPersist implements  BlockchainInterface {
    byte[] last_hash;

    BlockChainPersist(){
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()) {
            if(!jedis.exists("last_hash")){
//               this.newGenesisBlock();
            }
        }
        jedisPool.close();
    }

    public void addBlock(Transaction[] txn) {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()) {
            String prevHash = jedis.get("last_hash");
            Block block = new Block(txn,prevHash.getBytes());

            this.last_hash =block.hash;
            jedis.set("last_hash".getBytes(StandardCharsets.UTF_8),block.hash);
//            jedis.set(block.hash, serializeBlock(block));

            byte[] value = jedis.get(block.hash);
//            Block dbBlock = this.deSerializeBlock(value);
//            System.out.println(dbBlock);
        }
        jedisPool.close();

    }

//    public void newGenesisBlock(){
//        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
//        try (Jedis jedis = jedisPool.getResource()) {
//            Block block = new Block(Transaction.newCoinbaseTxn("rishav","paid 100 to rishav"),new byte[0]);
//            jedis.set("last_hash".getBytes(StandardCharsets.UTF_8),block.hash);
//            jedis.set(block.hash, serializeBlock(block));
//            this.last_hash =block.hash;
//        }
//        jedisPool.close();
//    }

//    private void addBlock(Block block){
//        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
//        try (Jedis jedis = jedisPool.getResource()){
//            jedis.set("last_hash".getBytes(StandardCharsets.UTF_8),block.hash);
//            jedis.set(block.hash, serializeBlock(block));
//            byte[] value = jedis.get(block.hash);
//            Block dbBlock = this.deSerializeBlock(value);
//            System.out.println(dbBlock);
//        }
//        jedisPool.close();
//    }

//    private byte[] serializeBlock(Block block){
//
//        BlockProto.Block blockProto = BlockProto.Block.newBuilder()
//                .setTimeStamp(block.timeStamp)
//                .setNonce(block.nonce)
//                .setTransactions(ByteString.copyFrom(block.transactions.toString().getBytes(StandardCharsets.UTF_8)))
//                .setPrevBlockHash(ByteString.copyFrom(block.prevBlockHash))
//                .setHash(ByteString.copyFrom(block.hash))
//                .build();
//
//       return blockProto.toByteArray();
//    }

//    private  Block deSerializeBlock(byte[] block){
//        try {
//            BlockProto.Block blockProto = BlockProto.Block.parseFrom(block);
//            System.out.println((int)blockProto.getNonce());
//            return new Block(
//                    blockProto.getTimeStamp(),
//                    blockProto.getTransactions().toByteArray(),
//                    blockProto.getPrevBlockHash().toByteArray(),
//                    blockProto.getHash().toByteArray(),
//                    (int)blockProto.getNonce()
//                    );
//        } catch (InvalidProtocolBufferException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    private static Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        Arrays.setAll(bytes, n -> bytesPrim[n]);
        return bytes;
    }

}
