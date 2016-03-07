/* Connor Corbett
 * 
 * This program is designed to open a .dat file that contains 27 data entries per line. Those data
 * entries are then used to create a new binary file of data. This binary file is then used to create
 * an extendible hash index. This hash index is composed of a directory full of pointers that point to
 * a specific bucket in a hash bucket file. Each bucket can hold 50 entries (an entry is an SPM value
 * and a line number) and will be searched through, which is determined by a query entered by the user.
 *
 * Written in Java 8
 *
 * The only problem with my program is the speed. When a .dat file that contains 100k+ data entries
 * is used the program will take multiple minutes to finish.
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;


public class CreateBin {
	
	public static void main(String args[]) throws IOException {
		
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter file name: ");
		String fileString = scan.next(); //The name of the file to open
		
		/* 
		 * These String variables will hold the original 27 data values for a line in the .dat file
		 * as a string. These string variables will later be parsed into their required type.
		 */
		String SPM, RAh, RAm, RAs, DE, DEd, DEm, DEs, RAdeg;
		String DEdeg, e_RAdeg, e_DEdeg, pmRA, pmDE, e_pmRA, e_pmDE, Vmag, Bmag;
		String B_V, e_Vmag, e_Bmag, Nfie, o_Bmag, o_Vmag, Fone, Ftwo, Fthree;
		
		FileReader file = null;
		
		String fileName = "out.bin"; //The name of the binary file
		
		/*
		 * This part of the code will attempt to open the file designated by the user. If the file is
		 * not found the program will print an error message then exit the program.
		 */
		try{
			file = new FileReader(fileString);
		}catch(FileNotFoundException e){
			System.out.println("File Not Found, Exiting Program");
			System.exit(-1);
		}
		
		BufferedReader f = new BufferedReader(file); //A buffer that will be used to pull data from the .dat file

		/*
		 * Creates a new RandomAccessFile, os, to store the ValueObjects variables in binary format
		 */
		File fileRef = new File(fileName);
		RandomAccessFile os;
		os = new RandomAccessFile(fileRef, "rw");

		/*
		 * This loop will run as long as the BufferedReader is not at the end of the file. The loop runs one line at a time
		 * pulling individual chunks of bytes as strings and stores those strings in a variable that is named according to
		 * the readMe file. The strings are created by calling a method called getValue that takes in a BufferedReader, the 
		 * amount of bytes to pull and a skip value that will skip over white space in the .dat file. After the strings are pulled
		 * The strings are parsed to the correct type (int, double or char). These new parsed variables are stored in a new 
		 * ValueObject. This new ValueObject is then added to the list of ValueObjects.
		 */
		while(f.ready()){
			
			SPM = getValue(f, 8, 0);
			RAh = getValue(f, 2, 1);
			RAm = getValue(f, 2, 1);
			RAs = getValue(f, 6, 1);
			DE = getValue(f, 1, 1);
			DEd = getValue(f, 2, 0);
			DEm = getValue(f, 2, 1);
			DEs = getValue(f, 5, 1);
			RAdeg = getValue(f, 10, 1);
			DEdeg = getValue(f, 10, 1);
			e_RAdeg = getValue(f, 4, 0);
			e_DEdeg = getValue(f, 4, 0);
			pmRA = getValue(f, 7, 1);
			pmDE = getValue(f, 7, 1);
			e_pmRA = getValue(f, 5, 1);
			e_pmDE = getValue(f, 5, 0);
			Vmag = getValue(f, 5, 0);
			Bmag = getValue(f, 5, 1);
			B_V = getValue(f, 5, 1);
			e_Vmag = getValue(f, 3, 0);
			e_Bmag = getValue(f, 3, 0);
			Nfie = getValue(f, 1, 3);
			o_Bmag = getValue(f, 2, 1);
			o_Vmag = getValue(f, 2, 1);
			Fone = getValue(f, 1, 1);
			Ftwo = getValue(f, 1, 0);
			Fthree = getValue(f, 1, 0);
			getValue(f, 0, 2);

			os.writeInt(Integer.parseInt(SPM));
			os.writeInt(Integer.parseInt(RAh));
			os.writeInt(Integer.parseInt(RAm));
			os.writeDouble(Double.parseDouble(RAs));
			os.writeChar(DE.charAt(0));
			os.writeInt(Integer.parseInt(DEd));
			os.writeInt(Integer.parseInt(DEm));
			os.writeDouble(Double.parseDouble(DEs));
			os.writeDouble(Double.parseDouble(RAdeg));
			os.writeDouble(Double.parseDouble(DEdeg));
			os.writeInt(Integer.parseInt(e_RAdeg));
			os.writeInt(Integer.parseInt(e_DEdeg));
			os.writeDouble(Double.parseDouble(pmRA));
			os.writeDouble(Double.parseDouble(pmDE));
			os.writeDouble(Double.parseDouble(e_pmRA));
			os.writeDouble(Double.parseDouble(e_pmDE));
			os.writeDouble(Double.parseDouble(Vmag));
			os.writeDouble(Double.parseDouble(Bmag));
			os.writeDouble(Double.parseDouble(B_V));
			os.writeInt(Integer.parseInt(e_Vmag));
			os.writeInt(Integer.parseInt(e_Bmag));
			os.writeInt(Integer.parseInt(Nfie));
			os.writeInt(Integer.parseInt(o_Bmag));
			os.writeInt(Integer.parseInt(o_Vmag));
			os.writeChar(Fone.charAt(0));
			os.writeChar(Ftwo.charAt(0));
			os.writeChar(Fthree.charAt(0));
			
		}
		
		os.close(); //Close the newly created binary file.

		System.out.println("\nBinary File Created\n");
		
		HashIndex hashing = new HashIndex();
		hashing.startHashing();
	}
	
	/*
	 * getValue
	 * 
	 * getValue is used to get a certain value from the .dat file. This value is stored as a string and
	 * is obtained by going byte by byte through the .dat file. The amount of bytes to pull is determined
	 * by the variable length. It then skips a certain number of white space determined by the variable skip.
	 * 
	 * Returns String
	 */
	private static String getValue(BufferedReader f, int length, int skip) throws IOException{
		
		String retString = ""; //The string to return
		
		/*
		 * A loop to skip a predetermined amount of white space.
		 */
		for(int i = 0; i < skip; i++){
			f.read();
		}
		
		/*
		 * This loop will run for an amount of bytes determined by the variable length. At each byte a temp char
		 * is created and added onto the end of the return String. If the char is a white space it is not added
		 * to the String.
		 */
		for(int i = 0; i < length; i++){
			char temp = (char)f.read();
			if(temp != ' '){
				retString = retString + temp;
			}
		}
		
		/*
		 * At the end of the loop if the returnString is still empty it is set equal to a single white space.
		 */
		if(retString.length() == 0){
			retString = " ";
		}
		
		return retString; //Return the created string.
	}
	


}
