package Wallet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Arrays;

public class Wallet {
    //TODO: make publicKey & privateKey private
    public PublicKey publicKey;
    public PrivateKey privateKey;
    private static final byte[] version = new byte[]{0x00} ;
    private static final int addressChecksum = 4;

    //generate wallet
    public Wallet() {
        try {
            newKeyPair();
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

    static public byte[] hashPubKey(byte[] key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            md.update(key);
            return Ripemd160.getHash(md.digest());
        } catch (Exception e) {
            return null;
        }
    }

    private void newKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        keyGen.initialize(256, random);

        KeyPair pair = keyGen.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
    }
}

