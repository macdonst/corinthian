package com.simonmacdonald.corinthian;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class FileDialog extends Plugin {
    private static final int PICK_FILE_RESULT_CODE = 8974;
    private static final int PICK_DIRECTORY_RESULT_CODE = 8975;
    private static final String LOG_TAG = "FileDialog";
    public String callbackId;

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.
     * @param args          JSONArry of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        this.callbackId = callbackId;
        
        JSONObject options = args.optJSONObject(0);

        if (action.equals("pickFile")) {
            showDialog(options, PICK_FILE_RESULT_CODE);
        } else if (action.equals("pickFolder")) {
            showDialog(options, PICK_DIRECTORY_RESULT_CODE);
        } 
        else {
            return new PluginResult(PluginResult.Status.INVALID_ACTION);
        }
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        return r;
    }

    private void showDialog(JSONObject options, int type) {
        Intent intent;
        if (type == PICK_FILE_RESULT_CODE) { 
            intent = new Intent("org.openintents.action.PICK_FILE");
        } else {
            intent = new Intent("org.openintents.action.PICK_DIRECTORY");
        }
        if (options != null) {
            String title = options.optString("title");
            if (title != null) {
                intent.putExtra("org.openintents.extra.TITLE", title);
            }
            String button = options.optString("button");
            if (button != null) {
                intent.putExtra("org.openintents.extra.BUTTON_TEXT", button);
            }
        }
        //intent.setData(Uri.fromFile(new File("/")));
        try {
            this.ctx.startActivityForResult((Plugin)this,intent,PICK_FILE_RESULT_CODE);
        } catch (ActivityNotFoundException e) { 
            showDownloadDialog();
        }
    }
    
    private void showDownloadDialog() {
        final Context context = this.ctx.getContext();
        Runnable runnable = new Runnable() {
            public void run() {

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Install File Manager?");
                dialog.setMessage("This requires the free OI File Manager app. Would you like to install it now?");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dlg, int i) {
                        dlg.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW, 
                            Uri.parse("market://search?q=pname:org.openintents.filemanager")
                        );
                        try {
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) { 
//                          We don't have the market app installed, so download it directly.
                            Intent in = new Intent(Intent.ACTION_VIEW);
                            in.setData(Uri.parse("http://openintents.googlecode.com/files/FileManager-1.2.apk"));
                            context.startActivity(in);

                        }

                    }
                });
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dlg, int i) {
                        dlg.dismiss();
                    }
                });
                dialog.create();
                dialog.show();
            }
        };
        this.ctx.runOnUiThread(runnable);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
      //super.onActivityResult(reqCode, resultCode, data);
      //Log.d(LOG_TAG, "Data is " + data.getData().toString());
      Log.d(LOG_TAG, "we are in on activity result");
      switch (reqCode) {
      case PICK_FILE_RESULT_CODE:
      case PICK_DIRECTORY_RESULT_CODE: {
          if (resultCode==Activity.RESULT_OK && data!=null && data.getData()!=null) {
              String filePath = data.getData().getPath();
              Log.d(LOG_TAG, "The data is = " + filePath);
              this.success(new PluginResult(PluginResult.Status.OK, filePath), this.callbackId);
          }
          break;
      }
  }
    }
}
