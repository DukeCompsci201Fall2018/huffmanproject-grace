import java.io.*;
import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffMainCompress {
	//added this yourself!!!
	private static final int ALPH_SIZE = 257;
	private static final int BITS_PER_WORD = 8;
	private static final int PSEUDO_EOF = ALPH_SIZE;
	
	public static void main(String[] args) {
		
		System.out.println("Huffman Compress Main");
		File inf = FileSelector.selectFile();
		File outf = FileSelector.saveFile();
		if (inf == null || outf == null) {
			System.err.println("input or output file cancelled");
			return;
		}
		BitInputStream bis = new BitInputStream(inf);
		BitOutputStream bos = new BitOutputStream(outf);
		HuffProcessor hp = new HuffProcessor();
		hp.compress(bis, bos);
		System.out.printf("compress from %s to %s\n", 
		                   inf.getName(),outf.getName());
		System.out.printf("file: %d bits to %d bits\n",inf.length()*8,outf.length()*8);
		System.out.printf("read %d bits, wrote %d bits\n", 
				           bis.bitsRead(),bos.bitsWritten());
		long diff = bis.bitsRead() - bos.bitsWritten();
		System.out.printf("bits saved = %d\n",diff);
	}
	
	public void compress(BitInputStream in, BitOutputStream out) {
		
		int[] counts = readForCounts(in);
		HuffNode root = makeTreeFromCounts(counts);
		String[] codings = makeCodingsFromTree(root); 

		out.writeBits(HuffProcessor.BITS_PER_INT, HuffProcessor.HUFF_TREE);
		writeHeader(root, out);
		
		in.reset();
		writeCompressedBits(codings, in, out);
		//codings above is like encoding
		out.close();
 	}
	
	public int[] readForCounts(BitInputStream in) {
		
		int[] myInts257 = new int[ALPH_SIZE + 1];
		while (true) {
			int value = in.readBits(BITS_PER_WORD);
			if (value == -1) {
				break;
			}
			myInts257[value] += 1;
			//will read these bits and convert from binary to normal
		}
		return myInts257;
	}
	
	public HuffNode makeTreeFromCounts(int[] counts) {
		//counts = myInts257
		PriorityQueue<HuffNode> pq = new PriorityQueue<>();
		
		for(int i = 0; i < counts.length; i++) {
			if (counts[i] > 0) {
				pq.add(new HuffNode(i,counts[i],null,null));
				//value, weight, null, null 
			}
		}
		pq.add(new HuffNode(PSEUDO_EOF,1,null,null));

		while (pq.size() > 1) {
		    HuffNode left = pq.remove();
		    HuffNode right = pq.remove();
		    int totalWeight = left.myWeight + right.myWeight;
		    HuffNode newHN = new HuffNode(-1, totalWeight, left, right);

		    pq.add(newHN);
		}
		HuffNode root = pq.remove();
		//building the tree from bottom up
		return root;
	}
	
	public String[] makeCodingsFromTree(HuffNode root) {

		
		String[] encodings = new String[ALPH_SIZE + 1];
		makeCFTHelper(root, "", encodings);
		
		return encodings;
	}
	
	private void makeCFTHelper(HuffNode root, String path, String[] encodings) {
		if (root == null) return;
		
		if (root.myLeft == null && root.myRight == null) {
			encodings[root.myValue] = path;
			//the integer value of the character is hard coded
			//into the computer and you put into the 
			//array its path 
			return;
        }
		
		makeCFTHelper(root.myLeft, path + "0", encodings);
		makeCFTHelper(root.myRight, path + "1", encodings);
	}

	public void writeHeader(HuffNode root, BitOutputStream out) {
		
		if (root.myLeft != null || root.myRight != null) {
			out.writeBits(1, 0);
			
			writeHeader(root.myLeft, out); 
			writeHeader(root.myRight, out);			
		}
		
		else if (root.myLeft == null && root.myRight == null) {
			out.writeBits(1, 1);
			out.writeBits(BITS_PER_WORD, root.myValue);
		}
	}
	
	public void writeCompressedBits(String[] encodings, BitInputStream in, BitInputStream out) {
		
		while (true) {
			int bits = in.readBits(BITS_PER_WORD);
			
		}
		read file 
		encode character
		find it in encodings
		write it out
		
	}
	
}