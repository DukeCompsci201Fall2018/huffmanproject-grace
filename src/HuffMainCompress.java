import java.io.*;
import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffMainCompress {
	//added this yourself!!!
	private static final int ALPH_SIZE = 257;
	private static final int BITS_PER_WORD = 8;
	
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
		
		out.wrtieBits(BITS_PER_INT, HUFF_TREE);
		writeHeader(root, out);
		
		in.reset();
		writeCompressesBits(codings, in, out);
		out.close();
 	}
	
	public int[] readForCounts(BitInputStream in) {
		
		int[] myInts257 = new int[ALPH_SIZE + 1];
		while (in.hasNextBit()) {
			
		}
		int value = in.readBits(BITS_PER_WORD);
		myInts257[value] += 1;
		//finish this!!! 
	}
	
	public HuffNode makeTreeFromCounts(int[] counts) {
		
		PriorityQueue<HuffNode> pq = new PriorityQueue<>();
//		HashMap<Integer, Integer> myFreqMap = new HashMap<Integer, Integer>(); 
//
//		for(int j = 0; j < counts.length; j++) {
//			myFreqMap.putIfAbsent(counts[j], 0); 
//			int newVal = myFreqMap.get(counts[j])+ 1;
//			myFreqMap.put(counts[j], newVal);
//		}
//		
		for(int i = 0; i < counts.length; i++) {
			if (counts[i] > 1) {
				pq.add(new HuffNode(i,counts[i],null,null));
				//value, weight, null, null 
			}
			else {
				continue;
			}
		}

		while (pq.size() > 1) {
		    HuffNode left = pq.remove();
		    HuffNode right = pq.remove();
		    int totalWeight = left.myWeight + right.myWeight;
		    HuffNode newHN = new Huffnode(_?_, totalWeight, left, right);
		    		
		    // create new HuffNode t with weight from
		    // left.weight+right.weight and left, right subtrees
		    pq.add(t);
		}
		HuffNode root = pq.remove();
	}
}