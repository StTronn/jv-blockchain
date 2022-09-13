package MerkleTree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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
                this.data = md.digest();
            } else {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                if(left!=null) outputStream.write(left.data);
                if(right!=null) outputStream.write(right.data);
                byte output[] = outputStream.toByteArray();
                md.update(output);
                this.data=md.digest();
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();

        }
    }

    public byte[] getBytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            if (left != null) outputStream.write(left.getBytes());
            if (right != null) outputStream.write(right.getBytes());
            if (data != null) outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public byte[] generateHash(byte[] leftData, byte[] rightData) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(left.data);
            outputStream.write(right.data);
            byte output[] = outputStream.toByteArray();
            md.update(output);
            return md.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
