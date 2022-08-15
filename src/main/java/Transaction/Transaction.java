package Transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

public class Transaction {
    static final int reward = 100;
    //hash of the current values
    public byte[] ID;
    public List<TXInput> vin;
    public List<TXOutput> vout;

    public Transaction(List<TXInput> vin, List<TXOutput> vout){
        this.vin = vin;
        this.vout = vout;
        this.getId();
    }

    public static Transaction newCoinbaseTxn(String to,String data){
        if (data.isEmpty()){
            data = String.format("Reward to %s",to);
        }
        TXInput in = new TXInput(null,-1,data );
        TXOutput out = new TXOutput(reward,to);

        Transaction txn = new Transaction(Arrays.asList(in),Arrays.asList(out));
        return txn;
    }

    private void getId() {
        int a = this.hashCode();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            md.update(String.valueOf(a).getBytes(StandardCharsets.UTF_8));
            this.ID = md.digest();

        } catch (Exception NoSuchAlgorithmException){
            this.ID = null;
        }
    }

    public byte[] hashTx() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(ID);
        for(TXInput in:vin){
            outputStream.write(in.hashTxInput());
        }
        for(TXOutput out:vout){
            outputStream.write(out.hashTxOutput());
        }
        return  outputStream.toByteArray();
    }

//    public static Transaction newUTXOTransaction(String from,String to,int amount,Blockchain blockchain){
//
//        List<TXInput> vin;
//        List<TXOutput> vout;
//
//    }
}
