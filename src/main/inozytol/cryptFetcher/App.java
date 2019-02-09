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

    private static String storageDirectory;
    private static Consumer<String> mprinter;
    private static Function<String,char []> pprompt;
    private static Supplier<String> inputPrompt;
    private static Scanner inScanner; // needed for reading input from standard input
    private static FileFetcherDispatcherById fileFetcher;


    private static void printMessage(String message) {
        mprinter.accept(message);
    }

    private static char[] askForPassword(String prompt) {
        return pprompt.apply(prompt);
    }

    private static String askForInput() {
        return inputPrompt.get();
    }
    
    private static String askForInput(String prompt) {
        printMessage(prompt);
        return inputPrompt.get();
    }

    private static void closeApp(int exitCode) {
        if(inScanner!=null) inScanner.close();
        System.exit(exitCode);
    }
    
    public static void main(String [] args){
        State appState = State.INIT;

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
	    pprompt = (s) -> {
	        mprinter.accept(s);
	        return inScanner.nextLine().toCharArray();
	    };
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
		message = "Select store: (f)ile store: (f) ";
		printMessage(message);
		message = askForInput();
	    } while(!message.equals("f") && !message.equals(""));
	    
	    // in future versions this shouldn't be hard wired
	    // this is case for diskFileFetcher, but some way to service another file fetchers 
	    // depending on chosen option should be devised
	    do {
			message = "Enter file store path: ";
			
			printMessage(message);
			message = askForInput();
			if(!Files.isDirectory(Paths.get(message))) {
			    printMessage("Path doesn't exist or is not a directory. ");
			} 
	    } while(!Files.isDirectory(Paths.get(message)));
	    storageDirectory  = message;
	    fileFetcher = FetcherDispatcherFactory.getDispatcher(Paths.get(storageDirectory));
	    // end of disk file fetcher specific code
	    
	    
	    while (true) {
    	    appState = selectStateInteractionLoop(); 
    	    handleAppState(appState);
	    }
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

    char passwordArray[] = askForPassword("Enter your secret password: ");
    char passwordArrayConfirm[] = askForPassword("Enter your secret password again: ");

	if (!Arrays.equals(passwordArray, passwordArrayConfirm)) {
	    printMessage("Password doesn't match!");
	    //TODO : LOG INFO
	    closeApp(1);
	}

	   FileFetcherDispatcherById diskFileFetcher = FetcherDispatcherFactory.getDispatcher(Paths.get(args[1]));
	
	printMessage("Using password to encrypt file: " + fileToStore);

	Path tempOutputFile = Paths.get(fileToStore.getParent()==null?".":fileToStore.getParent().toString(), fileToStore.getFileName().toString() + ".inocrypt");
	
	try (InputStream bis = new BufferedInputStream(new FileInputStream(fileToStore.toFile()));
	     OutputStream bos = new BufferedOutputStream(new FileOutputStream(tempOutputFile.toFile()))) {
	
	StreamCrypt sc = new Cryptest();
	sc.encryptDataStreamToStream(passwordArray, 5000, bis, bos);

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

	System.out.println("Cleaning up local files");
	try {
	    Files.delete(tempOutputFile);
	} catch (IOException e) {
	    printMessage("There has been an error: " + e);
	}

    }

    private static void handleAppState(State appState) {
        if(appState == State.LIST) {
            listFiles(storageDirectory);
        } else if(appState == State.RETREIVE) {
            String fileToRetrieveId = askForInput("Enter id of the file to retrieve: ");
            retrieveFile(fileToRetrieveId);
        } else if(appState == State.STORE) {
            String fileToStoreId = askForInput("Enter a name of file to store: ");
            Path fileToStorePath = Paths.get(fileToStoreId);
            storeFile(fileToStorePath);
        }
        else if(appState == State.EXIT) {
            closeApp(0);
        }
        else {
            printMessage("operation not supported yet\n");
            closeApp(0);
        }
    }
    
    private static void storeFile(Path fileToStore) {
        char passwordArray[] = askForPassword("Enter your secret password: ");
        char passwordArrayConfirm[] = askForPassword("Enter your secret password again: ");

        if (!Arrays.equals(passwordArray, passwordArrayConfirm)) {
            printMessage("Password doesn't match!");
            //TODO : LOG INFO
            closeApp(1);
        }
        
        printMessage("Storing file"); 
        printMessage("Creating temporary encrypted file");
        Path encryptedFile = Paths.get(storageDirectory, fileToStore.getFileName().toString(),".inocrypt");
        
        try (InputStream bis = new BufferedInputStream(new FileInputStream(fileToStore.toFile()));
             OutputStream bos = new BufferedOutputStream(new FileOutputStream(encryptedFile.toFile()))) {
        
            StreamCrypt sc = new Cryptest();
            sc.encryptDataStreamToStream(passwordArray, 5000, bis, bos);
        
            String storedFileId = fileFetcher.storeFile(encryptedFile);
            bis.close();
            bos.close();
            printMessage("Id returned from file fetcher " + storedFileId);
            
            /*printMessage("Deleting temporary encrypted file");
            if(Files.exists(encryptedFile)){
                Files.delete(encryptedFile);
            }*/

        } catch (FileNotFoundException e) {
            // TODO: LOG
            printMessage("For some reason some file was not found during decryption " + e);
        } catch (IOException e) {
            // TODO: LOG
            printMessage("Exception occured during decryption " + e);
        } 
       
    }

    private static void retrieveFile(String fileToRetrieveId) {
        char passwordArray[] = askForPassword("Enter your secret password: ");
        char passwordArrayConfirm[] = askForPassword("Enter your secret password again: ");

        if (!Arrays.equals(passwordArray, passwordArrayConfirm)) {
            printMessage("Password doesn't match!");
            //TODO : LOG INFO
            closeApp(1);
        }
        
        
        
        printMessage("Retreving file");
        
        // Creating temporary encrypted file as a compatibility layer
        // for file fetchers that would not allow to create streams 
        // directly from them
        Path encryptedFile = null;
        try {
            encryptedFile = Files.createTempFile("","");
        } catch (IOException e) {
            //TODO log
            printMessage("Failed to create temporary file for file retrieval");
            closeApp(1);
        }
        
        Path retrievedFile = fileFetcher.getFile(fileToRetrieveId, encryptedFile);
        // TODO fileFetcher should be modified to allow writing to given path
        
        Path outputFile = Paths.get(fileToRetrieveId);
        
        try (InputStream bis = new BufferedInputStream(new FileInputStream(retrievedFile.toFile()));
             OutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile.toFile()))) {
            
            StreamCrypt sc = new Cryptest();
            sc.decryptDataStreamToStream(passwordArray, bis, bos);

        } catch (FileNotFoundException e) {
            // TODO: LOG
            printMessage("For some reason some file was not found during decryption " + e);
        } catch (IOException e) {
            // TODO: LOG
            printMessage("Exception occured during decryption " + e);
        }
        
        System.out.println("Retrieved file " + retrievedFile +
                   " exists: " + Files.exists(retrievedFile));
        
    }

    private static void listFiles(String storageDirectory2) {
        // TODO: decide whether to pass storage directory as argument or use static class variable
        FileFetcherDispatcherById diskFileFetcher = FetcherDispatcherFactory.getDispatcher(Paths.get(storageDirectory2));
        String [] files = diskFileFetcher.fileList();
        for (String file : files) {
            System.out.println(file);
        }
    }

    private static State selectStateInteractionLoop() {
        String message;
        State ret = State.INIT;
        while(ret == State.INIT) {
        message = "You can (r)etrieve file, (s)tore file, (l)ist stored files and (d)elete stored file or (e)xit: (l) ";
        printMessage(message);
        message = askForInput();
            switch(message) {
                //    INIT, RETREIVE, STORE, DELETE, LIST, EXIT
            case "r": ret = State.RETREIVE; break;
            case "s": ret = State.STORE; break;
            case "" :
            case "l": ret = State.LIST; break;
            case "d": ret = State.DELETE; break;
            case "e": ret = State.EXIT; break;
            default: break;
            }
        }
        return ret;
        
    }
}

enum State {
    INIT, RETREIVE, STORE, DELETE, LIST, EXIT
}
