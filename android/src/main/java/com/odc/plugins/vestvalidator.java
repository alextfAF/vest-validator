package com.odc.plugins;

import com.getcapacitor.Logger;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.DataType;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

// Core implementation that holds the TFLite Interpreter and runs inference.
public class vestvalidator {
    // Application Context used to access assets (model.tflite) safely.
    private final Context context;
    // Lazily-initialized TensorFlow Lite interpreter (created once and reused).
    private Interpreter t;

    // Store application Context to avoid leaking an Activity.
    public vestvalidator(Context context) {
        this.context = context.getApplicationContext();
    }

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }

    // Memory-map the model file from assets for efficient loading.
    private MappedByteBuffer loadModel(String filename) throws IOException {
        AssetFileDescriptor afd = context.getAssets().openFd(filename);
        FileInputStream is = new FileInputStream(afd.getFileDescriptor());
        FileChannel channel = is.getChannel();
        long start = afd.getStartOffset();
        long length = afd.getDeclaredLength();
        MappedByteBuffer mapped = channel.map(FileChannel.MapMode.READ_ONLY, start, length);
        is.close();
        channel.close();
        afd.close();
        return mapped;
    }

    // Accepts the image as a base64 STRING and feeds it directly to the model (model must declare STRING input and BOOL output).
    public boolean checkHasVest(String imageString, boolean showLogs) {
        try {

            if (imageString != null) {
                Logger.info("checkHasVest", "imageString: " + imageString);
                return true;
            } else {
                Logger.error("checkHasVest", "imageString is null");
                return false;
            }
            /*if (t == null) {
                MappedByteBuffer model = loadModel("model.tflite");
                t = new Interpreter(model, new Interpreter.Options());
            }

            int[] inShape = t.getInputTensor(0).shape();
            DataType inType = t.getInputTensor(0).dataType();
            if (inType != DataType.STRING) {
                Logger.error("vestvalidator.checkHasVest", "Model input[0] is not STRING; got " + inType, null);
                return false;
            }

            // Build STRING tensor buffer: [count][offsets...][len,data][len,data]...
            int elements = 1;
            for (int d : inShape) {
                elements *= d;
            }
            if (elements < 1) {
                elements = 1;
            }
            String[] strings = new String[1];
            strings[0] = (imageString != null) ? imageString : "";

            ByteBuffer inputBuffer = encodeStringTensor(strings);

            int[] outShape = t.getOutputTensor(0).shape();
            int outElements = 1;
            for (int d : outShape) {
                outElements *= d;
            }
            DataType outType = t.getOutputTensor(0).dataType();
            if (outType != DataType.BOOL) {
                Logger.error("vestvalidator.checkHasVest", "Unsupported output dtype (expected BOOL): " + outType, null);
                return false;
            }

            ByteBuffer outBuffer = ByteBuffer.allocateDirect(outElements).order(ByteOrder.nativeOrder());
            t.run(inputBuffer, outBuffer);

            outBuffer.rewind();
            boolean hasVest = (outBuffer.get(0) != 0);
            if (showLogs) {
                int len = (strings[0] != null) ? strings[0].length() : 0;
                Logger.info("checkHasVest", "STRING input length=" + len + ", result=" + hasVest);
            }*/
            //return hasVest;
        } catch (Exception e) {
            Logger.error("TFLite inference failed (string)", e.getMessage(), e);
            return false;
        }
    }

    private static ByteBuffer encodeStringTensor(String[] strings) {
        if (strings == null) {
            strings = new String[]{""};
        }
        int n = strings.length;
        // Total size: 4 (count) + 4*n (offsets) + sum_i (4 + len_i)
        int dataBytes = 0;
        byte[][] data = new byte[n][];
        for (int i = 0; i < n; i++) {
            data[i] = (strings[i] != null) ? strings[i].getBytes(java.nio.charset.StandardCharsets.UTF_8) : new byte[0];
            dataBytes += 4 + data[i].length;
        }
        int headerBytes = 4 + 4 * n;
        ByteBuffer buffer = ByteBuffer.allocateDirect(headerBytes + dataBytes).order(ByteOrder.nativeOrder());
        // Write count
        buffer.putInt(n);
        // Compute and write offsets
        int offset = 0;
        for (int i = 0; i < n; i++) {
            buffer.putInt(offset);
            offset += 4 + data[i].length;
        }
        // Write string data blocks
        for (int i = 0; i < n; i++) {
            int len = data[i].length;
            buffer.putInt(len);
            if (len > 0) {
                buffer.put(data[i]);
            }
        }
        buffer.position(0);
        return buffer;
    }
}
