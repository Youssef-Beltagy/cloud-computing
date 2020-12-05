import json
import boto3

my_table = "people"

fname = 'firstname'
lname = 'lastname'

def put_in_list(response, ans):
    """
    Appends the people in the response to ans.

    """
    if(len(response['Items']) == 0):
        return

    for item in response['Items']:
        ans.append(item)

def get_item_response(event, table):
    """
    Gets a single item from the table
    """

    response = table.get_item( Key={
            lname:event['queryStringParameters'][lname],
            fname:event['queryStringParameters'][fname]
        }
    )

    if (not 'Item' in response):
        return {
            'statusCode': 404,
            'body': json.dumps('Could not find Item!')
        }

    return {
        'statusCode': 200,
        'body': json.dumps(response['Item'])
    }

def get_lastname_response(event, table):
    """
    Gets all table entries with the provided lastname

    """
    response = table.query(
        KeyConditionExpression=
            boto3.dynamodb.conditions.Key(lname).eq(event['queryStringParameters'][lname])
    )

    if(len(response['Items']) == 0):
        return {
            'statusCode': 404,
            'body': json.dumps('Could not find Items!')
        }

    ans = []
    last_key = None
    if 'LastEvaluatedKey' in response:
        last_key = response['LastEvaluatedKey']

    put_in_list(response, ans)

    while(last_key != None):  #FIXME: test the pagination

        response = table.query(
            KeyConditionExpression=boto3.dynamodb.conditions.Key(lname).eq(event['queryStringParameters'][lname]),
            ExclusiveStartKey=last_key)

        if ('LastEvaluatedKey' in response):
            last_key = response['LastEvaluatedKey']
        else:
            last_key = None

        put_in_list(response, ans)

    return {
            'statusCode': 200,
            'body': json.dumps(ans)
        }

def get_firstname_response(event, table):
    """
    Gets all table entries with the provided firstname

    """
    response = table.scan(
        FilterExpression=boto3.dynamodb.conditions.Attr(fname).eq(event['queryStringParameters'][fname])
    )
    
    if(len(response['Items']) == 0):
        return {
            'statusCode': 404,
            'body': json.dumps('Could not find Items!')
        }

    ans = []
    last_key = None
    if 'LastEvaluatedKey' in response:
        last_key = response['LastEvaluatedKey']

    put_in_list(response, ans)

    while(last_key != None):  #FIXME: test the pagination

        response = table.scan(
            KeyConditionExpression=boto3.dynamodb.conditions.Attr(fname).eq(event['queryStringParameters'][fname]),
            ExclusiveStartKey=last_key)

        if ('LastEvaluatedKey' in response):
            last_key = response['LastEvaluatedKey']
        else:
            last_key = None

        put_in_list(response, ans)

    return {
            'statusCode': 200,
            'body': json.dumps(ans)
        }

def lambda_handler(event, context):
    """
    An api to query the database.
    """

    # no query parameters
    if(not ('queryStringParameters' in event) or not (type(event['queryStringParameters']) is dict)):
        return {
            'statusCode': 404,
            'body': json.dumps('Invalid Query!')
        }

    isLastName = lname in event['queryStringParameters']
    isFirstName = fname in event['queryStringParameters']
    # no firstname nor a lastname

    if(isLastName):
        event['queryStringParameters'][lname] = event['queryStringParameters'][lname].lower()

    if(isFirstName):
        event['queryStringParameters'][fname] = event['queryStringParameters'][fname].lower()

    if(not isLastName and not isFirstName):
        return {
            'statusCode': 404,
            'body': json.dumps('Invalid Query!')
        }

    table = boto3.resource('dynamodb').Table(my_table)

    if(isLastName and isFirstName):
        return get_item_response(event, table)

    if(isLastName):
        return get_lastname_response(event, table)
    
    return get_firstname_response(event, table)


#delete before deployment
#event = {'queryStringParameters':{fname:'Youssef'}}
#print(lambda_handler(event, "NULL"))