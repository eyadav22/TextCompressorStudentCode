/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Eisha Yadav
 */
public class TextCompressor {
    // Initialize the EOF
    public static final int EOF = 256;
    static int bitSize;
    // Allows 4096 (2^12) codes, more codes, more can be simplified.

    private static void compress() {
        // Initialize TST with all ascii values
        TST read = new TST();
        for(int i = 0; i < EOF; i++){
            read.insert("" + (char)i, i);
        } 
        // Include space for End of File Code
        int code = EOF + 1;

        // Read file
        String text = BinaryStdIn.readString();

        int index = 0;
        while(index < text.length()){
            // Use TST function
            String prefix = read.getLongestPrefix(text, index);
            // Convert String to code
            BinaryStdOut.write(read.lookup(prefix), bitSize);
            // Add code to TST if there is space
            int length = index+prefix.length();
            if(length < text.length() && code < 4096){
                read.insert(prefix + text.charAt(index+prefix.length()), code++);
            }
            // Shift index by added prefix
            index += prefix.length();
        }
        // Write end of file for expander to know when to stop
        // Close
        BinaryStdOut.write(EOF, bitSize);
        BinaryStdOut.close();
    }

    private static void expand() {
        // Fill map with all codes until end of file char reached
        String[] read = new String[4096];
        for(int i = 0; i < EOF; i++){
            read[i] = (char)i +"";
        }
        // Read in the first bit chunk into your first code
        int code = BinaryStdIn.readInt(bitSize);
        // Identify next non-EOF code
        int nextOpenCode = EOF + 1;
        String nextStr;
        int lookaheadCode;
        while(code != EOF){
            // Write current code
            String s = read[code];
            for (int i = 0; i < s.length(); i++)
                BinaryStdOut.write(s.charAt(i), 8);
            // Lookahead to next string to add to read array
            // If lookaheadCode is the end of file, terminate process
            lookaheadCode = BinaryStdIn.readInt(bitSize);
            if(lookaheadCode == EOF){
                break;
            }
            // Write the next code as the lookahead code
            nextStr = read[lookaheadCode];

            // Add new code if avalible
            if(nextOpenCode < 4096){
                // Fixes Sejwick's Edge case when the code is used immediately after, so you can't lookahead
                if (lookaheadCode == nextOpenCode) {
                    read[nextOpenCode++] = read[code] + read[code].charAt(0);
                }
                else {
                    // Add code in if not edge case
                    read[nextOpenCode++] = read[code] + nextStr.charAt(0);
                }
            }
            // Increment Current Code
            // read[code] = nextStr;
            code = lookaheadCode;
        }
        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        bitSize = Integer.parseInt(args[1]);
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
