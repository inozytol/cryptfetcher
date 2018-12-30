package inozytol.cryptFetcher;

import inozytol.fileDispatcher.FileFetcherDispatcherById;
import inozytol.fileDispatcher.FetcherDispatcherFactory;

import inozytol.dataencryption.Cryptest;

import java.nio.file.Paths;

import java.io.Console;

import java.util.Arrays;

public class App{


    public static void main(String [] args){
	FileFetcherDispatcherById diskFileFetcher = FetcherDispatcherFactory.getDispatcher(Paths.get("."));

	System.out.println("works somewhat");


	Console console = System.console();
        if (console == null) {
	    // TODO: LOG
            System.out.println("Couldn't get Console instance");
            System.exit(0);
        }

        console.printf("Testing password input %n");
        char passwordArray[] = console.readPassword("Enter your secret password: ");
        console.printf("Password entered was: %s%n", new String(passwordArray));

	console.printf("Using password to encrypt file: target/foo");


    }
}
