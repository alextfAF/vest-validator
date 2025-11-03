package com.odc.plugins;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.Logger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

// Capacitor bridge: exposes native methods to JavaScript as the "vestvalidator" plugin.
@CapacitorPlugin(name = "vestvalidator")
public class vestvalidatorPlugin extends Plugin {

    // Holds the core implementation that performs logging and TFLite work.
    private vestvalidator implementation;

    @Override
    public void load() {
        super.load();
        // Provide an application Context to the implementation (for assets access, etc.).
        implementation = new vestvalidator(getContext());
    }

    @PluginMethod
    public void echo(PluginCall call) {
        // Simple passthrough: returns the same value sent from JS.
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void checkHasVest(PluginCall call) {
        // Read image string and optional showLogs from the JS call.
        String imageBase64 = call.getString("image");
        boolean showLogs = call.getBoolean("showLogs", false);

        if (imageBase64 == null) {
            call.reject("Missing required parameter: image (base64)");
            return;
        }

        try {
            // Pass the raw image string directly to the implementation (model expects string input).
            boolean result = implementation.checkHasVest(imageBase64, showLogs);
            JSObject ret = new JSObject();
            ret.put("hasVest", result);
            call.resolve(ret);
        } catch (Exception e) {
            // Surface any decode/inference errors back to JS with logs.
            Logger.error("vestvalidator.checkHasVest error", e.getMessage(), e);
            call.reject("Failed to process image: " + e.getMessage());
        }
    }
}
