using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using Amazon;
using Amazon.S3;
using Amazon.S3.Model;
using System.Threading.Tasks;
using System.IO;


// https://docs.aws.amazon.com/AmazonS3/latest/dev/CopyingObjectUsingNetSDK.html


namespace nosqldatabase
{
    public partial class _Default : Page
    {

        private const string sourceBucket = "cloud-chicken"; // Change to css490 later
        private const string destinationBucket = "cloud-chicken";
        private const string objectKey = "input.txt";
        private const string destObjectKey = "text.txt";
        private static readonly RegionEndpoint bucketRegion = RegionEndpoint.USWest2;
        private static IAmazonS3 s3Client = new AmazonS3Client(bucketRegion);

        protected void Page_Load(object sender, EventArgs e)
        {

        }

        protected void LoadButton_Click(object sender, EventArgs e)
        {
            TextBox1.Text = "SDFS";

            //CopyingObjectAsync().Wait();
            //ReadObjectDataAsync().Wait();

        }

        protected void QueryButton_Click(object sender, EventArgs e)
        {

        }

        protected void ClearButton_Click(object sender, EventArgs e)
        {

        }

        private async Task CopyingObjectAsync()
        {

            try
            {

                CopyObjectRequest request = new CopyObjectRequest
                {
                    SourceBucket = sourceBucket,
                    SourceKey = objectKey,
                    DestinationBucket = destinationBucket,
                    DestinationKey = destObjectKey
                };

                CopyObjectResponse response = await s3Client.CopyObjectAsync(request); //TODO: what is in the response?

                Response.Write(response);

            }
            catch (AmazonS3Exception e)
            {
                Response.Write(String.Format("Error encountered on server. Message:'{0}' when copying an object", e.Message));
            }
            catch (Exception e)
            {
                Response.Write(String.Format("Unknown encountered on server. Message:'{0}' when copying an object", e.Message));
            }
        }

        private static async Task ReadObjectDataAsync()
        {
            string responseBody = "";
            try
            {

                GetObjectRequest request = new GetObjectRequest
                {
                    BucketName = sourceBucket,
                    Key = objectKey
                };

                using (GetObjectResponse response = await s3Client.GetObjectAsync(request))
                using (Stream responseStream = response.ResponseStream)
                using (StreamReader reader = new StreamReader(responseStream))
                {
                    string title = response.Metadata["x-amz-meta-title"]; // Assume you have "title" as medata added to the object.
                    string contentType = response.Headers["Content-Type"];
                    Console.WriteLine("Object metadata, Title: {0}", title);
                    Console.WriteLine("Content type: {0}", contentType);

                    responseBody = reader.ReadToEnd(); // Now you process the response body.
                    Console.WriteLine(responseBody);
                }
            }
            catch (AmazonS3Exception e)
            {
                // If bucket or object does not exist
                Console.WriteLine("Error encountered ***. Message:'{0}' when reading object", e.Message);
            }
            catch (Exception e)
            {
                Console.WriteLine("Unknown encountered on server. Message:'{0}' when reading object", e.Message);
            }
        }

    }

}
