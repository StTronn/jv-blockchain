package Blockchain;

import java.util.Arrays;

class BlockchainIterator  {
    private final Blockchain blockchain;
    private byte[] currHash;

    BlockchainIterator(Blockchain b){
       this.blockchain = b;
       this.currHash=b.getHash();
    }

    //hasNext
    public boolean hasNext(){
        if(currHash==null) return false;
        Block currentBlock = blockchain.getBlock(currHash);
        return currentBlock.prevBlockHash !=null;
    }

    //Next
    public  Block next(){
       Block block = blockchain.getBlock(currHash);
       currHash = block.prevBlockHash;
       if(currHash.length==0) currHash=null;
       return  block;
    }

}
