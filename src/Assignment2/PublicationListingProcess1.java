// -------------------------------------------------------
// Assignment 2
// Written by: Gabriel Harel - 40006459
// For COMP 249-UJ-X – Winter 2016
// --------------------------------------------------------

//this class is the first driver file of the assihnment
//it will read a .txt file from a directory and make sure that there are no copy publication codes
//if some duplicates are found, it will ask the user to chamge them
//when all the codes are unique, it will write the corrected file to a user specified .txt file in the same directory
//finally, it prints out both files to the console

package Assignment2;

import java.util.*;
import java.io.*;
/**
 *
 * @author Gabriel
 */
public class PublicationListingProcess1 {
    
    private final static Scanner userInput = new Scanner(System.in);
    private static final String READFILENAME = "PublicationData_Input.txt";
    private static final String READPATH = "src/Assignment2/test_files/";
    
    private enum PublicationTypes{
        PUBLICATIONCODE,
        PUBLICATIONNAME,
        PUBLICATIONYEAR,
        PUBLICATIONAUTHORNAME,
        PUBLICATIONCOST,
        PUBLICATIONNBPAGES
    }
    
    public static void main(String[] args) {
        System.out.println("Welcome!");
        //asking user for output file name
        File fileToWrite;
        System.out.print("Specify the name of the output file > ");
        //making sure the file doesn't already exist
        while(true) {
            fileToWrite = new File(READPATH + userInput.next() + ".txt");
            userInput.nextLine();
            if(fileToWrite.exists()){
                System.out.print("That file already exists (" + fileToWrite.length() + "bytes) enter another name > ");
            }
            else {
                break;
            }        
        }
        //reading the file, changing duplicate codes and printing both new and old files using some helper methods
        try {
            fileToWrite.createNewFile();
            //case when the number of lines in the file is too small
            if(getLines(READPATH + READFILENAME) <= 1) {
                System.out.println("File too small to have duplicates");
            }
            //otherwise
            else {
                //method called to verify that there are no duplicates and asks the user to change them if there are any
                //also creates a new file with the corrected set of publications
                correctListOfItems(READPATH + READFILENAME, READPATH + fileToWrite.getName());
                //printing out the old and new files
                System.out.println("\nOld file :");
                printFileItems(READPATH + READFILENAME);
                System.out.println("\nNew file :");
                printFileItems(READPATH + fileToWrite.getName());
            }
        } catch (FileNotFoundException ex) {
            //error if one of the files is not found
            System.out.println("The file is missing, please place it in \"" + READPATH + "\" and restart the program." + Arrays.toString(ex.getStackTrace()));
            System.exit(0);
        } catch (IOException ex) {
            //error for any other IO related exception
            System.out.println("Something went wrong with the file!");
            System.out.println(ex.getMessage());
        }
        System.out.println("All done!");
    }
    
    /**
     * checks that there are no duplicated publication codes in an array of Publication objects
     * 
     * @param inputName
     * @param outputName
     * @throws IOException
     */
    public static void correctListOfItems(String inputName, String outputName) throws IOException {
        System.out.println();
        //creating an array out of the file to be read
        Publication[] publicationArray = arrayMaker(inputName);
        //looking for repeated publication codes
        while(true) {
            try {
                for(int i = 0 ; i < publicationArray.length ; i++) {
                    for(int j = i + 1 ; j < publicationArray.length ; j++) {
                        if(publicationArray[i].getPublicationCode() == publicationArray[j].getPublicationCode()) {
                            throw new CopyCodeException("Publication code no." + (j + 1) + " is the same as publication code no." + (i + 1), j);
                        }
                    }
                }
                //will only break if it has gone through the whole array without finding duplicates
                //break is skipped if a duplicate was found and the loop will look for duplicates again (with the new value from the user)
                break;
            } catch (CopyCodeException ex) {
                    //error if two codes are the same
                    System.out.print(", enter the new code > ");
                    publicationArray[ex.getToChange()].setPublicationCode(userInput.next());
                    userInput.nextLine();
            }
        }
        //outputting to new file
        try(BufferedWriter output = new BufferedWriter(new FileWriter(outputName, true))) {
            for (Publication i : publicationArray) {
                output.write(i.toString() + "\n");
            }
        }
    }
    
    /**
     * prints the contents of a file to the console
     * 
     * @param outputFile
     * @throws FileNotFoundException, IOException
     */
    private static void printFileItems(String inputName) throws FileNotFoundException, IOException {
        //calls the method of the same name in the other listing process class
        PublicationListingProcess2.printFileItems(inputName);
    }
    
    /**
     * returns the number of non-empty lines found in the inputted filename in the READPATH directory
     * 
     * @param name
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private static int getLines(String inputName) throws FileNotFoundException, IOException {
        try(BufferedReader reader = new BufferedReader(new FileReader(inputName))) {
            int nbPublications = 0;
            String line;
            while(true) {
                line = reader.readLine();
                //increment if line exists and is not empty
                if(line != null && !line.equals("")) {
                    nbPublications++;
                    continue;
                }
                break;
            }
            return nbPublications;
        }
    }
    
    /**
     * makes an array of Publications from the inputted filename in the READPATH directory
     * 
     * @param inputName
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static Publication[] arrayMaker(String inputName) throws FileNotFoundException, IOException{
        try(BufferedReader reader = new BufferedReader(new FileReader(inputName))) {
            //creating the array of the right lenght
            Publication[] array = new Publication[getLines(inputName)];
            String[] fileContent;
            //reading the file line by line and parsing each entry
            for(int i = 0; i < array.length; i++) {
                fileContent = split(reader.readLine()); 
                try {
                    array[i] = new Publication(Long.parseLong(fileContent[0]), fileContent[1], Integer.parseInt(fileContent[2]), fileContent[3], 
                                                Double.parseDouble(fileContent[4]), Integer.parseInt(fileContent[5]));
                } catch (NumberFormatException ex) {
                    //error if one of the entries is of the wrong format
                    array[i] = new Publication();
                    System.out.println("The format of publication no." + (i + 1) + " is incorrect and has been skipped");
                }
            }
            return array;
        }
        
    }
    
    /**
     * splits inputted strings every whitespace and returns an array of the substrings
     * 
     * @param toSplit
     * @return 
     */
    public static String[] split(String toSplit) {
        //making all whitespace characters simply spaces
        String allSpace = toSplit.trim().replace('\t', ' ').replace('\n', ' ');
        //removing duplicate spaces
        String temp = "";
        for(int i = 0 ; i < allSpace.length() ; i++) {
            if(!(i > 0 && allSpace.charAt(i) == ' ' && allSpace.charAt(i-1) == ' ')) {
                temp += allSpace.charAt(i);
            }
        }
        return temp.split(" ");
    }
}