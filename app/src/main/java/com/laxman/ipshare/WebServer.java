package com.laxman.ipshare;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.laxman.ipshare.models.DataModel;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    private Context context;
    private DataModel dataModel;

    WebServer(Context context, int port) {
        super(port);
        this.context = context;
        dataModel = new ViewModelProvider((AppCompatActivity)context).get(DataModel.class);
    }
    public void start() throws IOException{
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    private InputStream getInputStream(String path){
        try {
            if (path.startsWith("/")){
                path = path.substring(1);
            }
            return context.getAssets().open(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return getInputStream("404.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        String mime_type = null;
        InputStream inputStream;

        if (session.getMethod() == Method.POST) {
            Map<String, String> stringMap = new HashMap<>();
            try {
                session.parseBody(stringMap);
                //File path from Body
                String filePath = stringMap.get("file");
                //File name from parameters
                String fileName = String.valueOf(session.getParameters().get("file"));
                fileName = fileName.substring(1, fileName.length() - 1);

                if (filePath != null) {
                    String response = App.read(getInputStream("file-upload.html"));
                    response = App.addNavMenus(response, "file-upload");

                    if (filePath.length() < 1) {
                        response = response.replace("{{ message }}", "<p>No file was selected</p>");
                        return NanoHTTPD.newFixedLengthResponse(response);
                    }

                    App.getAppDir(context);
                    File in = new File(filePath);
                    File out = new File(App.getAppDir(context) , fileName);
                    FileUtils.copyFile(in, out);

                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.parse("file://" + out.getAbsolutePath())));

                    response = response.replace("{{ message }}",
                            "<p>File uploaded successfully to " + out.getAbsolutePath() + "</p>");
                    ((AppCompatActivity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dataModel.getDataChanged().setValue(true);
                        }
                    });
                    return NanoHTTPD.newFixedLengthResponse(response);
                }
            } catch (IOException | ResponseException e) {
                e.printStackTrace();
            }
        }

        if (uri.equals("/") | uri.equals("/index.html") | uri.equals("/file-explorer")){
            inputStream = getInputStream("index.html");
            try {
                String response = App.read(inputStream);
                response = App.addNavMenus(response, "file-explorer");

                //Uploading file
                if (session.getParameters().containsKey("file")) {
                    String file = String.valueOf(session.getParameters().get("file"));
                    file = file.substring(1, file.length()-1);

                    File internalFile = new File(file);

                    if (!internalFile.exists() | internalFile.isDirectory()) {
                        return newChunkedResponse(Response.Status.OK, MIME_HTML, getInputStream("404.html"));
                    }

                    FileInputStream fileInputStream = new FileInputStream(file);
                    Response responseData = NanoHTTPD.newChunkedResponse(Response.Status.OK, "application/octet-stream", fileInputStream);
                    responseData.addHeader("Content-Disposition", "attachment; filename=" + internalFile.getName());
                    return responseData;
                }

                //Directory & files Listing
                if (session.getParameters().containsKey("dir")) {
                    String dir = String.valueOf(session.getParameters().get("dir"));
                    dir = dir.substring(1, dir.length()-1);
                    response = App.addDirectoryNavigation(response, dir);
                    response = App.addFiles(response, dir);
                } else {
                    response = App.addDirectoryNavigation(response, App.externalDir().getAbsolutePath());
                    response = App.addFiles(response, "/");
                }

                return NanoHTTPD.newFixedLengthResponse(response);
            } catch (IOException e) {
                e.printStackTrace();
                return NanoHTTPD.newFixedLengthResponse("Oops! Something went wrong");
            }

        } else if (uri.equals("/clipboard")) {
            try {
                InputStream assetsInputStream = getInputStream(uri + ".html");
                String response = App.read(assetsInputStream);
                response = App.addNavMenus(response, "clipboard");
                response = App.addClipboard(response);
                return NanoHTTPD.newFixedLengthResponse(response);
            } catch (IOException e) {
                e.printStackTrace();
                return NanoHTTPD.newFixedLengthResponse("Oops! Something went wrong");
            }
        } else if (uri.equals("/file-upload")) {
            try {
                InputStream assetsInputStream = getInputStream(uri + ".html");
                String response = App.read(assetsInputStream);
                response = App.addNavMenus(response, "file-upload");
                response = response.replace("{{ message }}", "");
                return NanoHTTPD.newFixedLengthResponse(response);
            } catch (IOException e) {
                e.printStackTrace();
                return NanoHTTPD.newFixedLengthResponse("Oops! Something went wrong");
            }
        }

        else if (uri.equals("/about")) {
            try {
                InputStream assetsInputStream = getInputStream(uri + ".html");
                String response = App.read(assetsInputStream);
                response = App.addNavMenus(response, "about");
                response = App.addAboutContents(context, response);

                return NanoHTTPD.newFixedLengthResponse(response);
            } catch (IOException e) {
                e.printStackTrace();
                return NanoHTTPD.newFixedLengthResponse("Oops! Something went wrong");
            }
        } else {
            if (uri.endsWith(".css")){
                mime_type = "text/css";
            } else if (uri.endsWith(".js")){
                mime_type = "application/javascript";
            } else if (uri.endsWith(".html")){
                mime_type = MIME_HTML;
            } else if (uri.endsWith(".txt")){
                mime_type = MIME_PLAINTEXT;
            } else if (uri.endsWith(".png")){
                mime_type = "image/png";
            }
            inputStream = getInputStream(uri);
        }

        if (inputStream != null){
            return NanoHTTPD.newChunkedResponse(Response.Status.OK, mime_type, inputStream);
        } else {
            return NanoHTTPD.newFixedLengthResponse("Oops! Something went wrong");
        }
    }
}
