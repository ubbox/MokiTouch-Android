package com.moki.touch.cache;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.moki.touch.models.ContentObject;
import com.moki.touch.util.UrlUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Copyright (C) 2014 Moki Mobility Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 *
 * You may only use this file in compliance with the license
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class DownloadThread {

    private ContentObject mContentObject;
    private Downloader mDownloader;
    private String filepath;

    public DownloadThread(ContentObject contentObject, Downloader downloader, String filePath) {
        mContentObject = contentObject;
        mDownloader = downloader;
        this.filepath = filePath;
        downloadContent();
    }

    public void downloadContent() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                loadRemoteData(UrlUtil.addHttp(mContentObject.getUrl()), mContentObject.getContentFileName());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mDownloader.downloadComplete(mContentObject);
            }
        };
        task.execute();
    }

    public File getExternalDirectory() {

        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dataDir = new File(dir + filepath);

        if (!dataDir.exists())
            dataDir.mkdirs();

        return  dataDir;
    }

    public String loadRemoteData(String link, String filename) {

        File outputFile = null;

        try {

            Log.i(DownloadThread.class.getSimpleName(), "Downloading File: " + link);

            File dataDir = getExternalDirectory();

            // Deletes the file if it exists
            outputFile = new File(dataDir, filename);
            outputFile.delete();

            //this will be used to write the downloaded data into the file we created
            FileOutputStream fileOutput = new FileOutputStream(outputFile);

            URL url = new URL(link);

            //create the new connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            //set up some things on the connection
            urlConnection.setRequestMethod("GET");

            //and connect!
            urlConnection.connect();
            int total = urlConnection.getContentLength();

            //this will be used in reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0; //used to store a temporary size of the buffer
            int dataRead = 0;

            //now, read through the input buffer and write the contents to the file
            while ((bufferLength = inputStream.read(buffer)) > 0) {

                dataRead += bufferLength;

                //add the data in the buffer to the file in the file output stream (the file on the sd card
                fileOutput.write(buffer, 0, bufferLength);
            }

            //close the output stream when done
            fileOutput.close();

        } catch (Exception e) {

            Log.e(DownloadThread.class.getSimpleName(), "Error Downloading", e);

            if (outputFile != null)
                outputFile.delete();

            return "";
        }

        return outputFile.getAbsolutePath();
    }
}
