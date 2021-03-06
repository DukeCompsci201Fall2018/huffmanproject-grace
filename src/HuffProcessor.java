import java.util.PriorityQueue;

/**
 * Although this class has a history of several years,
 * it is starting from a blank-slate, new and clean implementation
 * as of Fall 2018.
 * <P>
 * Changes include relying solely on a tree for header information
 * and including debug and bits read/written information
 * 
 * @author Owen Astrachan
 */

public class HuffProcessor {

	public static final int BITS_PER_WORD = 8;
	public static final int BITS_PER_INT = 32;
	public static final int ALPH_SIZE = (1 << BITS_PER_WORD); 
	public static final int PSEUDO_EOF = ALPH_SIZE;
	public static final int HUFF_NUMBER = 0xface8200;
	public static final int HUFF_TREE  = HUFF_NUMBER | 1;

	private final int myDebugLevel;
	
	public static final int DEBUG_HIGH = 4;
	public static final int DEBUG_LOW = 1;
	
	public HuffProcessor() {
		this(0);
	}
	
	public HuffProcessor(int debug) {
		myDebugLevel = debug;
	}

	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be compressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
//	public void compress(BitInputStream in, BitOutputStream out){
//
//		while (true){
//			int val = in.readBits(BITS_PER_WORD);
//			if (val == -1) break;
//			out.writeBits(BITS_PER_WORD, val);
//		}
//		out.close();
//	}
	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be decompressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void decompress(BitInputStream in, BitOutputStream out){

		int bits = in.readBits(BITS_PER_INT);
		if(bits != HUFF_TREE) {
			throw new HuffException("illegal header starts with" + bits);
		}
		
		HuffNode root = readTreeHeader(in);
		readCompressedBits(root, in, out);
		out.close();
	}
	
	private HuffNode readTreeHeader(BitInputStream in) {
		
		int bit = in.readBits(1);
		if (bit == -1) {
			throw new HuffException("reading bits method fails"); 
			//not sure if this exception is okay or not. 
		}
		if (bit == 0) {
			HuffNode left = readTreeHeader(in);
			HuffNode right = readTreeHeader(in);
			return new HuffNode(0,0,left,right);
		}
		else {
			int value = in.readBits(BITS_PER_WORD + 1); 
			return new HuffNode(value,0,null,null);
		}
	}
	
	private void readCompressedBits(HuffNode root, BitInputStream in, BitOutputStream out) {
		
		HuffNode current = root; 
		while (true) {
		    int bits = in.readBits(1);
		    if (bits == -1) {
		        throw new HuffException("bad input, no PSEUDO_EOF");
		    }
		    else { 
		        if (bits == 0) {
		        	current = current.myLeft;
		        }
		        else current = current.myRight;

		        if (current.myLeft == null && current.myRight == null) {
		            if (current.myValue == PSEUDO_EOF) {
		            	break;
		            }
		            else {
		                out.writeBits(BITS_PER_WORD, current.myValue);
		                current = root;
		            }
		        }
		    }
	    }
	}
	public void compress(BitInputStream in, BitOutputStream out) {
		
		int[] counts = readForCounts(in);
		HuffNode root = makeTreeFromCounts(counts);
		String[] codings = makeCodingsFromTree(root); 

		out.writeBits(BITS_PER_INT, HUFF_TREE);
		writeHeader(root, out);
		
		in.reset();
		writeCompressedBits(codings, in, out);
		//codings above is like encoding
		out.close();
 	}
	
	private int[] readForCounts(BitInputStream in) {
		
		int[] myInts257 = new int[ALPH_SIZE + 1];
		while (true) {
			int value = in.readBits(BITS_PER_WORD);
			if (value == -1) {
				break;
			}
			myInts257[value] += 1;
			//will read these bits and convert from binary to normal
		}
		myInts257[PSEUDO_EOF] = 1;
		return myInts257;
	}
	
	private HuffNode makeTreeFromCounts(int[] counts) {
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
	
	private String[] makeCodingsFromTree(HuffNode root) {

		
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

	private void writeHeader(HuffNode root, BitOutputStream out) {
		
		if (root.myLeft != null || root.myRight != null) {
			out.writeBits(1, 0);
			
			writeHeader(root.myLeft, out); 
			writeHeader(root.myRight, out);			
		}
		
		else if (root.myLeft == null && root.myRight == null) {
			out.writeBits(1, 1);
			out.writeBits(BITS_PER_WORD + 1, root.myValue);
			//changed to bpw + 1
		}
	}
	
	private void writeCompressedBits(String[] encodings, BitInputStream in, BitOutputStream out) {
		
		while (true) {
			int bits = in.readBits(BITS_PER_WORD);
			
			if (bits == -1) {
				break;
			}
			String code = encodings[bits];
			//Integer.parseInt(code);
			out.writeBits(code.length(), Integer.parseInt(code,2));
		}
		String code = encodings[PSEUDO_EOF];
		out.writeBits(code.length(), Integer.parseInt(code,2));
	}
}