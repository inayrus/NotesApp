/* ************************************************************************************************
 * File where a photo-to-text conversion request is being send to the Microsoft Computer Vision API.
 *
 * Code is based on:
 * https://docs.microsoft.com/nl-nl/azure/cognitive-services/computer-vision/quickstarts/java-hand-text
 * https://stackoverflow.com/questions/40099028/how-to-use-http-post-with-application-octet-stream-in-android-microsoft-cogn
 *
 * by Valerie Sawirja
 * ***********************************************************************************************/

package com.example.finalappproject;

import android.content.Context;

import org.apache.http.entity.FileEntity;
import org.json.JSONArray;

import java.io.File;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.Header;
import org.json.JSONObject;

public class ConvertRequest {

    // instance variables
    private Context context;
    private Callback activity;

    // callback interface for the send requests (implemented in highscore activity)
    public interface Callback {
        void gotNote(Note note);
        void gotNoteError(String message);
    }

    // constructor
    public ConvertRequest(Context context) {
        this.context = context;
    }


    // **********************************************
    // *** Update or verify the following values. ***
    // **********************************************

    // Replace <Subscription Key> with your valid subscription key.
    private static final String subscriptionKey = "337ae08a8ace4bc7950da0128c1e3752";

    // You must use the same Azure region in your REST API method as you used to
    // get your subscription keys. For example, if you got your subscription keys
    // from the West US region, replace "westcentralus" in the URL
    // below with "westus".
    //
    // Free trial subscription keys are generated in the "westus" region.
    // If you use a free trial subscription key, you shouldn't need to change
    // this region.
    private static final String uriBase =
            "https://westus.api.cognitive.microsoft.com/vision/v2.0/read/core/asyncBatchAnalyze";

    private static final String imageToAnalyze =
            "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/" +
                    "Cursive_Writing_on_Notebook_paper.jpg/800px-Cursive_Writing_on_Notebook_paper.jpg";

    public void convertToText(Callback activity, File imageFile) {


        CloseableHttpClient httpTextClient = HttpClientBuilder.create().build();
        CloseableHttpClient httpResultClient = HttpClientBuilder.create().build();;

        try {
            // This operation requires two REST API calls. One to submit the image
            // for processing, the other to retrieve the text found in the image.

            URIBuilder builder = new URIBuilder(uriBase);

            // Prepare the URI for the REST API method.
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);

            // Request headers.
            // binary image file: content-type, application/octet-stream
            // url: content-type, application/json
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

            // Request body.
            request.setEntity(new FileEntity(imageFile));
//            StringEntity requestEntity =
//                    new StringEntity("{\"url\":\"" + imageToAnalyze + "\"}");
//            request.setEntity(requestEntity);

            // Two REST API methods are required to extract handwritten text.
            // One method to submit the image for processing, the other method
            // to retrieve the text found in the image.

            // Call the first REST API method to detect the text.
            HttpResponse response = httpTextClient.execute(request);
            System.out.println("request send");

            // Check for success.
            if (response.getStatusLine().getStatusCode() != 202) {
                // Format and display the JSON error message.
                HttpEntity entity = response.getEntity();
                String jsonString = EntityUtils.toString(entity);
                JSONObject json = new JSONObject(jsonString);
                System.out.println("Error:\n");
                System.out.println(json.toString(2));
                return;
            }

            // Store the URI of the second REST API method.
            // This URI is where you can get the results of the first REST API method.
            String operationLocation = null;

            // The 'Operation-Location' response header value contains the URI for
            // the second REST API method.
            Header[] responseHeaders = response.getAllHeaders();
            for (Header header : responseHeaders) {
                if (header.getName().equals("Operation-Location")) {
                    operationLocation = header.getValue();
                    break;
                }
            }

            if (operationLocation == null) {
                System.out.println("\nError retrieving Operation-Location.\nExiting.");
                System.exit(1);
            }

            // If the first REST API method completes successfully, the second
            // REST API method retrieves the text written in the image.
            //
            // Note: The response may not be immediately available. Handwriting
            // recognition is an asynchronous operation that can take a variable
            // amount of time depending on the length of the handwritten text.
            // You may need to wait or retry this operation.

            System.out.println("\nHandwritten text submitted.\n" +
                    "Waiting 10 seconds to retrieve the recognized text.\n");
            Thread.sleep(10000);

            // Call the second REST API method and get the response.
            HttpGet resultRequest = new HttpGet(operationLocation);
            resultRequest.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

            HttpResponse resultResponse = httpResultClient.execute(resultRequest);
            HttpEntity responseEntity = resultResponse.getEntity();

            if (responseEntity != null) {
                // Format and display the JSON response.
                String jsonString = EntityUtils.toString(responseEntity);
                JSONObject json = new JSONObject(jsonString);
                System.out.println("Text recognition result response: \n");
                System.out.println(json.toString(2));
            }

            // unpack response

            // send string/ note to activity.gotNote();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
