using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Net;
using System.Net.Http;
using Newtonsoft;
using Newtonsoft.Json;

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

            if (firstnameTxtbox.Text != null && !firstnameTxtbox.Text.Equals(""))
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
            if(response.StatusCode == HttpStatusCode.OK)
            {
                output.Text += "Here are the matches: <br /><br />";

                result = result.Substring(1, result.Length - 2);

                int count = 0;
                int lastStart = 0;
                int lastSize = 0;
                for(int i = 0; i < result.Length; i++)
                {
                    if(result[i] == '{')
                    {
                        count++;
                    }
                    
                    if(result[i] == '}')
                    {
                        count--;
                    }

                    lastSize++;

                    if(count == 0)
                    {
                        if(result[i] == '}')
                        {
                            output.Text += result.Substring(lastStart + 1, lastSize - 2) + "<br />";
                        }
                        lastStart = lastStart + lastSize;
                        lastSize = 0;
                        
                    }
                }
            }
            else
            {
                output.Text = result;
            }
            
        }
    }

}