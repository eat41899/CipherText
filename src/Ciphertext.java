/*
 * Created by Eric Tapia on 9/19/2016.
 * Prof Vahab
 * Comp 424
 */

import java.io.IOException;
import java.util.*;
import java.lang.String;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class Ciphertext{
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static void main(String args[]) throws IOException{
        //Given string to be deciphered
        String cipherStr = "DRPWPWXHDRDKDUBKIHQVQRIKPGWOVOESWPKPVOBBDVVVDXSURWRLUEBKOLVHIHBKHLHBLNDQRFLOQ";
        //Dictionary object used to read final message
        Dictionary dictionary = new Dictionary();

        //Map each character(key) to it's frequency count(value)
        Map freqMap = mostFrequent(cipherStr);

        //Sort by most frequent first and add to list
        ArrayList<Character> sortedList = frequencySort(freqMap);

        //Find possible shifts based on the most frequent
        Set<Integer> possibleShifts = findShifts(sortedList);

        //attempt every candidate shift on the ciphered message
        Set<String> possibleStrings = new HashSet<>();
        for(Integer shift : possibleShifts){
            //add substitution decrypted string to a Set (does't allow dups)
            possibleStrings.add(decryptSubstitution(shift, cipherStr));
        }

        //Add each permutation to the keyList
        List<String> keyList = new ArrayList<>();
        //Begin with the first three letters of the alphabet and up
        // to 5 chars. Starting half way from the assumption hint
        // provided in the assignment (<= 10)
        for(int i = 3; i <= 5; i++){
            //key is generated from Alphabet starting at index 0 to i-1
            String key = ALPHABET.substring(0, i);
            //Add each permutation generated from the current key
            for(String str : permutation(key)){
                keyList.add(str);
            }
        }

        int count = 1;
        //Col Transposition, test each key against each candidate string
        for(String strCandidate : possibleStrings){
            for(String strKeys : keyList){
                //Decipher based on Columnar Transposition
                String deciphered = decryptColTransp(strCandidate, strKeys);
                //Attempt to read the deciphered message using dictionary words.
                //SegmentString method is not intended to segment the entire
                // message, only attempts to and is used as a helper method
                // to filter candidates which will then be decrypted directly
                // to the decryptColTransp method
                if(segmentString(deciphered, dictionary) != null){
                    System.out.println("Candidate: [" + strCandidate + "] key = " + strKeys);
                    System.out.println("Deciphered: #" + count + " [" + segmentString(deciphered, dictionary) + "]");
                    System.out.println();
                    count++;
                }
            }
        }

        //Best match based on previous OUTPUT which segments the string based on dictionary words.
        /*Candidate: [AOMTMTUEAOAHARYHFENSNOFHMDTLSLBPTMHMSLYYASSSAUPROTOIRBYHLISEFEYHEIEYIKANOCILN]
          key = CEBAD
        Deciphered: #19 [BE HAPPY FOR THE MOMENT THIS MOMENT IS YOUR LIFE BY KHAY YAMOH AND ALSO THIS CLASS IS REALLY XXX]*/

        //Test the best match (mentioned above) candidate and key without segmenting the string
        String candidate = "AOMTMTUEAOAHARYHFENSNOFHMDTLSLBPTMHMSLYYASSSAUPROTOIRBYHLISEFEYHEIEYIKANOCILN";
        //String deciphered = decryptColTransp(cipher,"CEBAD");
        System.out.print(decryptColTransp(candidate, "CEBAD"));

    }//END MAIN

    //Performs a Columnar Transposition decryption
    private static String decryptColTransp(String str, String key){
        int keyLength = key.length();
        int remainder = str.length() % keyLength;
        int flag = remainder;
        int amountOfRows = (int) Math.ceil((double) str.length() / key.length());
        int[] arrangedKeyArr = arrangeKey(key);//int array based on key
        char[][] grid = new char[amountOfRows][keyLength];
        String newStr = "";

        //finds each col & match it against the int from arrangedKeyArr
        for(int col = 0; col < keyLength; col++){
            for(int keyInt : arrangedKeyArr){
                if(keyInt == col){
                    //Account for message that is not padded
                    if(flag <= keyLength && flag != 0){
                        //This loop starts at the last columns since they will
                        // be the ones requiring padding if needed, if padding
                        // is needed, it won't be for more columns than the
                        // remainder count.
                        for(int p = keyLength - 1; p > remainder - 1; p--){
                            if(arrangedKeyArr[p] == col){
                                newStr = str.substring(0, (amountOfRows - 1));
                                newStr = newStr + "X";
                                flag++;
                                str = str.substring((amountOfRows - 1), str.length());
                            }
                        }
                    }
                    //No padding required for this col, take substring of row
                    // length to populate the column in grid
                    if(newStr == ""){
                        newStr = str.substring(0, amountOfRows);
                        str = str.substring(amountOfRows, str.length());
                    }
                    //Populate grid at the given column
                    for(int row = 0; row < amountOfRows; row++){
                        grid[row][col] = newStr.charAt(row);
                    }
                    newStr = "";
                }
            }
        }
        //Populate the message with the all of the characters
        // in order of the arrangedKey array since the keys now
        // correspond to the column indexes of the grid. Begins
        // with the first row at zero.
        String message = "";
        for(int r = 0; r < amountOfRows; r++){
            for(int keyInt : arrangedKeyArr){
                message = message + grid[r][keyInt];
            }
        }
        //System.out.println(message);
        return message;
    }

    //Arrange position of grid based on String key
    /*This method is direct from the following site:
    https://programmingcode4life.blogspot.com/
    2015/09/columnar-transposition-cipher.html*/
    private static int[] arrangeKey(String key){
        String[] keys = key.split("");
        Arrays.sort(keys);
        int[] num = new int[key.length()];
        for(int x = 0; x < keys.length; x++){
            for(int y = 0; y < key.length(); y++){
                if(keys[x].equals(key.charAt(y) + "")){
                    num[y] = x;
                    break;
                }
            }
        }
        return num;
    }

    //Segment a String based on dictionary words
    /*Modified method from: http://thenoisychannel.com
    /2011/08/08/retiring-a-great-interview-problem*/
    private static String segmentString(String newString, Dictionary dictionary){
        for(int i = 1; i < newString.length(); i++){
            String prefix = newString.substring(0, i);
            //If the dictionary file contains the word taken from index 0
            // to i (prefix) return it with a space and the remaining string
            // which will perform a recursive call on that remaining string
            if(dictionary.wordExists(prefix)){
                String suffix = newString.substring(i, newString.length());
                String segSuffix = segmentString(suffix, dictionary);

                if(segSuffix != null){
                    return prefix + " " + segSuffix;
                }else{
                    return suffix;
                }
            }
        }
        //if no word was able to be found, will return null
        // helpful when attempting to filter a final decrypted
        // message
        return null;
    }

    //Return string with specified shift to a cipher text
    private static String decryptSubstitution(int shift, String cipherStr){
        String plainText = "";

        //Locates where each char in cipher str is on the alphabet
        for(char ch : cipherStr.toCharArray()){
            int alphaIn = ALPHABET.indexOf(ch);
            //If the shift count requires to wrap around the alphabet
            // else get the char located on the alphabet after shifting
            if((alphaIn - shift) < 0){
                int shiftRmdr = Math.abs(alphaIn - shift) % 25;
                plainText += ALPHABET.charAt(26 - shiftRmdr);
            }else{
                plainText += ALPHABET.charAt(alphaIn - shift);
            }
        }
        return plainText;
    }

    //Map frequency of each char in cipherStr
    private static Map mostFrequent(String cipherStr){
        Map freqMap = new HashMap();
        int count;
        //Iterate through each char in the alphabet
        for(char alphaChar = 'A'; alphaChar <= 'Z'; alphaChar++){
            count = 0;
            //Iterate through the cipher str
            for(int i = 0; i <= cipherStr.length() - 1; i++){
                //count every time the alphabet char
                // is seen in the cipher str
                if(alphaChar == cipherStr.charAt(i)){
                    count++;
                }
            }
            //If that char is seen more than three times so that
            // it is one of the most frequent then add that character
            // as the key so that there are no repeated characters
            // and the count as the value
            if(count > 3){
                freqMap.put(alphaChar, count);
            }
        }
        return freqMap;
    }

    //return list from most freq chars to least frequent key/val map
    private static ArrayList frequencySort(Map freqMap){
        List keyList = new ArrayList<Character>();
        //Get the max int value from the map
        int maxVal = (int) Collections.max(freqMap.values());
        //Start with the max value
        for(int max = maxVal; max >= 0; max--){
            for(Object entry : freqMap.keySet()){
                //if the key matches its value in the map - currently
                // the max then take that key (char) in the front
                // of the list
                if(freqMap.get(entry).equals(max)){
                    keyList.add(entry);
                }
            }
        }
        return (ArrayList) keyList;
    }

    //Returns set of ints to be shifted based on freq and common chars
    private static Set findShifts(List<Character> sortedList){
        Set<Integer> possibleShifts = new HashSet<>();
        char[] mostCommonArr = {'A', 'E', 'O', 'S', 'T'};
        char shiftedChar;
        //For each char in the list passed in, locate it's position
        // in the alphabet.
        for(Character sortedChar : sortedList){
            int alphaIndex = ALPHABET.indexOf(sortedChar);
            boolean done = false;
            int shift = 1;
            //subtract the current shift until finding a match to the
            // common letters in the English language - keep shift count
            while(!done){
                //Account for having to wrap around the alphabet.
                if((alphaIndex - shift) < 0){
                    int shiftRmdr = Math.abs(alphaIndex - shift) % 25;
                    shiftedChar = ALPHABET.charAt(26 - shiftRmdr);
                }else{
                    shiftedChar = ALPHABET.charAt(alphaIndex - shift);
                }
                //keep the shift count if shifted char matches most common
                for(char ch : mostCommonArr){
                    if(shiftedChar == ch){
                        possibleShifts.add(shift);
                        done = true;
                    }
                }
                shift++;
            }
        }
        return possibleShifts;
    }

    //Generate possible key permutations - Stack Overflow
    /*Methods directly from: http://stackoverflow.com/
    questions/4240080/generating-all-permutations-of-a-given-string */
    private static ArrayList<String> permutation(String s){
        // The result
        ArrayList<String> res = new ArrayList<String>();
        // If input string's length is 1, return {s}
        if(s.length() == 1){
            res.add(s);
        }else if(s.length() > 1){
            int lastIndex = s.length() - 1;
            // Find out the last character
            String last = s.substring(lastIndex);
            // Rest of the string
            String rest = s.substring(0, lastIndex);
            // Perform permutation on the rest string and
            // merge with the last character
            res = merge(permutation(rest), last);
        }
        return res;
    }
    //Used by permutation method which is also from - Stack Overflow
    /*Methods directly from: http://stackoverflow.com/
    questions/4240080/generating-all-permutations-of-a-given-string */
    private static ArrayList<String> merge(ArrayList<String> list, String c){
        ArrayList<String> res = new ArrayList<String>();
        // Loop through all the string in the list
        for(String s : list){
            // For each string, insert the last character to all possible positions
            // and add them to the new list
            for(int i = 0; i <= s.length(); ++i){
                String ps = new StringBuffer(s).insert(i, c).toString();
                res.add(ps);
            }
        }
        return res;
    }


}//END CIPHERTEXT CLASS
