package inozytol.cryptFetcher;

import inozytol.fileDispatcher.FileFetcherDispatcherById;
import inozytol.fileDispatcher.FetcherDispatcherFactory;

import java.nio.file.Paths;

public class App{


    public static void main(String [] args){
	FileFetcherDispatcherById diskFileFetcher = FetcherDispatcherFactory.getDispatcher(Paths.get("."));

	System.out.println("works somewhat");
    }
}
