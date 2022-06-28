package Blockchain;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    public List<Block> blocks;

     Blockchain(){
        Block genesisBlock = newGenesisBlock();
        this.blocks = new ArrayList<Block>();
        this.blocks.add(genesisBlock);
    }

    public void addBlock(String data){
        if(this.blocks == null ){
            this.blocks = new ArrayList<Block>();
        }
        Block prevBlock = this.blocks.get(blocks.size() - 1);
        Block newBlock = new Block(data, prevBlock.prevBlockHash);
        this.blocks.add(newBlock);

    }

    private Block newGenesisBlock() {
        return new Block("Genesis Block",new byte[0]);
    }

}
