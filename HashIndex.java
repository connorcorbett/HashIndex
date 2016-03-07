/* HashIndex
 * Connor Corbett
 * 
 * HashIndex performs the majority of the work for this program. HashIndex will reopen the binary file created in
 * Prog2.java and will also create a new binary file used as buckets for the entries. HashIndex creates a 1D
 * array full of pointers to specific byte locations in the bucket binary file. These byte locations are filled
 * with SPM and line numbers from the previous binary file. The newley created bucket binary file can then be 
 * searched through based on a query designated by the user.
 * 
 * The only public class is startHashing() which is called in Prog2.java. startHashing() will being the process
 * of creating an extendible hash index and will call all other methods in this .java file.
 * 
 */


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HashIndex {

	private DirectoryObj[] directory = new DirectoryObj[10]; //The array to hold pointers
	private int globalDepth = 1; //The global depth of the directory
	private Scanner scan;
	private int totalBuckets = 10; //Used to speed up insertion into buckets, so only active buckets are looked at.
	
	/* startHashing
	 * 
	 * Will begin the creation of the extendible hash index. It is called by Prog2.java. startHashing will traverse 
	 * every SPM number in the past binary file and call writeToBucket on these SPM values in order to create buckets
	 * of data entries.
	 * 
	 * returns void
	 */
	public void startHashing() throws IOException{
		
		initDirectory();
		
		File fileRef = new File("out.bin"); //The file created in Prog2.java
		RandomAccessFile data = new RandomAccessFile(fileRef, "rw");
		
		File bucketFile = new File("bucket.bin"); //The file to hold the hash buckets
		RandomAccessFile bucket = new RandomAccessFile(bucketFile, "rw");
		
		/* Will loop through the initial directory setting each pointers count to 0. The position of count is the first
		 * int in the bucket binary file. Each bucket can hold 50 SPM values, 50 line numbers, and 1 count or 101 ints
		 * which is 404 bytes. 404 times the current index of the directory is used in the seek to find the beginning of
		 * each bucket.
		 */
		for(int i = 0; i < directory.length; i++){
			bucket.seek(i*404);
			bucket.writeInt(0);
		}
		
		/* This loop is used to pull the SPM number from each line in the binary file from Prog2.java. This SPM value and
		 * the line it was found on is used to call writeToBucket. The loop will run for every SPM value in the .bin file
		 */
		for(int i = 0; i < data.length() / 144; i++){
			int spm;
			data.seek(i * 144);
			spm = data.readInt();
			String indexString = String.valueOf(spm);
			writeToBucket(indexString, spm, i, bucket);
		}
		
		/* This will begin the querying process. The user is prompted to enter any value to search for in the data. The
		 * value entered by the user is passed to queryBucket() inorder to search through the buckets for the specified
		 * data.
		 */
		scan = new Scanner(System.in);
		while(true){
			System.out.print("Enter Value to Search for: ");
			String search = scan.next();
			if(search.compareTo("e") == 0){
				break;
			}
			queryBucket(bucket, search, data);
		}
		
		fileRef.delete(); //Delete the binary file from Prog2.java
		bucketFile.delete(); //Delete the binary file containing the buckets
		try {
			data.close();
			bucket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		scan.close();
		
	}

	/* initDirectory
	 * 
	 * Called at the beginning of startHasing in order to fill the directory with DirectoryObj that have a depth and
	 * key set.
	 * 
	 * return void
	 */
	private void initDirectory(){
		for(int i = 0; i < 10; i++){
			directory[i] = new DirectoryObj();
			directory[i].addDepth();
			directory[i].setHashPoint(String.valueOf(i));
		}
	}
	
	/* writeToBucket
	 * 
	 * This method will write SPM values and their line numbers to specific buckets determined by where the 
	 * SPM value matches with a given pointers key in the directory.
	 */
	private void writeToBucket(String ind, int spm, int line, RandomAccessFile buck) throws IOException{
		for(int i = 0; i < totalBuckets; i++){
			/* This if statment get a substring of both the pointers key and the SPM value. This way an SPM values 1st, 2nd etc
			 * digits can be matched to a pointer that is only 1, 2.. digits long.
			 */
			if(directory[i].getHashPoint().substring(0, directory[i].getDepth()).compareTo(ind.substring(0, directory[i].getDepth())) == 0 &&
					directory[i].getDepth() != 0){
				buck.seek(i*404); //Seek to the beginning of a bucket. 404 is the amount of bytes in a bucket and i is the offset of each bucket
				int temp = buck.readInt(); //temp holds the count of SPM values in a bucket
				if(temp != 50){
					buck.seek(i*404); //Seek to the beginning of the bucket
					buck.writeInt(temp + 1); //Increment the buckets count by 1
					buck.seek((i*404) + (temp * 8) + 4); //Seek to the last SPM value in the bucket
					buck.writeInt(spm); //Write the SPM value to the bucket and then the line number
					buck.writeInt(line);
					break;
				}else{
					splitBucket(buck, i, spm, line); //if the SPM value being added will be the 51st entry, split the bucket
					break;
				}
			}
		}
	}

	/* splitBucket
	 * 
	 * The purpose of this method is to split a bucket into 10 new buckets with a depth one greater then the bucket
	 * they are being split from. The split method is called whenever a bucket has 50 entries in it and another
	 * entry is trying to be added to that bucket.
	 */
	private void splitBucket(RandomAccessFile buck, int index, int spm, int line) throws IOException{
		if(directory[index].getDepth() < globalDepth){
			List<Integer> tempList = new ArrayList<Integer>(); //A list that will temporary hold SPM and line numbers for the bucket being split
			buck.seek(index * 404); //seek to the beginning of the bucket
			int temp = buck.readInt(); //temp will hold the count of the bucket
			for(int i = 0; i < temp; i++){
				tempList.add(buck.readInt()); //Add the SPM value to the list
				tempList.add(buck.readInt()); //Add the line number to the list
			}
			tempList.add(spm); //Add the SPM and line number that caused the split to the list
			tempList.add(line);
			
			buck.seek(index * 404); //Seek to the beginning of the bucket and reset the count to 0
			buck.writeInt(0);
			
			directory[index].addDepth(); //Add 1 to the depth of the bucket
			
			String hash = directory[index].getHashPoint(); //hash holds the key for the pointer to the bucket to be split
			directory[index].setHashPoint(hash + "0"); //Apend a 0 to the end of that key
			
			int concat = 1; //concat will iterate 1-9 creating 9 more buckets
			for(int i = totalBuckets; i < totalBuckets + 10; i++){ //Add these 9 new buckets to the end of the directory
				String newHash = hash.substring(0, hash.length()) + String.valueOf(concat); //New hash holds the old key with concat appended to the end of it
				directory[i] = new DirectoryObj(); 
				directory[i].setHashPoint(newHash);
				directory[i].setDepth(directory[index].getDepth());
				buck.seek(i*404);
				buck.writeInt(0);
				concat++;
			}
			
			totalBuckets = totalBuckets + 9;
			
			for(int i = 0; i < tempList.size(); i+=2){ //For each value in the list call writeToBucket to readd them to the newly created buckets
				writeToBucket(String.valueOf(tempList.get(i)), tempList.get(i), tempList.get(i + 1), buck);
			}
			
		}else{ //If the depth of the bucket is equal to the global depth, split the directory first then the bucket.
			splitDirectory(buck);
			splitBucket(buck, index, spm, line);
		}
	}

	/* splitDirectory
	 * 
	 * This method will increase the directories size by a factor of 10
	 */
	private void splitDirectory(RandomAccessFile buck) throws IOException{
		DirectoryObj[] temp = new DirectoryObj[directory.length * 10];
		for(int i = 0; i < totalBuckets; i++){
			temp[i] = directory[i];
		}
		
		directory = temp;
		globalDepth++;
	}
	
	/* queryBucket
	 * 
	 * This method will search through the buckets, printing out all SPM values that start with a users given input.
	 * Once it finds each SPM value that matches the users input queryBucket calls printLine on each SPM value displaying
	 * all 27 fields for each SPM value that are in the binary file from Prog2.java
	 */
	private void queryBucket(RandomAccessFile buck, String search, RandomAccessFile out) throws IOException {
		boolean found = false;
		int count = 0; //A count of all the SPM values found for a given user input
		for(int i = 0; i < totalBuckets; i++){
			String hash = directory[i].getHashPoint();
			int sub; //Sub will hold the length of the shortest string (either the key or user input)
			if(hash.length() > search.length()){
				sub = search.length();
			}else{
				sub = hash.length();
			}		
			if(hash.substring(0, sub).compareTo(search.substring(0, sub)) == 0){
				buck.seek(404*i);
				int temp = buck.readInt(); //Get the count of the bucket
				for(int k = 0; k < temp; k++){ //Loop through the bucket
					String tString = String.valueOf(buck.readInt()); //Convert the buckets SPM value to a string
					if(tString.substring(0, search.length()).compareTo(search) == 0){
						printLine(out, buck.readInt()); //Call printLine with the binary file and the line number of the given SPM value
						found = true;
						count++;
					}else{
						buck.readInt();
					}
				}
			}
		}
		if(found){
			System.out.println("Total records found: " + count);
		}else{
			System.out.println("No SPM values fit the entry: " + search);
		}
	}
	
	/*
	 * printLine
	 * 
	 * This method is used to print a certain line in the binary file. The printLine method takes in a 
	 * RandomAccessFile and the line number to print. It uses the seek method to look at the correct spot
	 * in the binary functions. The seek funtion takes in the line number multiplied by 144. 144 is the 
	 * amount of bytes in a line of data. The method then reads an int, double, or char and then prints that
	 * value out. At the end of the method it resets the seek to 0.
	 */
	private static void printLine(RandomAccessFile f, int lineNum) throws IOException{
		int i; //Will hold the current int being read
		double d; //Will hold the current double being read
		char c; //Will hold the current char being read
		
		/*
		 * Moves the current byte being looked at in the RandomAccess file by the line number
		 * multiplied by 144 (the amount of bytes in a line of data).
		 */
		f.seek(lineNum * 144);
		
		/*
		 * This line of code will read either an int, double, or char. The order in which they are read
		 * is based on the order in which they were written to the binary file.
		 */
		i = f.readInt();
		System.out.print(i + ",");
		i = f.readInt();
		System.out.print(i + ",");
		i = f.readInt();
		System.out.print(i + ",");
		d = f.readDouble();
		System.out.print(d + ",");
		c = (char)f.readChar();
		System.out.print(c + ",");
		i = f.readInt();
		System.out.print(i + ",");
		i = f.readInt();
		System.out.print(i + ",");
		d = f.readDouble();
		System.out.print(d + ",");
		d = f.readDouble();
		System.out.print(d + ",");
		d = f.readDouble();
		System.out.print(d + ",");
		i = f.readInt();
		System.out.print(i + ",");
		i = f.readInt();
		System.out.print(i + ",");
		d = f.readDouble();
		System.out.print(d + ",");
		d = f.readDouble();
		System.out.print(d + ",");
		d = f.readDouble();
		System.out.print(d + ",");
		d = f.readDouble();
		System.out.print(d + ",");
		d = f.readDouble();
		System.out.print(d + ",");
		d = f.readDouble();
		System.out.print(d + ",");
		d = f.readDouble();
		System.out.print(d + ",");
		i = f.readInt();
		System.out.print(i + ",");
		i = f.readInt();
		System.out.print(i + ",");
		i = f.readInt();
		System.out.print(i + ",");
		i = f.readInt();
		System.out.print(i + ",");
		i = f.readInt();
		System.out.print(i + ",");
		c = (char)f.readChar();
		System.out.print(c + ",");
		c = (char)f.readChar();
		System.out.print(c + ",");
		c = (char)f.readChar();
		System.out.print(c + "\n");
		
		f.seek(0); //Set the seek to 0, the beginning of the file
	}
	
	
}