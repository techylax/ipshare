package com.laxman.ipshare;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;

import com.laxman.ipshare.models.NavigationMenu;
import com.laxman.ipshare.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class App {

    private static String clipboard = null;
    private static ClipboardManager clipboardManager;
    private static boolean wifiConnected = false;

    public static boolean isNetworkAvailable(Context context) {
        wifiConnected = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager
                        .getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities == null) {
                    return false;
                }
                wifiConnected = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                return true;
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo == null) {
                    return false;
                } else {
                    wifiConnected = activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                }
            }
        }
        return false;
    }

    static boolean isWifiConnected() {
        return wifiConnected;
    }

    static void initClipboard(Context context) {
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null | Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            clipboard = context.getString(R.string.app_name) + " is not allowed to access your clipboard";
            return;
        }

        setClipboard();

        clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                setClipboard();
            }
        });

    }

    // check if nothing is found in clipboard
    private static void setClipboard() {
        if (clipboardManager.hasPrimaryClip() && clipboardManager.getPrimaryClip() != null) {
            clipboard = String.valueOf(clipboardManager.getPrimaryClip().getItemAt(0).getText());
        } else {
            clipboard = "No clipboards found";
        }
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), text);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clip);
        }
    }

    public static boolean checkStoragePermission(Context context) {
        int readPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (readPermission != writePermission) {
            return false;
        }
        return writePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static String getAppUrl(String packageName) {
        return "http://play.google.com/store/apps/details?id=" + packageName;
    }

    public static void shareApp(Context context, String chooserTitle, String message) {
        try {
            ShareCompat.IntentBuilder.from((AppCompatActivity) context)
                    .setType("text/plain")
                    .setChooserTitle(chooserTitle)
                    .setText(message)
                    .startChooser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openGooglePlay(Context context, String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    public static void emailIntent(Context context, String mailto, String subject, String text, String chooserTitle) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mailto));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(emailIntent, chooserTitle));
    }

    public static List<String> getIpAddress(int port) {
        List<String> ipAddress = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ipAddress.add("http://" + inetAddress.getHostAddress() + ":" + port);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

    static File externalDir() {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public static File getAppDir(Context context) {
        File dir = new File(App.externalDir() + "/" + context.getString(R.string.app_name));
        if (!dir.exists()) {
            // noinspection ResultOfMethodCallIgnored

            dir.mkdir();
        }
        return dir;
    }

    static String read(InputStream inputStream) throws IOException {
        int data;
        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        while ((data = bufferedReader.read()) != -1) {
            stringBuilder.append((char) data);
        }
        return stringBuilder.toString();
    }

    static String addNavMenus(String content, String currentPagePath) {
        List<NavigationMenu> navigationMenus = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        navigationMenus.add(new NavigationMenu("images/file-explorer.png",
                "File Explorer", "file-explorer"));
        navigationMenus.add(new NavigationMenu("images/clipboard.png", "Clipboard", "clipboard"));
        navigationMenus.add(new NavigationMenu("images/upload.png", "File Upload", "file-upload"));
        navigationMenus.add(new NavigationMenu("images/about.png", "About", "about"));

        for (int i = 0; i < navigationMenus.size(); i++) {
            NavigationMenu navigationMenu = navigationMenus.get(i);
            String html;
            if (currentPagePath.equals(navigationMenu.getPath())) {
                html = "  <li class=\"li-focused\">\n" +
                        "              <a href=\"" + navigationMenu.getPath() +
                        "\"><img class=\"icon\" src=\"" + navigationMenu.getIcon() + "\"> " +
                        navigationMenu.getTitle() + "</a>\n" +
                        "            </li>" + "\n";

            } else {
                html = "  <li>\n" +
                        "              <a href=\"" + navigationMenu.getPath() +
                        "\"><img class=\"icon\" src=\"" + navigationMenu.getIcon() + "\"> " +
                        navigationMenu.getTitle() + "</a>\n" +
                        "            </li>" + "\n";
            }
            if (i == navigationMenus.size() - 1) {
                stringBuilder.append("<hr/>" + "\n");
            }
            stringBuilder.append(html);
        }
        return content.replace("{{ navigation_menu }}", stringBuilder);
    }

    static String addDirectoryNavigation(String content, String currentDir) throws UnsupportedEncodingException {

        File file = new File(currentDir);
        if (!file.exists()) {
            return content.replace("{{ paths }}", "Error");
        }

        StringBuilder stringBuilder = new StringBuilder();
        String html;

        if (currentDir.startsWith("/")) {
            currentDir = currentDir.substring(1);
        }

        String[] parts = currentDir.split("/");

        for (int i = 0; i < parts.length; i++) {

            int startIndex = file.getAbsolutePath().indexOf(parts[i]);
            String path = file.getAbsolutePath().substring(0, startIndex);

            // Encode URL
            path = URLEncoder.encode(path, "UTF-8");
            parts[i] = URLEncoder.encode(parts[i], "UTF-8");

            html = "<a href=\"?dir=" + path + parts[i] + "\">" + URLDecoder.decode(parts[i],
                    "UTF-8") + "</a>\n";

            if (i != parts.length - 1) {
                html += " > ";
            }
            stringBuilder.append(html);
        }
        return content.replace("{{ paths }}", stringBuilder.toString());
    }

    static String addFiles(String content, String currentPath) throws UnsupportedEncodingException {
        File fileDir;
        if (currentPath.equals("/")) {
            fileDir = externalDir();
        } else {
            fileDir = new File(currentPath);
        }

        if (!fileDir.exists()) {
            return content.replace("{{ file }}", "File not found");
        }

        List<String> folders = new ArrayList<>();
        List<String> files = new ArrayList<>();
        List<String> orderFiles = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();

        File[] listFiles = fileDir.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    folders.add(file.getAbsolutePath());
                } else {
                    files.add(file.getAbsolutePath());
                }
            }

            // Sorting
            Collections.sort(folders);
            Collections.sort(files);

            orderFiles.addAll(folders);
            orderFiles.addAll(files);

            for (String path : orderFiles) {
                File file = new File(path);
                String name = file.getName();

                // Encode URL
                path = URLEncoder.encode(path, "UTF-8");

                String html;
                if (file.isDirectory()) {
                    html = "<div class=\"file\">\n" +
                            "               <a href=\"?dir=" + path
                            + "\"><img class=\"icon\" src=\"images/folder.png\"> " + name + "</a>\n" +
                            "            </div>\n";
                } else {
                    html = "<div class=\"file\">\n" +
                            "               <a href=\"?file=" + path
                            + "\"><img class=\"icon\" src=\"images/file.png\"> " + name + "</a>\n" +
                            "            </div>\n";
                }

                stringBuilder.append(html);

            }

        }

        return content.replace("{{ file }}", stringBuilder.toString());
    }

    static String addClipboard(String content) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            setClipboard();
        }
        return content.replace("{{ clipboard }}", clipboard);
    }

    static String addAboutContents(Context context, String contents) {
        int webVersion = 1;
        contents = contents.replace("{{ web_version }}", String.valueOf(webVersion));
        contents = contents.replace("{{ app_url }}", getAppUrl(context.getPackageName()));
        return contents;
    }
}