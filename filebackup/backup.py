import boto3
import os
import sys

# A backup client to upload and download directories from aws.
# requires boto3 and that aws is configured before use.
# Author: Youssef Beltagy
# Date: 11/14/2020 implemented for CSS436 of AUT2020


class Backup:
    """
    The backup class. Abstracts and hides the backup logic.
    """

    def __init__(self, my_s3):
        """the backup needs an s3 resource.

        Args:
            my_s3 (aws s3 resource): the s3 resource to upload to
        """
        self.my_s3 = my_s3
        self.bucket = None

    def __str__(self):
        """
        returns a string representation of Backup

        Returns:
            string: a string representation of this Backup instance
        """
        if(self.bucket == None):
            return "Current Bucket: " + "None"

        return "Current Bucket: " + self.bucket.name + "\n\tCreated on: " + str(self.bucket.creation_date)

    def does_bucket_exist(self, bucket_name):
        """
        returns true if the bucket exists in my_s3.
        false otherwise

        Args:
            bucket_name (string): the name of the bucket you are looking for.

        Returns:
            bool: true if the bucket_name exists in my_s3
        """
        for bucket in self.my_s3.buckets.all():
            if(bucket_name == bucket.name):
                return True
        
        return False

    def print_bucket(self, cur_bucket = None):
        """
        prints the bucket and the objects in it. If the bucket is not given,
        prints the bucket in the object.

        Args:
            cur_bucket (aws bucket object): the bucket to print.
        """

        if(cur_bucket == None):
            cur_bucket = self.bucket

        if(cur_bucket == None):
            print("None")
            return

        print(cur_bucket.name)
        print(cur_bucket.creation_date)
        for obj in cur_bucket.objects.all():
            print("\t uploaded at: " + str(obj.last_modified) + " key: " + obj.key)
        
    def print_all_buckets(self):
        """
        Prints all buckets in my_s3
        """
        print("Printing All Buckets--------")
        for bucket in self.my_s3.buckets.all():
            self.print_bucket(bucket)
            print()
            print()

    def empty_bucket(self, bucket = None):
        """
        Deletes everything in the bucket.

        Args:
            bucket (aws bucket resource, None): the bucket to empty, defaults to the current bucket
        """
        if (bucket == None):
            bucket = self.bucket

        if (bucket == None):
            return

        for obj in bucket.objects.all():
            obj.delete()

    def create_bucket(self, bucket_name):
        """
        Creates a bucket.

        Args:
            bucket_name (string): the name of the bucket

        Returns:
            aws bucket resource: the created bucket or None
        """
        try:
            self.my_s3.Bucket(bucket_name).create()
            return self.my_s3.Bucket(bucket_name)
        except Exception as e:
            print("Error creating bucket")
            print(e) # error log is useful for the user.
            return None

    def set_bucket(self, bucket_name):
        """
        sets the current bucket to bucket_name.
        creates a bucket if necessary.

        Args:
            bucket_name (string): the bucket name
        """
        if self.does_bucket_exist(bucket_name):
            self.bucket = self.my_s3.Bucket(bucket_name)
            return self.bucket

        self.bucket = self.create_bucket(bucket_name)
        return self.bucket

    def upload_file(self, file_name, upload_name):
        """
        uploads a file_name to the bucket with key upload_name.

        Args:
            bucket (aws bucket resource): the bucket to upload to
            file_name (string): the file name on the file system
            upload_name (string): the key to upload with
        """
        try:
            self.bucket.upload_file(file_name,upload_name)
        except Exception as e:
            print("could not upload: " + file_name)
            print(e)


    def make_stub(self, stub_name):
        """
        uploads a stub to the bucket to represent an empty directory.
        the stub is an invalid file name, so it is impossible for a file to have this 
        file name.

        Args:
            bucket (aws bucket resource): the bucket to upload to.
            stub_name (string): the name of the directory.
        """
        try:
            self.bucket.put_object(Key=(stub_name)) #upload a stub.
        except Exception as e:
            print("could not upload empty directory: " + stub_name)
            print(e)

    def download_file(self, file_name, obj_name):
        """
        Downloads the object with obj_name in the current bucket into
        file_name

        Args:
            file_name (string): the name of the file to be save
            obj_name (string): the name of the object to download
        """
        try:
            self.bucket.download_file(obj_name, file_name)
        except Exception as e:
            print("could download file")
            print(e)

    def allow_upload(self, key, entry, to_delete):
        """
        Determines whether the file was modified after the last upload.

        Args:
            key (string): the file name
            entry (a file): the file to compare the dates with.
            to_delete (a set of keys): a set of some objects in the bucket. If the
            key is present in the bucket, it will be present in to_delete as well.

        Returns:
            bool: true if you should upload.
        """
        if(not set([key]).issubset(to_delete)): # the key wasn't in the directory in the first place.
            return True
        
        # if the file was modified after it was uploaded.
        if entry.stat().st_mtime >= self.bucket.Object(key).last_modified.timestamp():
            return True

        return False
   

    def recursive_upload(self, dir_name, string_prepend, to_delete):
        """
        Traverses the current directory recursively to upload all files.

        Args:
            dir_name (string): the name of the directory to upload from
            string_prepend (string): what to append before the file name
            to_delete (set(string)): A set of all they keys that should be deleted from the bucket at the end. 
        """

        try:

            with os.scandir(dir_name) as entries:

                empty = True # use this to keep track of wether the directory is empty or not.

                for entry in entries:

                    if entry.is_file(): # upload the file

                        if(self.allow_upload(string_prepend + entry.name, entry, to_delete)):
                            self.upload_file(dir_name + "/" + entry.name, string_prepend + entry.name)
                            print("uploaded: " + string_prepend + entry.name)

                        else:
                            print("Did not re-upload: " + string_prepend + entry.name)

                        to_delete.discard(string_prepend + entry.name) # don't delete this object
                        empty = False

                    elif entry.is_dir(): # make a recursive call
                        if(dir_name[-1] != "/"): # if the directory name doesn't end with a trailing slash, add a slash.
                            self.recursive_upload(dir_name + "/" + entry.name, string_prepend + entry.name + "/", to_delete)
                        
                        else:
                            self.recursive_upload(dir_name  + entry.name, string_prepend + entry.name + "/", to_delete)
                        
                        empty = False

                if empty and (string_prepend != ""): #If the current directory is empty and not the root directory.
                    if (not set([string_prepend]).issubset(to_delete)):
                        self.make_stub(string_prepend) # Always upload empty directories. They are not worth optimizing.\
                        print("uploaded: " + string_prepend)
                    else:
                        print("Did not re-upload: : " + string_prepend)

                    to_delete.discard(string_prepend)

        except NotADirectoryError as e:
            print("Not A directory")
            print(e)
        except Exception as e:
            print("Error uploading from the directory: " + dir_name)
            print(e)

    
    def upload(self, dir_name, bucket_name):
        """
        uploades the content of dir_name to the bucket called bucket_name

        Args:
            dir_name (string): the name of the directory to upload from
            bucket_name (string): the name of the bucket to upload to
        """
        if(self.set_bucket(bucket_name) == None):
            return

        to_delete = set() # a set of the keys of what was not uploaded

        for obj in self.bucket.objects.all():
            to_delete.add(obj.key)

        self.recursive_upload(dir_name,"",to_delete)

        for key in to_delete: # delete all extra objects in the bucket.
            self.bucket.Object(key).delete()
            print("deleted: " + key)

    def download(self, dir_name, bucket_name):
        """Downloads the bucket into the directory.

        Args:
            dir_name (string): the name of the directory.
        """

        self.set_bucket(bucket_name)
        if(self.bucket == None):
            return

        try:
            os.makedirs(dir_name, exist_ok=True)
        except Exception as e:
            print("Error creating download directory.")
            print(e)
            return
            

        for obj in self.bucket.objects.all():  

            if(obj.key[-1] == "/"): # if this is a directory
                os.makedirs(dir_name + "/" + obj.key, exist_ok=True)
                print("created: " + dir_name + "/" + obj.key)

            else: #this is a file.
                arr = obj.key.split("/")
                
                if len(arr) != 1: #this file is in a directory
                    arr = arr[0:-1]
                    separator = "/"
                    os.makedirs(dir_name + "/" + separator.join(arr), mode=0o777, exist_ok=True)

                if((not os.path.exists(dir_name +"/"+ obj.key)) or (os.path.getmtime(dir_name +"/"+ obj.key) < obj.last_modified.timestamp())):
                    #overrwite old files 
                    self.download_file(dir_name +"/"+ obj.key, obj.key)
                    print("downloaded: " + dir_name +"/"+ obj.key)
                else:
                    print("did not re-download: " + dir_name +"/"+ obj.key)
                    





# Main

if(len(sys.argv) != 4):
    print("usage: python backup.py <download|upload> <dir_name> <bucket_name>")
    exit()

upload = False
if (sys.argv[1] == "upload"):
    upload = True
elif (sys.argv[1] == "download"):
    upload = False
else:
    print("usage: python backup.py <d|u> <dir_name> <bucket_name>")
    exit()

backup = Backup(boto3.resource('s3'))
if(upload):
    backup.upload(sys.argv[2], sys.argv[3])
else:
    backup.download(sys.argv[2],sys.argv[3])
