package Blockchain;

import Transaction.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

public class Block {
    public String timeStamp;
    public Transaction[] transactions;
    public byte[] prevBlockHash;
    public byte[] hash;
    public int nonce;

    Block(Transaction[] transactions, byte[] prevBlockHash){
        this.timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        this.transactions=transactions;
        this.prevBlockHash=prevBlockHash;
        try {
            ProofOfWork pow = new ProofOfWork(this);
            pow.run();
            this.hash = pow.hash;
            this.nonce = pow.nonce;
        } catch (Exception e){
            this.hash = new byte[256];
        }
    }

    //required when we deseralize the data from proto
    Block(String timeStamp,Transaction[] transactions,byte[] prevBlockHash,byte[] hash,int nonce){
       this.timeStamp=timeStamp;
       this.transactions=transactions;
       this.prevBlockHash=prevBlockHash;
       this.hash=hash;
       this.nonce=nonce;

    }

    //calculate the hash of whole block based on its content
    public byte[] setHash() throws NoSuchAlgorithmException, IOException {
        byte[] _timestamp = timeStamp.getBytes();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(transactions);
        hash = joinByteStream(_timestamp,out.toByteArray(),prevBlockHash);
        MessageDigest md = MessageDigest.getInstance("SHA256");
        md.update(hash);
        return md.digest();
    }


    private byte[] joinByteStream(byte[] timestamp,byte[] data, byte[] prevBlockHash){
        byte[] c = new byte[timestamp.length + data.length + prevBlockHash.length];
        System.arraycopy(timestamp, 0, c, 0, timestamp.length);
        System.arraycopy(data, 0, c, timestamp.length, data.length);
        System.arraycopy(prevBlockHash, 0, c, data.length, prevBlockHash.length);
        return c;
    }
}
