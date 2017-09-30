public class Encryption {

   public final static int MAX_DEFAULT_SIZE = 16;
   public final static int LEVELS_OF_ENCRYPTION = 2;

   private int passwordSize = MAX_DEFAULT_SIZE;
   private StringBuilder keyWord = new StringBuilder("default");
   private StringBuilder finalEncryption = new StringBuilder();
   private String password = null;
   private int start = 0;
   private int offset = 0;
   private int userPIN = 0;
   private boolean forceSymbol = true;
   private int encryptionLevels = LEVELS_OF_ENCRYPTION;
   Alphabet alpha = new Alphabet();

   public Encryption () {

   }

   public int getStart() {
      return this.start;
   }

   public int getOffset() {
      return this.offset;
   }

   public String toString() {
      if (password == null) {
         return "No Password Found";
      } else {
         return password;
      }
   }

   public void setKey (String key) {
      this.keyWord.replace(0, this.keyWord.length(), key);
   }

   public void setUserPIN (int PIN) {
      this.userPIN = PIN;
   }

   public String getKey () {
      return keyWord.toString();
   }

   public void setMaxSize(int size) {
      passwordSize = size;
   }

   public int getMaxSize() {
      return passwordSize;
   }

   public void clear() {
      this.finalEncryption.delete(0, finalEncryption.length());
   }

   public void requireSymbol (boolean status) {
      this.forceSymbol = status;
   }

   public boolean getSymbolStatus () {
      return this.forceSymbol;
   }

   public String removeHTTP (String plainText) { //If there is an HTTP at the beginning of the plain text this removes it. It also sets the plain text to the maximum size in either case
      if (plainText.contains("http")) {
         int index = plainText.indexOf("/") + 2;
         if (passwordSize + index < plainText.length()) {
            plainText = plainText.substring(index, passwordSize + index);
         } else {
            plainText = plainText.substring(index);
         }
      }
      return plainText;
   }

   public static int Convert(char letter) {
      return (int)letter;
   }

   public static char Convert(int ascii) {
      return (char)ascii;
   }

   private char EncryptPlainNum (char charToEncrypt, char currentKeyLetter){
      int letterOutput;
      //System.out.println(alpha.getLetterIndex(currentKeyLetter));
      this.offset = (Alphabet.getLetterIndex(currentKeyLetter) + this.start);

      /*Offset starts as a letter of the keyword. Say the word is bat. The first letter b is 1 letter away from a and gives an offset
       * of 1. start selects a letter from the plain text, say the 7 letter, and finds that offset. If the letter was c there would be
       * an offset of 2. The start offset is then added to the regular offset so that everything is offset by 2 more units than
       * before. offsetMult is a value selected by the user that gives the offset a greater magnitude in order to prevent the encryption
       * from being stuck in one sequence of characters in the alphabet. I.E. If the offset was 2 units, each "unit" is now x number
       * of steps along the alphabet.
       */

      letterOutput = (Alphabet.getLetterIndex(charToEncrypt) + this.offset)%Alphabet.alphabet.length;
      //Take the cell number of the letter we found, move over x number of spaces, make sure it loops around to fit within the length
      //of the alphabet and then switch that int back to a char in the return statement.

      return Convert(Alphabet.alphabet[letterOutput]);
   }

   public String getEncryption (String plainText) {
      int k = 0;
      this.clear();
      String tempKey;
      String output;
      while (plainText.length() < passwordSize) {
         plainText = plainText + plainText; //This is really not a good solution for short plainText. It could create up to 15 Strings. The hope is that the majority of plain urls will be more than 15 characters.
      }

      this.start = Convert(plainText.charAt((userPIN%(passwordSize-5))+5)); //Picks a value other than the www. at the beginning of a url.
      for (int i = 0; i < passwordSize; i++) {

         finalEncryption.append(EncryptPlainNum(plainText.charAt(i),keyWord.charAt(k)));
         k++;
         if (k >= keyWord.length()) {
            k=0;
         }
      }
      if (forceSymbol == true) {
         int symbolChoice = offset%6;
         char selectedSymbol = '*';
         if (symbolChoice == 1){
            selectedSymbol = '@';
         } else if (symbolChoice == 2){
            selectedSymbol = '#';
         } else if (symbolChoice == 3){
            selectedSymbol = '%';
         } else if (symbolChoice == 4){
            selectedSymbol = '&';
         } else if (symbolChoice == 5){
            selectedSymbol = '(';
         } else if (symbolChoice == 6){
            selectedSymbol = ')';
         }

         finalEncryption.setCharAt(offset%passwordSize, selectedSymbol);
      }

      //After running the encryption once, the hashed string becomes the keyword and runs the process again.
      //This adds variance to very similar plaintext. 
      tempKey = this.keyWord.toString();
      this.setKey(finalEncryption.toString());
      this.encryptionLevels--;
      if (this.encryptionLevels > 0) {
    	  System.out.println(finalEncryption.toString());
    	  System.out.println(this.encryptionLevels);
    	  output = this.getEncryption(plainText);
      } else {
    	  this.encryptionLevels = this.LEVELS_OF_ENCRYPTION;
    	  System.out.println(finalEncryption.toString());
    	  System.out.println(this.encryptionLevels);
    	  return finalEncryption.toString();
      }
      
      this.setKey(tempKey);
	  return output;
   }
}
