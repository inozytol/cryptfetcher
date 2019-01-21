package inozytol.cryptFetcher;

import inozytol.fileDispatcher.FileFetcherDispatcherById;
import inozytol.fileDispatcher.FetcherDispatcherFactory;

import inozytol.dataencryption.Cryptest;
import inozytol.dataencryption.StreamCrypt;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

import java.io.Console;

import java.util.Arrays;
import java.util.Scanner;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

/** 
 * Class for testing fileFetcher and Cryptest usage in file encryption app
 */

public class App{

    static Consumer<String> mprinter;
    static Function<String,char []> pprompt;
    static Supplier<String> inputPrompt;
    static Scanner inScanner; // needed for reading input from standard input

    private static void printMessage(String message) {
	mprinter.accept(message);
    }

    private static char[] askForPassword(String prompt) {
	return pprompt.apply(prompt);
    }

    private static String askForInput() {
	return inputPrompt.get();
    }

    private static void closeApp(int exitCode) {
	if(inScanner!=null) inScanner.close();
	System.exit(exitCode);
    }
    
    public static void main(String [] args){


	//If console can't be obtained use alternative (System.in and System.out)
	Console console = System.console();
        if (console != null) {
	    mprinter = (s) -> console.printf(s);
	    pprompt = (s) -> {return console.readPassword(s);};
	    inputPrompt = () -> {return console.readLine();};

        } else {
	    // TODO: LOG
	    inScanner = new Scanner(System.in);
	    mprinter = (s) -> System.out.println(s);
	    pprompt = (S) -> {return inScanner.nextLine().toCharArray();};
	    inputPrompt = () -> {return inScanner.nextLine();};
	}

	// If no arguments given - run in interactive mode
	// First select file store (enter path or something)
	// Then: you can retrieve file, store file, list stored files and delete stored file or exit
	//

	if(args.length==0) {
	    String message = "No argument given. Running in interactive mode.";
	    printMessage(message);
	    do {
		message = "Select store: File store - f (f)";
		printMessage(message);
		message = askForInput();
	    } while(!message.equals("f") && !message.equals(""));
	    do {
		message = "Enter file store path: ";
		printMessage(message);
		message = askForInput();
		if(!Files.isDirectory(Paths.get(message))) {
		    printMessage("Path doesn't exist or is not a directory. ");
		} 
	    } while(!Files.isDirectory(Paths.get(message)));
	}
	

	Path fileToStore = Paths.get(args[0]);
	if(!Files.exists(fileToStore)){
	    printMessage("Unfortunately the file you want to store does not exist.");
	    closeApp(1);
	}

	if(!Files.isRegularFile(fileToStore)){
	    printMessage("Unfortunately the file you want to store is in fact, not a file.");
	    closeApp(1);
	}

	
	if(!Files.isDirectory(Paths.get(args[1]))){
	    printMessage("Storage path doesn't exist or is not a directory.");
	    closeApp(1);
	}


	FileFetcherDispatcherById diskFileFetcher = FetcherDispatcherFactory.getDispatcher(Paths.get(args[1]));
	
        char passwordArray[] = askForPassword("Enter your secret password: ");
        char passwordArrayConfirm[] = askForPassword("Enter your secret password again: ");

	if (!Arrays.equals(passwordArray, passwordArrayConfirm)) {
	    printMessage("Password doesn't match!");
	    //TODO : LOG INFO
	    closeApp(1);
	}

	
	printMessage("Using password to encrypt file: " + fileToStore);

	Path tempOutputFile = Paths.get(fileToStore.getParent()==null?".":fileToStore.getParent().toString(), fileToStore.getFileName().toString() + ".inocrypt");
	
	try (InputStream bis = new BufferedInputStream(new FileInputStream(fileToStore.toFile()));
	     OutputStream bos = new BufferedOutputStream(new FileOutputStream(tempOutputFile.toFile()))) {
	
	StreamCrypt sc = new Cryptest();
	sc.encryptDataStreamToStream​(passwordArray,
                                     5000,
                                     bis,
                                     bos);

	} catch (FileNotFoundException e) {
	    // TODO: LOG
	    printMessage("For some reason some file was not found during encryption " + e);
	} catch (IOException e) {
	    // TODO: LOG
	    printMessage("Exception occured during encryption " + e);
	}

	// file store function removes the original file ...
	String storedFileId = diskFileFetcher.storeFile(tempOutputFile);
	printMessage("Stored file " + storedFileId);

	try {
	    Files.delete(tempOutputFile);
	} catch (IOException e) {
	    printMessage("There has been an error: " + e);
	}

	for(String s : diskFileFetcher.fileList()){
	    printMessage("File in storage: " + s + "\n");
	}

	System.out.println("Stored file " + tempOutputFile +
			   " deleted from local: " +
			   !(Files.exists(tempOutputFile)));

	System.out.println("Retreving file");
	Path retrievedFile = diskFileFetcher.getFile(storedFileId);
	System.out.println("Retrieved file " + retrievedFile +
			   " exists: " + Files.exists(retrievedFile));

	System.out.println("Cleaning up local files");
	try {
	    Files.delete(tempOutputFile);
	} catch (IOException e) {
	    printMessage("There has been an error: " + e);
	}

    }
}
