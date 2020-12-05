import json
import boto3

my_file_key = "text.txt"
my_bucket = "chicken-in-the-cloud"

my_table = "people"

fname = 'firstname'
lname = 'lastname'


def remove_items(table, response):
    """
    Removes the items in the response form the table

    Args:
        table (aws table): the table to delete objects from
        response (table.scan() response): contains the objects to delete
    """
    for item in response['Items']:
        table.delete_item(Key={lname: item[lname], fname: item[fname]})


def clear_table():
    """
    Clears the people table.
    """
    table = boto3.resource('dynamodb').Table(my_table)

    response = table.scan()
    last_key = None
    if 'LastEvaluatedKey' in response:
        last_key = response['LastEvaluatedKey']

    remove_items(table, response)

    while(last_key != None):
        response = table.scan(ExclusiveStartKey=last_key) #FIXME: test the pagination
        if 'LastEvaluatedKey' in response:
            last_key = response['LastEvaluatedKey']
        else:
            last_key = None
        remove_items(table, response)
        

def lambda_handler(event, context):
    """
    Clears the table and deletes the s3 object.
    
    Provided through the DELETE verb.
    """

    # Delete the s3 object
    boto3.resource('s3').Bucket(my_bucket).Object(my_file_key).delete()

    clear_table()

    return {
        'statusCode': 200,
        'body': json.dumps('The S3 object has been deleted and the table has been emptied.')
    }


#TODO: Delete before uploading
#print(lambda_handler("NULL", "NULL"))
