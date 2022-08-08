package Wallet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.HexFormat;

public class Wallet {
    PublicKey publicKey;
    PrivateKey privateKey;
    static final byte[] version = new byte[]{0x00} ;
    static final int addressChecksum = 4;

    //generate wallet
    Wallet() {
        try {
            newKeyPair();
        } catch (Exception e) {
            System.out.print(e.toString());
            System.exit(1);
        }
    }

    public String getAddress() {
        byte[] pubKeyHash = hashPubKey(publicKey.getEncoded());
        byte[] versionPayload =appendVersion(pubKeyHash);
        byte[] checksum = getChecksum(versionPayload);
        byte[] fullPayload = appendChecksum(versionPayload,checksum);
        return Base58.encode(fullPayload);

    }

    private byte[] appendChecksum(byte[] versionPayload,byte[] checksum)  {
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

    private byte[] appendVersion(byte[] pubKeyHash){
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

    static public byte[] hashPubKey(byte[] key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            md.update(key);
            return Ripemd160.getHash(md.digest());
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] getChecksum(byte[] pubKey) {
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

    private void newKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        keyGen.initialize(256, random);

        KeyPair pair = keyGen.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
    }
}

