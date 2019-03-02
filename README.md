# cryptfetcher
Toy program - written for learning. Its sample purpose is to to encrypt and decrypt files

Uses classes from Cryptest and fileFetcher packages (available in other github archives)


## Assumptions and details of purpose

### Excersise in DECOUPLING
The program doesn't change regardless where fileFetcher object gets files from and where it writes them. Whole encryption is also hidden beneath interace, so encryption can also be implemented by other class implementing interface used.


### Excersise in importing homemade jars
Cryptest and fileFetcher packages are homemade

## Workflow and interface

### ENCRYPTION
1. I need to create a stream from file to encrypt
1. file is stored and i get file id, that can be used to retrieve this encrypted file from that storage or to write to database (important if id is not equivalent to filename)


### DECRYPTION
1. ask for id, password, and create
1. pass an id and temp dir to file fetcher
1. perform an decryption on file, write to destination

## Done
1. Proof of concept - fixed file encrypted and decrypted
1. Interactive text interface for basic encryption decryption

    
## Future work/TODO
### Codewise
1. Move to Maven (figure out how to move functionality from run.sh script)
1. Move interface from fileFetcher to this package
1. Separate text interface from logic/app workflow - make UI pluggable, not part of logic/app workflow