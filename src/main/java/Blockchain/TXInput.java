package Blockchain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class TXInput {
    byte[] txId;
    int voutIndex;
    String scriptSig;

    //int vout is the index of the txin array of given txId
    TXInput(byte[] txId, int voutIndex, String scriptSig){
        this.txId = txId;
        this.voutIndex = voutIndex;
        this.scriptSig = scriptSig;
    }

    public byte[] hashTxInput() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {

            if(txId!=null) outputStream.write(txId);
            outputStream.write(voutIndex);
            outputStream.write(scriptSig.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {

            System.out.println(e.toString());
        }
        return  outputStream.toByteArray();
    }

    public boolean canUnlockOutput(String scriptPubKey){
        return scriptSig.equals(scriptPubKey);
    }

    private byte[] bigIntToByteArray( final int i ) {
        BigInteger bigInt = BigInteger.valueOf(i);
        return bigInt.toByteArray();
    }
}
