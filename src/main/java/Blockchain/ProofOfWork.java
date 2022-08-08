package Blockchain;

import Transaction.Transaction;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

public class ProofOfWork {
    final int targetBits = 0;
    Block block;
    byte[] target;
    int nonce =1;
    byte[] hash;

    ProofOfWork(Block block){
        this.block = block;
        this.target = Arrays.copyOfRange(BigInteger.ONE.shiftLeft(targetBits).toByteArray(),0,256);

    }

    public void run (){
        int maxNonce = Integer.MAX_VALUE;
        System.out.printf("Mining the block containing \"%s\"\n", new String(this.block.transactions[0].ID));
        while (nonce <= maxNonce -1) {
          try {
              byte[] toHash = prepareData(nonce++);
              MessageDigest md = MessageDigest.getInstance("SHA256");
              md.update(toHash);
              byte[] biHash = md.digest();

              if (Arrays.compare(biHash,target)>0){
                this.hash = biHash;
                return;
              }
          } catch (Exception e) {

          }

        }
    }


    private byte[] prepareData(int nonce){
        System.out.println(nonce);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            for(Transaction tx:this.block.transactions){
                output.write(tx.hashTx());
            }
            output.write(this.block.timeStamp.getBytes(StandardCharsets.UTF_8));
            output.write(this.block.prevBlockHash);
            output.write(targetBits);
            output.write(nonce);
        } catch (Exception e) {
            System.out.println(e.toString());
            return output.toByteArray();
        }

        return output.toByteArray();
    }


    public boolean validate() {
        byte[] toHash = prepareData(this.block.nonce);
        byte[] biHash;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            md.update(toHash);
            biHash = md.digest();
        } catch (Exception e){
            return false;
        }
        return Arrays.compare(biHash,target)>0;
    }



}
