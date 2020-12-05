import json
import boto3

my_bucket = "chicken-in-the-cloud"
my_file_key = "text.txt"

input_bucket = "css490"
input_file_key = "input.txt"

fname = 'firstname'
lname = 'lastname'

my_table = "people"

def get_map(line):
    line = line.split()

    if(len(line) < 2):
        return None        #Ignore invalid items

    item_map = {}
    item_map[lname] = line[0].lower() # convert to lower case to make queries case-insensitive
    item_map[fname] = line[1].lower()

    for i in range(2,len(line)):
        temp = line[i].split("=")

        if(len(temp) != 2):
            continue        #Ignore invalid attributes

        item_map[temp[0]] = temp[1]

    return item_map

def lambda_handler(event, context):
    """
    Loads the input data in s3 that the professor provides and puts it into
    the database.

    Args:
        event ([type]): [description]
        context ([type]): [description]

    Returns:
        [type]: [description]
    """

    #Define s3
    s3 = boto3.resource('s3')


    #Copy the s3 object
    copy_source = {
          'Bucket': my_bucket, #TODO: change to input_bucket
          'Key': input_file_key,
        }
        
    bucket = s3.Bucket(my_bucket)
    bucket.copy(copy_source, my_file_key, ExtraArgs={'ACL':'public-read'}) # make the file public
    
    
    # Get the content of the copy.
    obj = bucket.Object(my_file_key)
    lines = (obj.get()['Body'].read().decode('utf-8')).splitlines()


    # get the table
    table = boto3.resource('dynamodb').Table(my_table)

    for line in lines:
        if((len(line) != 0) and (not line.isspace())):
            item_map = get_map(line)
            if(item_map != None):
                table.put_item(Item=item_map)

    return {
        'statusCode': 200,
        'body': json.dumps("Yes!")
    }



#TODO: Delete before uploading
#print(lambda_handler("NULL", "NULL"))
