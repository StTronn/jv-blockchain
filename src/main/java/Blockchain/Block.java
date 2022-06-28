package Blockchain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

public class Block {
    public String timeStamp;
    public byte[] data;
    public byte[] prevBlockHash;
    public byte[] hash;
    public int nonce;

    Block(String data, byte[] prevBlockHash){
        this.timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        this.data=data.getBytes();
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

    Block(String timeStamp,byte[] data,byte[] prevBlockHash,byte[] hash,int nonce){
       this.timeStamp=timeStamp;
       this.data=data;
       this.prevBlockHash=prevBlockHash;
       this.hash=hash;
       this.nonce=nonce;

    }

    public byte[] setHash() throws NoSuchAlgorithmException {
        byte[] _timestamp = timeStamp.getBytes();
        hash = joinByteStream(_timestamp,data,prevBlockHash);
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
