package com.daftar.study;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.JavascriptInterface;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.graphics.Color;
import androidx.core.view.WindowCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
    private WebView webView;
    private WebViewAssetLoader assetLoader;
    private ValueCallback<Uri[]> fileCallback;
    private static final int FILE_CHOOSER_REQUEST = 4102;
    private String lastPickedUri = "";
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.rgb(245,244,239));
        getWindow().setNavigationBarColor(Color.rgb(245,244,239));
        getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        assetLoader = new WebViewAssetLoader.Builder().addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        webView = new WebView(this);
        ViewCompat.setOnApplyWindowInsetsListener(webView, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(0, bars.top, 0, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); settings.setDomStorageEnabled(true); settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false); settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false); settings.setSupportMultipleWindows(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) { return assetLoader.shouldInterceptRequest(request.getUrl()); }
        });
        webView.addJavascriptInterface(new NativeDocumentBridge(), "DAFTARNative");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                boolean json = false; String[] accepted = params.getAcceptTypes();
                if (accepted != null) for (String type : accepted) if (type != null && type.toLowerCase().contains("json")) json = true;
                Intent intent;
                if (json) intent = params.createIntent();
                else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/pdf","application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/vnd.openxmlformats-officedocument.presentationml.presentation"});
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                }
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try { startActivityForResult(intent, FILE_CHOOSER_REQUEST); }
                catch (Exception e) { fileCallback = null; callback.onReceiveValue(null); return false; }
                return true;
            }
        });
        setContentView(webView);
        ViewCompat.requestApplyInsets(webView);
        webView.loadUrl("https://appassets.androidplatform.net/assets/www/index.html");
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && fileCallback != null) {
            Uri[] result = resultCode == RESULT_OK && data != null && data.getData() != null ? new Uri[]{data.getData()} : null;
            if (result != null) { lastPickedUri = result[0].toString(); try { getContentResolver().takePersistableUriPermission(result[0], Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) {} }
            fileCallback.onReceiveValue(result); fileCallback = null;
        }
    }
    public final class NativeDocumentBridge {
        private final Map<String, File> cachedPdfFiles = new HashMap<>();
        @JavascriptInterface public String getLastPickedUri() { return lastPickedUri; }
        @JavascriptInterface public void setSystemBars(boolean light) {
            runOnUiThread(() -> {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                if (light) flags |= android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                else flags &= ~(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                getWindow().getDecorView().setSystemUiVisibility(flags);
                int color = light ? Color.rgb(245,244,239) : Color.rgb(17,28,33);
                getWindow().setStatusBarColor(color); getWindow().setNavigationBarColor(color);
            });
        }
        @JavascriptInterface public String preparePdfBase64(String encoded) {
            try {
                byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
                if (bytes.length == 0 || bytes.length > 40 * 1024 * 1024) return "";
                File dir = new File(getCacheDir(), "daftar-pdf"); if (!dir.exists() && !dir.mkdirs()) return "";
                File file = File.createTempFile("restored-", ".pdf", dir);
                try (FileOutputStream out = new FileOutputStream(file)) { out.write(bytes); out.flush(); }
                String token = UUID.randomUUID().toString(); cachedPdfFiles.put(token, file); return token;
            } catch (Exception e) { return ""; }
        }
        @JavascriptInterface public int getCachedPdfPageCount(String token) {
            File file = cachedPdfFiles.get(token); if (file == null || !file.exists()) return -1;
            try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY); PdfRenderer renderer = new PdfRenderer(fd)) { return renderer.getPageCount(); }
            catch (Exception e) { return -1; }
        }
        @JavascriptInterface public String renderCachedPdfPage(String token, int pageIndex, int maxWidth) {
            File file = cachedPdfFiles.get(token); if (file == null || !file.exists()) return "";
            try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY); PdfRenderer renderer = new PdfRenderer(fd)) {
                if (pageIndex < 0 || pageIndex >= renderer.getPageCount()) return "";
                try (PdfRenderer.Page page = renderer.openPage(pageIndex)) {
                    float scale = Math.min(1.5f, Math.max(0.5f, maxWidth / (float) page.getWidth()));
                    Bitmap bitmap = Bitmap.createBitmap(Math.round(page.getWidth()*scale), Math.round(page.getHeight()*scale), Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    ByteArrayOutputStream output = new ByteArrayOutputStream(); bitmap.compress(Bitmap.CompressFormat.PNG, 100, output); bitmap.recycle();
                    return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP);
                }
            } catch (Exception e) { return ""; }
        }
        @JavascriptInterface public void clearCachedPdf(String token) {
            File file = cachedPdfFiles.remove(token); if (file != null && file.exists()) file.delete();
        }
        @JavascriptInterface public int getPdfPageCount(String uri) {
            try (ParcelFileDescriptor fd=getContentResolver().openFileDescriptor(Uri.parse(uri),"r"); PdfRenderer renderer=new PdfRenderer(fd)) { return renderer.getPageCount(); } catch(Exception e){return -1;}
        }
        @JavascriptInterface public String renderPdfPage(String uri,int pageIndex,int maxWidth) {
            try (ParcelFileDescriptor fd=getContentResolver().openFileDescriptor(Uri.parse(uri),"r"); PdfRenderer renderer=new PdfRenderer(fd)) {
                if(pageIndex<0||pageIndex>=renderer.getPageCount())return "";
                try(PdfRenderer.Page page=renderer.openPage(pageIndex)){
                    float scale=Math.min(1.5f,Math.max(0.5f,maxWidth/(float)page.getWidth()));
                    Bitmap bitmap=Bitmap.createBitmap(Math.round(page.getWidth()*scale),Math.round(page.getHeight()*scale),Bitmap.Config.ARGB_8888);
                    page.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    ByteArrayOutputStream output=new ByteArrayOutputStream(); bitmap.compress(Bitmap.CompressFormat.PNG,100,output); bitmap.recycle();
                    return Base64.encodeToString(output.toByteArray(),Base64.NO_WRAP);
                }
            }catch(Exception e){return "";}
        }
    }
    @Override public void onBackPressed(){if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){if(webView!=null){webView.loadUrl("about:blank");webView.destroy();}super.onDestroy();}
}
