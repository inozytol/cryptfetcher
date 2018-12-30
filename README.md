# cryptfetcher

Sample app to use classes from Cryptest and fileFetcher

Its sample purpose is to to encrypt and decrypt files

## DECOUPLING, so I don't care whre fileFetcher gets this file it writes to temp for decryption or where it stores that file
	
## ENCRYPTION
1. I need to create a stream from file to encrypt
1. It would be beneficial to create some temporary directory that would contain files after encryption and files before decryption?
1. I have a file here, want do encrypt it
1. First I point a program to this file, and encrypt it using password and put it into temp
 then I invoke file fetcher on this file, and remove temp with all contents
1. file is stored and i get file id, that can be used to retrieve this encrypted file from that storage



## DECRYPTION
1. ask for id, password, and create temp dir
1. pass an id and temp dir to file fetcher
1. perform an decryption on file, write to destination
1. remove temp dir