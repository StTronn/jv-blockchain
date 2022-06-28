package Blockchain;
import blockProto.BlockProto;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import redis.clients.jedis.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BlockChainPersist {
    byte[] hash;

    public void addBlock(String data) {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()) {
            String prevHash = jedis.get("last_hash");
            Block block = new Block(data,prevHash.getBytes());

            this.hash=block.hash;
            jedis.set("last_hash",Arrays.toString(block.hash));
            jedis.set(Arrays.toString(block.hash), serializeBlock(block));

            String value = jedis.get(block.hash.toString());
            Block dbBlock = this.deSerializeBlock(value);
            System.out.println(dbBlock);
        }
        jedisPool.close();
    }

    public void newGenesisBlock(){
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()) {
            Block block = new Block("Genesis Block",new byte[0]);
            jedis.set("last_hash",Arrays.toString(block.hash));
            jedis.set(Arrays.toString(block.hash), serializeBlock(block));
            this.hash=block.hash;
        }
        jedisPool.close();
    }

    private void addBlock(Block block){
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()){
            jedis.set("last_hash",Arrays.toString(block.hash));
            jedis.set(Arrays.toString(block.hash), serializeBlock(block));
            String value = jedis.get(block.hash.toString());
            Block dbBlock = this.deSerializeBlock(value);
            System.out.println(dbBlock);
        }
        jedisPool.close();
    }

    private String serializeBlock(Block block){
        BlockProto.Block blockProto = BlockProto.Block.newBuilder()
                .setTimeStamp(block.timeStamp)
                .setNonce(0,block.nonce)
                .setPrevBlockHash(0, ByteString.copyFrom(block.prevBlockHash))
                .setData(0,ByteString.copyFrom(block.data))
                .setHash(0,ByteString.copyFrom(block.hash))
                .build();

       return Arrays.toString(blockProto.toByteArray());
    }

    private  Block deSerializeBlock(String block){
        try {
            BlockProto.Block blockProto = BlockProto.Block.parseFrom(block.getBytes());
            int nonce = (int) blockProto.getNonce(0);
            return new Block(
                    blockProto.getTimeStamp(),
                    blockProto.getData(0).toByteArray(),
                    blockProto.getPrevBlockHash(0).toByteArray(),
                    blockProto.getHash(0).toByteArray(),
                    nonce
                    );
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }


}
