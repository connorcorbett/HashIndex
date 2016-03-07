/* DirectoryObj
 * Connor Corbett
 * 
 * The purpose of this class is to be used as a pointer to specific hash bucket. This
 * object will be used in the 1D array directory. DirectoryObj uses its index in the 
 * 1D array as its "pointer" to the hash bucket
 * 
 * Methods:
 *   setHashPoint: Used to set a specific key for the pointer.
 *   getHashPoint: Returns the specific key.
 *   addDepth: Will increment the depth of the pointer by 1
 *   setDepth: Mainly used to reset a depth to 0 during splitting
 *   getDepth: Will return the depth of the pointer
 *   
 * Variables:
 * 	 hashPoint: The key that is used to decide which SPM values will guided by this pointer
 *   depth: Maintains the current depth of the pointer.
 * 
 */


public class DirectoryObj {

	private String hashPoint;
	private int depth = 0;
	
	public void setHashPoint(String point){
		hashPoint = point;
	}
	
	public String getHashPoint(){
		return hashPoint;
	}
	
	public void addDepth(){
		depth++;
	}
	
	public void setDepth(int add){
		depth = add;
	}
	
	public int getDepth(){
		return depth;
	}
}
