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

// https://docs.aws.amazon.com/AmazonS3/latest/dev/CopyingObjectUsingNetSDK.html


namespace nosqldatabase
{
    public partial class _Default : Page
    {

        private const string sourceBucket = "css490";
        private const string destinationBucket = "cloud-chicken";
        private const string objectKey = "input.txt";
        private const string destObjectKey = "text.txt";
        private static readonly RegionEndpoint bucketRegion = RegionEndpoint.USWest2;

        protected void Page_Load(object sender, EventArgs e)
        {

        }

        protected void LoadButton_Click(object sender, EventArgs e)
        {
            CopyingObjectAsync().Wait();

        }

        protected void QueryButton_Click(object sender, EventArgs e)
        {

        }

        protected void ClearButton_Click(object sender, EventArgs e)
        {

        }

        private static async Task CopyingObjectAsync()
        {

            try
            {
                IAmazonS3 s3Client = new AmazonS3Client(bucketRegion);

                CopyObjectRequest request = new CopyObjectRequest
                {
                    SourceBucket = sourceBucket,
                    SourceKey = objectKey,
                    DestinationBucket = destinationBucket,
                    DestinationKey = destObjectKey
                };
                CopyObjectResponse response = await s3Client.CopyObjectAsync(request);
            }
            catch (AmazonS3Exception e)
            {
                Console.WriteLine("Error encountered on server. Message:'{0}' when writing an object", e.Message);
            }
            catch (Exception e)
            {
                Console.WriteLine("Unknown encountered on server. Message:'{0}' when writing an object", e.Message);
            }
        }

    }

}
