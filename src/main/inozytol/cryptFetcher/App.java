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

    private static FileFetcherDispatcherById fileFetcher;
    private static UI userInterface;


    private static State appState = State.INIT;


    private static void closeApp(int exitCode) {
        userInterface.closeInterface();
        System.exit(exitCode);
    }

    public static void main(String [] args){
        userInterface = new ConsoleTextUI();

    // If no arguments given - run in interactive mode
    // First select file store (enter path or something)
    // Then: you can retrieve file, store file,
    // list stored files and delete stored file or exit

    if(args.length==0) {
        String message = "No argument given. Running in interactive mode.";
        userInterface.conveyMessage(message);
        do {
            message = "Select store: (f)ile store, (e)xit: (f) ";
            userInterface.conveyMessage(message);
            message = userInterface.askForInput();
            if(message.equalsIgnoreCase("e")) {
                closeApp(0);
            }
        } while(!message.equals("f") && !message.equals(""));

        // in future versions this shouldn't be hard wired
        // this is case for diskFileFetcher,
        // but some way to service another file fetchers
        // depending on chosen option should be devised
        do {
            message = "Enter file store path: (./) ";

            userInterface.conveyMessage(message);
            message = userInterface.askForInput();

            if((message == null)
               || !Files.exists(Paths.get(message))
               || !Files.isDirectory(Paths.get(message))) {
                userInterface.conveyMessage("Path doesn't exist or is not a directory. ");
            }
        } while(!Files.isDirectory(Paths.get(message)));
        storageDirectory  = message;
        fileFetcher = FetcherDispatcherFactory
            .getDispatcher(Paths.get(storageDirectory));
        // end of disk file fetcher specific code


        while (true) {
            appState = selectStateInteractionLoop();
            handleAppState(appState);
        }
    }


    }

    private static void handleAppState(State appState) {
        if(appState == State.LIST) {
            userInterface.listFilesInStorage(storageDirectory);
        } else if(appState == State.RETREIVE) {
            String fileToRetrieveId =
                userInterface.askForInput("Enter id of the file to retrieve: ");
            retrieveFile(fileToRetrieveId);
        } else if(appState == State.STORE) {
            String fileToStoreId =
                userInterface.askForInput("Enter a name of file to store: ");
            Path fileToStorePath = Paths.get(fileToStoreId);
            storeFile(fileToStorePath);
        }
        else if(appState == State.EXIT) {
            closeApp(0);
        }
        else {
            userInterface.conveyMessage("operation not supported yet\n");
            closeApp(0);
        }
    }

    private static void storeFile(Path fileToStore) {
        char [] passwordArray = loopAskForPasswordAndCompare();

        userInterface.conveyMessage("Storing file");
        userInterface.conveyMessage("Creating temporary encrypted file");
        Path encryptedFile =
            Paths.get(storageDirectory, fileToStore.getFileName().toString());

        try (InputStream bis =
             new BufferedInputStream(new FileInputStream(fileToStore.toFile()));
             OutputStream bos =
             new BufferedOutputStream(new FileOutputStream(encryptedFile.toFile()))) {

            StreamCrypt sc = new Cryptest();
            sc.encryptDataStreamToStream(passwordArray, 5000, bis, bos);

            String storedFileId = fileFetcher.storeFile(encryptedFile);
            bis.close();
            bos.close();
            userInterface.conveyMessage("Id returned from file fetcher " + storedFileId);

        } catch (FileNotFoundException e) {
            // TODO: LOG
            userInterface.conveyMessage("For some reason some file was not found during decryption " + e);
        } catch (IOException e) {
            // TODO: LOG
            userInterface.conveyMessage("Exception occured during decryption " + e);
        }

    }

    private static void retrieveFile(String fileToRetrieveId) {
        char passwordArray[] = loopAskForPasswordAndCompare();

        userInterface.conveyMessage("Retreving file");

        // Creating temporary encrypted file as a compatibility layer
        // for file fetchers that would not allow to create streams
        // directly from them
        Path encryptedFile = null;
        try {
            encryptedFile = Files.createTempFile("","");
        } catch (IOException e) {
            //TODO log
            userInterface.conveyMessage("Failed to create temporary file for file retrieval");
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
            userInterface.conveyMessage("For some reason some file was not found during decryption " + e);
        } catch (IOException e) {
            // TODO: LOG
            userInterface.conveyMessage("Exception occured during decryption " + e);
        }
        
        userInterface.conveyMessage("Retrieved file " + retrievedFile +
                   " exists: " + Files.exists(retrievedFile));
        
    }

    
    private static char[] loopAskForPasswordAndCompare() {
        int attempts = 3;
        char passwordArray[] = null;
        char passwordArrayConfirm[] = null;
        do {
            passwordArray = userInterface.askForPassword("Enter your secret password: ");
            passwordArrayConfirm = userInterface.askForPassword("Enter your secret password again: ");

            if (!Arrays.equals(passwordArray, passwordArrayConfirm)) {
                userInterface.conveyMessage("Passwords don't match! Attempts left: " + --attempts);
                if(attempts == 0) {
                    userInterface.conveyMessage("Program terminated.");
                    //TODO : LOG INFO
                    closeApp(1);
                }
            }
        } while ( (!Arrays.equals(passwordArray, passwordArrayConfirm)) );
        
        Arrays.fill(passwordArrayConfirm, 'x');
        return passwordArray;
    }

    private static State selectStateInteractionLoop() {
        String message;
        State ret = State.INIT;
        while(ret == State.INIT) {
        message = "You can (r)etrieve file, (s)tore file, (l)ist stored files and (d)elete stored file or (e)xit: (l) ";
        userInterface.conveyMessage(message);
        message = userInterface.askForInput();
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


class AppLogic {
    

}


enum State {
    INIT, RETREIVE, STORE, DELETE, LIST, EXIT
}


// TODO: Create interface UI to separate logic from reprezentation?
/*
Interface will have a state probably
Interface will probably have to register main app as listener of events
Events such as closing app. Maybe it should just close app?
 */
interface UI {
    public void conveyMessage(String message);

    public char[] askForPassword(String prompt);

    public String askForInput();

    public String askForInput(String prompt);

    public void closeInterface();

    public void listFilesInStorage(String storageDir);
}

class ConsoleTextUI implements UI {

    private static Consumer<String> mprinter;
    private static Function<String,char []> pprompt;
    private static Supplier<String> inputPrompt;
    private static Scanner inScanner; // needed for reading from standard input
    
    
    
    
    public ConsoleTextUI() {


        //If console can't be obtained
        //use alternative (System.in and System.out)

        Console console = System.console();
        if (console != null) {
            mprinter = (s) -> console.printf(s + "\n");
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
    }

    public void conveyMessage(String message) {
        mprinter.accept(message);
    }

    public char[] askForPassword(String prompt) {
        return pprompt.apply(prompt);
    }

    public String askForInput() {
        return inputPrompt.get();
    }

    public String askForInput(String prompt) {
        conveyMessage(prompt);
        return inputPrompt.get();
    }

    public void closeInterface() {
        if(inScanner!=null) inScanner.close();
    }

    public void listFilesInStorage(String storageDirectory2) {
        // TODO: decide whether to pass storage directory as argument or use static class variable
        FileFetcherDispatcherById diskFileFetcher = FetcherDispatcherFactory.getDispatcher(Paths.get(storageDirectory2));
        String [] files = diskFileFetcher.fileList();
        for (String file : files) {
            conveyMessage(file);
        }
    }



}
