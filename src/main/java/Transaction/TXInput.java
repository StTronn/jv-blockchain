package Transaction;

import Wallet.Wallet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class TXInput implements Serializable {
    public byte[] txId;
    public int voutIndex;
    public byte[] signature;
    public byte[] pubKey;
    public String address;

    //int vout is the index of the txin array of given txId
    public TXInput(byte[] txId, int voutIndex, String address) {
        this.txId = txId;
        this.voutIndex = voutIndex;
        this.address = address;
        this.signature = null;
        this.pubKey = null;
    }

    public boolean usesKey(byte[] pubKeyHash) {
        byte[] lockingHash = Wallet.hashPubKey(pubKeyHash);
        return lockingHash.equals(pubKeyHash);
    }

    public byte[] hashTxInput() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            if (txId == null) return new byte[]{};

            if (txId != null) outputStream.write(txId);
            outputStream.write(voutIndex);
            outputStream.write(address.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {

            System.out.println("txInput hashing cannot convert to byte hash " + e.toString());
        }
        return outputStream.toByteArray();
    }

    public boolean canUnlockOutput(String address) {
        return address.equals(this.address);
    }

    private byte[] bigIntToByteArray(final int i) {
        BigInteger bigInt = BigInteger.valueOf(i);
        return bigInt.toByteArray();
    }
}
