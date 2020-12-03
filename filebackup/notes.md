# Program 3 notes

Some notes to organize my thoughts and keep records.

When uploading, any artifacts are deleted. When downloading, this is not done.
## Questions

What to do when the given bucket name doesn't exist? Should I make a bucket or print an error message?

For now, I'm thinking of printing an error message. I may improve the program later to make a bucket.

consider:
- directory doesn't exist (upload/download)
- directory is empty (upload/download)
- directory contains files (upload/download) 
- directory contains folders (upload/download)
- directory contains files and folders (upload/download)
- should I upload an empty directory?
- How to know if two files are the same?
- How to know if when a file was uploaded and when it was modified locally and compare them?
- backslashes and forward slashes in filename.
- Can I expect the grader to have both boto3 installed and configured?
- Figure out the region problem. Maybe use boto3 sessions?
- If the called directory doesn't exist.
- If the called directory is actually a file.
- What if two folders have the same files.
- Test with inputs that trail with /
- ampersand


## Reminders

Handle all exceptions.


## Resources

https://boto3.amazonaws.com/v1/documentation/api/latest/index.html

https://mkyong.com/java/how-to-get-the-file-last-modified-date-in-java/

https://stackoverflow.com/questions/4871051/how-to-get-the-current-working-directory-in-java

https://pypi.org/project/boto3/

https://www.geeksforgeeks.org/with-statement-in-python/

https://boto3.amazonaws.com/v1/documentation/api/latest/guide/paginators.html

https://boto3.amazonaws.com/v1/documentation/api/latest/guide/migrations3.html

https://realpython.com/working-with-files-in-python/