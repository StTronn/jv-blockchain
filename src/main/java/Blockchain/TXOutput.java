package Blockchain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TXOutput {
    String scriptPubKey;
    int value;

    TXOutput(int value,String scriptPubKey){
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
