package Transaction;

import Wallet.Base58;
import Wallet.Wallet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TXOutput {
    public String scriptPubKey;
    public  byte[] pubKeyHash;
    public int value;

    public void Lock(String address){
        byte[] hash = Base58.decode(address);
        this.pubKeyHash = Arrays.copyOfRange(hash,1,hash.length -4);
    }

    public  boolean isLockedWithKey(byte[] pubKeyHash){
        return pubKeyHash.equals(this.pubKeyHash);
    }

    public TXOutput(int value,String scriptPubKey){
       this.value=value;
       this.scriptPubKey=scriptPubKey;
    }

    public boolean canBeUnlockedWithInput(String scriptSig){
        return scriptPubKey.equals(scriptSig);
    }

    public byte[] hashTxOutput() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(value);
        outputStream.write(scriptPubKey.getBytes(StandardCharsets.UTF_8));
        return  outputStream.toByteArray();
    }
}
