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

    static MessagePrinter mprinter;
    static PasswordPrompt pprompt;
    static Scanner inScanner;

    public static void main(String [] args){
	
	Console console = System.console();
        if (console != null) {
	    mprinter = (s) -> console.printf(s);
	    pprompt = (s) -> {return console.readPassword(s);};

        } else {
	    // TODO: LOG
	    inScanner = new Scanner(System.in);
	    mprinter = (s) -> System.out.println(s);
	    pprompt = (S) -> {return inScanner.nextLine().toCharArray();};
	}
	if(args.length!=2) {
	    mprinter.printMessage("Well, you should give two arguments to this app: one - file to store; second - storage path");
	    System.exit(1);
	}

	Path fileToStore = Paths.get(args[0]);
	if(!Files.exists(fileToStore)){
	    mprinter.printMessage("Unfortunately the file you want to store does not exist.");
	    System.exit(1);
	}

	if(!Files.isRegularFile(fileToStore)){
	    mprinter.printMessage("Unfortunately the file you want to store is in fact, not a file.");
	    System.exit(1);
	}

	
	if(!Files.isDirectory(Paths.get(args[1]))){
	    mprinter.printMessage("Storage path doesn't exist or is not a directory.");
	    System.exit(1);
	}


	FileFetcherDispatcherById diskFileFetcher = FetcherDispatcherFactory.getDispatcher(Paths.get(args[1]));

	
        mprinter.printMessage("Testing password input %n");
	
        char passwordArray[] = pprompt.readPassword("Enter your secret password: ");
        char passwordArrayConfirm[] = pprompt.readPassword("Enter your secret password again: ");

	if (!Arrays.equals(passwordArray, passwordArrayConfirm)) {
	    mprinter.printMessage("Password doesn't match!");
	    //TODO : LOG INFO
	    System.exit(1);
	}
	    

	mprinter.printMessage("Using password to encrypt file: target/foo");

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
	    mprinter.printMessage("For some reason some file was not found during encryption " + e);
	} catch (IOException e) {
	    // TODO: LOG
	    mprinter.printMessage("Exception occured during encryption " + e);
	}

	// file store function removes the original file ...
	String storedFileId = diskFileFetcher.storeFile(tempOutputFile);
	mprinter.printMessage("Stored file " + storedFileId);

	try {
	    Files.delete(tempOutputFile);
	} catch (IOException e) {
	    mprinter.printMessage("There has been an error: " + e);
	}

	for(String s : diskFileFetcher.fileList()){
	    mprinter.printMessage("File in storage: " + s + "\n");
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
	    mprinter.printMessage("There has been an error: " + e);
	}

    }
}


interface MessagePrinter{
    void printMessage(String message);
}

interface PasswordPrompt{
    char [] readPassword(String passwordPrompt);
}
