import java.util.ArrayList;
import java.io.IOException;
import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Eric on 10/2/2016.
 */
public class Dictionary{
    private ArrayList<String> dictionary;

    public Dictionary() throws IOException{

        dictionary = new ArrayList<>();
        //System.out.println("in constructor");
        readInDictionaryWords();
    }

    private void readInDictionaryWords() throws FileNotFoundException{
        //File dictionaryFile = new File("C:\\Users\\Eric\\Documents\\CalStateNorthridge\\Comp424_CompSecurity\\IntelliJ Projects\\DictionaryFile.txt");
        File dictionaryFile = new File("DictionaryFile.txt");
        if(!dictionaryFile.exists()) {
            System.out.println("*** Error *** \n" +
                    "Your dictionary file has the wrong name or is " +
                    "in the wrong directory.  \n" +
                    "Aborting program...\n\n");
            System.exit( -1);    // Terminate the program
        }

        Scanner inputFile = new Scanner(dictionaryFile);
        while(inputFile.hasNext()) {
            dictionary.add( inputFile.nextLine().toUpperCase() );
        }
        //System.out.println(dictionary);
    }
    //System.out.println(dictionary);

    public boolean wordExists(String wordToLookup){
       // System.out.println(dictionary);
        if( dictionary.contains(wordToLookup)) {
            return true;    // words was found in dictionary
        }else {
            return false;   // word was not found in dictionary
        }
    }//end wordExist
}
