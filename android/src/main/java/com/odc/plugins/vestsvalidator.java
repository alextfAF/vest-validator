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
public class vestsvalidator {
    // Application Context used to access assets (model.tflite) safely.
    private final Context context;
    // Lazily-initialized TensorFlow Lite interpreter (created once and reused).
    private Interpreter tflite;

    // Store application Context to avoid leaking an Activity.
    public vestsvalidator(Context context) {
        this.context = context.getApplicationContext();
    }

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }

    // Memory-map the model file from assets for efficient loading.
    private MappedByteBuffer loadModel(String filename) throws IOException {
        AssetFileDescriptor afd = context.getAssets().openFd(filename);
        FileInputStream inputStream = new FileInputStream(afd.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = afd.getStartOffset();
        long declaredLength = afd.getDeclaredLength();
        MappedByteBuffer mapped = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        inputStream.close();
        fileChannel.close();
        afd.close();
        return mapped;
    }

    // Accepts the image as a base64 STRING and feeds it directly to the model (model must declare STRING input and BOOL output).
    public boolean checkHasVest(String imageString, boolean showLogs) {
        try {
            if (tflite == null) {
                MappedByteBuffer model = loadModel("model.tflite");
                tflite = new Interpreter(model, new Interpreter.Options());
            }

            int[] inShape = tflite.getInputTensor(0).shape();
            DataType inType = tflite.getInputTensor(0).dataType();
            if (inType != DataType.STRING) {
                Logger.error("checkHasVest", "Model input[0] is not STRING; got " + inType, null);
                return false;
            }

            int elements = 1;
            for (int d : inShape) { elements *= d; }
            if (elements < 1) { elements = 1; }

            String[] strings = new String[elements];
            Arrays.fill(strings, imageString);

            ByteBuffer inputBuffer = encodeStringTensor(strings);

            int[] outShape = tflite.getOutputTensor(0).shape();
            int outElements = 1; for (int d : outShape) outElements *= d;
            DataType outType = tflite.getOutputTensor(0).dataType();
            if (outType != DataType.BOOL) {
                Logger.error("checkHasVest", "Unsupported output dtype (expected BOOL): " + outType, null);
                return false;
            }

            ByteBuffer outBuffer = ByteBuffer.allocateDirect(outElements).order(ByteOrder.nativeOrder());
            tflite.run(inputBuffer, outBuffer);

            outBuffer.rewind();
            boolean hasVest = (outBuffer.get(0) != 0);
            if (showLogs) {
                int len = (imageString != null) ? imageString.length() : 0;
                Logger.info("checkHasVest", "STRING input length=" + len + ", result=" + hasVest);
            }
            return hasVest;
        } catch (Exception e) {
            Logger.error("TFLite inference failed (string)", e.getMessage(), e);
            return false;
        }
    }

    // Encode a STRING tensor for TFLite: header of N int32 offsets, then each string length + bytes.
    private static ByteBuffer encodeStringTensor(String[] strings) {
        if (strings == null) strings = new String[] { "" };
        int n = strings.length;
        int payload = 0;
        byte[][] data = new byte[n][];
        for (int i = 0; i < n; i++) {
            data[i] = (strings[i] != null) ? strings[i].getBytes(java.nio.charset.StandardCharsets.UTF_8) : new byte[0];
            // 4 bytes for length + content length
            payload += 4 + data[i].length;
        }
        int header = n * 4; // one int32 offset per string
        ByteBuffer buf = ByteBuffer.allocateDirect(header + payload).order(ByteOrder.nativeOrder());
        int offset = header;
        for (int i = 0; i < n; i++) {
            buf.putInt(offset);       // write offset for this string
            int len = data[i].length;
            buf.position(offset);
            buf.putInt(len);
            if (len > 0) {
                buf.put(data[i]);
            }
            offset += 4 + len;
        }
        buf.position(0);
        return buf;
    }
}
