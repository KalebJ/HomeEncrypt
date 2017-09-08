/* June 1, 2015
 * 	Removed special characters from alphabet. Special characters are added into final passwords through a case
 * 	statement in Encryption.java
 */

import java.util.Arrays;

public class Alphabet {
	public static int[] alphabet = new int[62];
	
	Alphabet () {
		Arrays.fill(alphabet, 0);
		BuildAlphabetArray();
	}

	public static void BuildAlphabetArray() {
		int k = 0;
			
		for (int i = 48; i<58; i++) {
			alphabet[k] = i;
			k++;
		}
		
		for (int i = 65; i<91; i++) {
			alphabet[k] = i;
			k++;
		}

		for (int i = 97; i<123; i++) {
			alphabet[k] = i;
			k++;
		}
	}
	
	public static int getLetterIndex (char letter) { //Finds the index of a desired Letter in the alphabet
		Arrays.sort(alphabet);
		return Arrays.binarySearch(alphabet, (int)letter);
	}
	
	/*public static void showAlphabet () {
		for (int i = 0; i< alphabet.length; i++) {
			System.out.print((char)alphabet[i] + ", ");
		}
	}
	
	public static void main (String[] args) {
		BuildAlphabetArray();
		showAlphabet();
	}*/

}
