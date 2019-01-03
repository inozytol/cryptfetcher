package inozytol.cryptFetcher;

import inozytol.fileDispatcher.FileFetcherDispatcherById;
import inozytol.fileDispatcher.FetcherDispatcherFactory;

import inozytol.dataencryption.Cryptest;
import inozytol.dataencryption.StreamCrypt;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.File;

import java.io.Console;

import java.util.Arrays;


/** 
 * Class for testing fileFetcher and Cryptest usage in file encryption app
 */
public class App{


    public static void main(String [] args){
	FileFetcherDispatcherById diskFileFetcher = FetcherDispatcherFactory.getDispatcher(Paths.get("."));

	System.out.println("works somewhat");


	Console console = System.console();
        if (console == null) {
	    // TODO: LOG FATAL
            System.out.println("Couldn't get Console instance");
            System.exit(1);
        }
	if(args.length!=2) {
	    console.printf("Well, you should give two arguments to this app: one - file to store; second - storage path");
	    System.exit(1);
	}
	
        console.printf("Testing password input %n");
	
        char passwordArray[] = console.readPassword("Enter your secret password: ");
        char passwordArrayConfirm[] = console.readPassword("Enter your secret password again: ");

	if (!Arrays.equals(passwordArray, passwordArrayConfirm)) {
	    console.printf("Password doesn't match!");
	    //TODO : LOG INFO
	    System.exit(1);
	}
	    

	console.printf("Using password to encrypt file: target/foo");


	// Path.toFile
	
	
	Path fileToStore = Paths.get(args[0]);
	if(!fileToStore.toFile().exists()){
	    console.printf("Unfortunately the file you want to store does not exist.");
	    System.exit(1);
	}

	if(!fileToStore.toFile().isFile()){
	    console.printf("Unfortunately the file you want to store is in fact, not a file.");
	    System.exit(1);
	}
	
	// create output path, write to it from Cryptest,
	// give this path to fileFetcher to store
	// remove temporary file

	// get file names from runtime arguments
	// arg1 - input file
	// arg2 - output folder

	// output file inputFileName.cryptfetcher

	// input file must exist
	// output folder must exist

	//
	/*
	try () {
	    Cryptest.encryptDataStreamToStream(passwordArray,
					       5000,
					       fileToEncryptStream,
					       outputFileStream);
	} catch (Exception e) {
	    // TODO: LOG
	    System.err.println("Error occured in App during encryption" + e);
	}
	*/				       
    }
}
