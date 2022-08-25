package Transaction;

import Wallet.Base58;
import Wallet.Wallet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

public class TXOutput implements Serializable {
    public  byte[] pubKeyHash;
    public int value;

    public TXOutput(int value,String address){
       this.value=value;
       lock(address);
    }

    public void lock(String address){
        byte[] hash = Base58.decode(address);
        this.pubKeyHash = Arrays.copyOfRange(hash,0,hash.length -5);
    }

    public  boolean isLockedWithKey(String address){
        return address.equals(Wallet.getAddressFromHashPubKey(this.pubKeyHash));
    }

//    public boolean canBeUnlockedWithInput(String scriptSig){
//        return scriptPubKey.equals(scriptSig);
//    }

    public byte[] hashTxOutput() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(value);
        outputStream.write(pubKeyHash);
        return  outputStream.toByteArray();
    }
}
