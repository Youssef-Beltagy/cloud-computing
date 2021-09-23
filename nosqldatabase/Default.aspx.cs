using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Net;
using System.Net.Http;

namespace nosqldatabase_website
{
    public partial class _Default : Page
    {
        private static HttpClient client = new HttpClient(new HttpClientHandler { AutomaticDecompression = DecompressionMethods.GZip | DecompressionMethods.Deflate });

        private static string baseurl = "https://svj981o6h4.execute-api.us-west-2.amazonaws.com/prod";
        protected void Page_Load(object sender, EventArgs e)
        {
            output.Text = "";
        }

        protected void LoadBtn_Click(object sender, EventArgs e)
        {
            HttpResponseMessage response = client.PutAsync(baseurl, null).Result;
            string result = response.Content.ReadAsStringAsync().Result;
            output.Text = result;
            
        }

        protected void ClearBtn_Click(object sender, EventArgs e)
        {
            HttpResponseMessage response = client.DeleteAsync(baseurl).Result;
            string result = response.Content.ReadAsStringAsync().Result;
            output.Text = result;
        }

        protected void QueryBtn_Click(object sender, EventArgs e)
        {
            string url = baseurl;
            
            if(firstnameTxtbox.Text != null && !firstnameTxtbox.Text.Equals(""))
            {
                url += "?firstname=" + firstnameTxtbox.Text;
            }

            if (lastnameTxtbox.Text != null && !lastnameTxtbox.Text.Equals(""))
            {
                if (!url.Equals(baseurl))
                {
                    url += "&";
                }
                else
                {
                    url += "?";
                }
                url += "lastname=" + lastnameTxtbox.Text;
            }

            HttpResponseMessage response = client.GetAsync(url).Result;
            string result = response.Content.ReadAsStringAsync().Result;
            output.Text = result;
        }
    }
}