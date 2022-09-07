package MerkleTree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MerkleNode {
    public MerkleNode left;
    public MerkleNode right;
    public byte[] data;

    MerkleNode(MerkleNode left, MerkleNode right, byte[] data) {
        this.left = left;
        this.right = right;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            if (this.left == null && this.right == null) {
                md.update(data);
            } else {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(left.data);
                outputStream.write(right.data);
                byte output[] = outputStream.toByteArray();
                md.update(output);
            }
            this.data=md.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
           e.printStackTrace();

        }
    }
}
