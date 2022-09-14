package Wallet;

import Blockchain.Block;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Wallet implements Serializable {
    //TODO: make publicKey & privateKey private
    public PublicKey publicKey;
    public PrivateKey privateKey;
    private static final byte[] version = new byte[]{0x00} ;
    private static final int addressChecksum = 4;
    private static final byte[] walletStoreKey="wallet".getBytes(StandardCharsets.UTF_8);

    //generate wallet
    public Wallet() {
        try {
            newKeyPair();
            saveWallet();
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            System.out.print(e.toString());
            System.exit(1);
        }
    }


    public String getAddress() {
        return Wallet.getAddressFromPubKey(publicKey.getEncoded());
    }

    public static String getAddressFromPubKey(byte[] pubKey){
        byte[] pubKeyHash = hashPubKey(pubKey);
        return getAddressFromHashPubKey(pubKeyHash);
    }

    public static String getAddressFromHashPubKey(byte[] pubKeyHash){
        byte[] versionPayload =appendVersion(pubKeyHash);
        byte[] checksum = getChecksum(versionPayload);
        byte[] fullPayload = appendChecksum(versionPayload,checksum);
        return Base58.encode(fullPayload);
    }

     public static byte[] hashPubKey(byte[] key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            md.update(key);
            return Ripemd160.getHash(md.digest());
        } catch (Exception e) {
            return null;
        }
    }

    //getLoadedWallets
    public static  List<Wallet> getLoadedWallets(){
        List<Wallet> walletList = new ArrayList<Wallet>();
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()) {
            List<byte[]> walletListBytes= jedis.lrange(walletStoreKey,0,-1);
            for(byte[] walletbytes:walletListBytes){
               walletList.add(deserialize(walletbytes));
            }
        }
        jedisPool.close();
        return  walletList;
    }

    //save Wallet into redis
    public void saveWallet(){
        byte[] walletSerialized = serialize();

        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.lpush(walletStoreKey,walletSerialized);
//            if(!jedis.exists(walletStoreKey)){
//
//            }

        }
        jedisPool.close();

    }

    //generate keypair
    private void newKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        keyGen.initialize(256, random);

        KeyPair pair = keyGen.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
    }

    //deserealize
    private static Wallet deserialize(byte[] byteArrayWallet){
        try{
            InputStream in = new ByteArrayInputStream(byteArrayWallet);
            ObjectInputStream objStream = new ObjectInputStream(in);
            Wallet wallet = (Wallet) objStream.readObject();
            return wallet;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;

    }
    //serialize
    private byte[] serialize(){
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(this);
            return b.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static byte[] appendChecksum(byte[] versionPayload,byte[] checksum)  {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(versionPayload);
            outputStream.write(checksum);
            return outputStream.toByteArray();
        }catch (IOException e){
            System.out.println(e);
            System.out.println("Failed to set version returning byte array");
            System.exit(1);
            return null;
        }
    }

    private static byte[] appendVersion(byte[] pubKeyHash){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(pubKeyHash);
            outputStream.write(version);
            return outputStream.toByteArray();
        }catch (Exception e){
            System.out.println(e);
            System.out.println("Failed to set version returning byte array");
            return pubKeyHash;
        }

    }

    private static byte[] getChecksum(byte[] pubKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            md.update(pubKey);
            byte[] firstSHA = md.digest();
            md.update(firstSHA);
            byte[] secondSHA = md.digest();
            return Arrays.copyOfRange(secondSHA,0,addressChecksum);

        } catch (Exception e) {
            return null;
        }

    }


}

