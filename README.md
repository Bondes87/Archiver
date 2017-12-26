### Archiver ###

It's an archiver. He packages the file into an archive and unpacks the file from the archive. 
The program can create two types of archive: zip and bds. 
The BDS-archiver for packing and unpacking the file uses the [Huffman algorithm](https://en.wikipedia.org/wiki/Huffman_coding).

A set of commands for working with the program:
1. Creating an archive:

  * zip-archive - **zip archive "file name"**. (for example, *zip archive test.txt*)

* bds-archive - **bds archive "file name"**. (for example, *bds archive test.txt*)

2. Unpacking the file:

 * zip-archive - **zip unarchive "archive name"**. (for example, *zip archive test.txt.zip*)

 * bds-archive - **bds unarchive "archive name"**.(for example, *bds archive test.txt.zip*)

Example name:
* before packaging - *test.txt*
* after packaging - *test.txt.zip* (or *test.txt-bds*)
* after unpacking - *test_copy.txt*
