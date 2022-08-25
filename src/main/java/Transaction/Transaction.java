package Transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Transaction {
    static final int reward = 100;
    //hash of the current values
    public byte[] ID;
    public List<TXInput> vin;
    public List<TXOutput> vout;

    public Transaction(List<TXInput> vin, List<TXOutput> vout){
        this.vin = vin;
        this.vout = vout;
        this.ID = this.hashTx();
    }

    private Transaction(Transaction toCopy){
        this.ID = toCopy.ID;
        this.vin = toCopy.vin;
        this.vout = toCopy.vout;
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
    public boolean isCoinbase(){
        List<TXInput> inputs = this.vin;
       return  inputs.size() ==1 && inputs.get(0).txId == null &&  inputs.get(0).voutIndex == -1;
    }

    public void sign(PrivateKey privateKey,Map<String,Transaction> prevTxs){
        //TODO: if coinbase return
        if(this.isCoinbase()) return;

        Transaction copyTx = new Transaction(this);

        for(int index=0;index<copyTx.vin.size();index++){
            TXInput in = vin.get(index);
            Transaction prevTx = prevTxs.get(Arrays.toString(in.txId));
            in.signature=null;
            in.pubKey = prevTx.vout.get(in.voutIndex).pubKeyHash;
            //checking if
            System.out.println("checking if updated"+ Arrays.toString(vin.get(index).pubKey));
            copyTx.ID = in.hashTxInput();
            try {
                Signature ecdsa = Signature.getInstance("SHA256withECDSA");
                ecdsa.initSign(privateKey);
                ecdsa.update(copyTx.ID);
                in.signature = ecdsa.sign();
            } catch (java.security.NoSuchAlgorithmException e){
                System.out.println(e.toString());
                System.out.println("signing transaction no suchAlgorithm SHA256withECDSA");
                e.printStackTrace();

            } catch (InvalidKeyException e) {
                System.out.println("Invalid private key: signing transaction ");
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            }
            in.pubKey = null;




        }
    }

    public boolean verify(Map<String,Transaction> prevTxs){
//        if(this.isCoinbase()) return true;
        return true;
        //TODO: check for coinbase txn
        //TODO: Extract public key from signature

//        Transaction copyTx = new Transaction(this);
//        for(int index=0;index<copyTx.vin.size();index++) {
//            TXInput in = vin.get(index);
//            Transaction prevTx = prevTxs.get(Arrays.toString(in.txId));
//            in.signature = null;
//            in.pubKey = prevTx.vout.get(in.voutIndex).pubKeyHash;
//            copyTx.ID = in.hashTxInput();
//
//            try {
//                Signature ecdsa = Signature.getInstance("SHA256withECDSA");
//                ecdsa.initVerify(in.pubKey);
//                ecdsa.update(copyTx.ID);
//                in.signature = ecdsa.sign();
//            } catch (java.security.NoSuchAlgorithmException e){
//                System.out.println(e.toString());
//                System.out.println("signing transaction no suchAlgorithm SHA256withECDSA");
//                e.printStackTrace();
//
//            } catch (InvalidKeyException e) {
//                System.out.println("Invalid private key: signing transaction ");
//                e.printStackTrace();
//            } catch (SignatureException e) {
//                e.printStackTrace();
//            }
//        }

    }
//    private Transaction getTrimmedCopy(){
//        List<TXOutput> outputs = new ArrayList<TXOutput>();
//        List<TXInput> inputs = new ArrayList<TXInput>();
//
//        for(TXInput in:this.vin){
//            inputs.add(in);
//        }
//        for(TXOutput out: this.vout){
//            outputs.add(out);
//        }
//    }


    public byte[] hashTx()  {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {

            for (TXInput in : vin) {
                outputStream.write(in.hashTxInput());
            }
            for (TXOutput out : vout) {
                outputStream.write(out.hashTxOutput());
            }
        } catch (IOException e){
            e.printStackTrace();
           System.out.println("hashTx: failed to convert object to byte");
        }
        byte[] hash = outputStream.toByteArray();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            md.update(hash);
            return  md.digest();

        } catch (Exception NoSuchAlgorithmException){
            System.out.println("no suchAlgorithmException while hashing transaction");
            NoSuchAlgorithmException.printStackTrace();
            this.ID = null;
        }
        return null;
    }

//    public static Transaction newUTXOTransaction(String from,String to,int amount,Blockchain blockchain){
//
//        List<TXInput> vin;
//        List<TXOutput> vout;
//
//    }
}
