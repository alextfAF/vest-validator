package com.odc.plugins;

import com.getcapacitor.Logger;
import android.graphics.Bitmap;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

// Core implementation that holds the TFLite Interpreter and runs inference.
public class vestvalidator {
    // Application Context used to access assets (model.tflite) safely.
    private final Context context;
    // Lazily-initialized TensorFlow Lite interpreter (created once and reused).
    private Interpreter tflite;

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
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(filename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        MappedByteBuffer mapped = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        inputStream.close();
        fileChannel.close();
        fileDescriptor.close();
        return mapped;
    }

    /*public boolean checkHasVest(Bitmap imageBitmap, boolean showLogs) {
        try {
            // Lazy init: create the interpreter on first use and keep it for reuse.
            if (tflite == null) {
                MappedByteBuffer model = loadModel("model.tflite");
                tflite = new Interpreter(model, new Interpreter.Options());
            }

            // Preprocess the Bitmap to the input tensor your model expects.
            TensorImage inputImage = preprocessBitmapForModel(imageBitmap);
            ByteBuffer inputBuffer = inputImage.getBuffer();

            // Prepare output buffer for first output tensor
            int[] outShape = tflite.getOutputTensor(0).shape();
            int elements = 1;
            for (int d : outShape) elements *= d;
            DataType outType = tflite.getOutputTensor(0).dataType();

            ByteBuffer outBuffer;
            if (outType == DataType.FLOAT32) {
                outBuffer = ByteBuffer.allocateDirect(elements * 4).order(ByteOrder.nativeOrder());
            } else if (outType == DataType.UINT8 || outType == DataType.BOOL) {
                outBuffer = ByteBuffer.allocateDirect(elements).order(ByteOrder.nativeOrder());
            } else {
                throw new IllegalStateException("Unsupported output dtype: " + outType);
            }

            // Run inference
            tflite.run(inputBuffer, outBuffer);

            // Interpret first element as boolean
            boolean hasVest;
            outBuffer.rewind();
            if (outType == DataType.FLOAT32) {
                FloatBuffer fb = outBuffer.asFloatBuffer();
                float v = fb.get(0);
                hasVest = v > 0.5f;
            } else if (outType == DataType.UINT8) {
                int v = outBuffer.get(0) & 0xFF;
                hasVest = v > 127;
            } else { // BOOL
                hasVest = (outBuffer.get(0) != 0);
            }

            if (showLogs) {
                Logger.info("checkHasVest", "imageWidth=" + (imageBitmap != null ? imageBitmap.getWidth() : -1) + ", result=" + hasVest);
            }
            return hasVest;
        } catch (Exception e) {
            Logger.error("TFLite inference failed", e.getMessage(), e);
            return false;
        }
    }*/

    // Overload that accepts the image as a base64/string as required by the model.
    public boolean checkHasVest(String imageString, boolean showLogs) {
        try {
            if (tflite == null) {
                MappedByteBuffer model = loadModel("model.tflite");
                tflite = new Interpreter(model, new Interpreter.Options());
            }

            int[] inShape = tflite.getInputTensor(0).shape();
            DataType inType = tflite.getInputTensor(0).dataType();

            if (inType != DataType.STRING) {
                Logger.error("checkHasVest", "Model input[0] is not STRING; got " + inType);
                return false;
            }

            // Build a STRING tensor with a single element containing the image string
            TensorBuffer input = TensorBuffer.createFixedSize(inShape, DataType.STRING);
            input.loadArray(new String[] { imageString }, inShape);

            // Prepare output buffer for first output tensor (same as bitmap path)
            int[] outShape = tflite.getOutputTensor(0).shape();
            int elements = 1; for (int d : outShape) elements *= d;
            DataType outType = tflite.getOutputTensor(0).dataType();

            ByteBuffer outBuffer;
            if (outType == DataType.FLOAT32) {
                outBuffer = ByteBuffer.allocateDirect(elements * 4).order(ByteOrder.nativeOrder());
            } else if (outType == DataType.UINT8 || outType == DataType.BOOL) {
                outBuffer = ByteBuffer.allocateDirect(elements).order(ByteOrder.nativeOrder());
            } else {
                Logger.error("checkHasVest", "Unsupported output dtype: " + outType);
                return false;
            }

            // Run inference with STRING input buffer
            tflite.run(input.getBuffer(), outBuffer);

            boolean hasVest;
            outBuffer.rewind();
            if (outType == DataType.FLOAT32) {
                FloatBuffer fb = outBuffer.asFloatBuffer();
                float v = fb.get(0);
                hasVest = v > 0.5f;
            } else if (outType == DataType.UINT8) {
                int v = outBuffer.get(0) & 0xFF;
                hasVest = v > 127;
            } else { // BOOL
                hasVest = (outBuffer.get(0) != 0);
            }

            if (showLogs) {
                Logger.info("checkHasVest", "STRING input length=" + (imageString != null ? imageString.length() : 0) + ", result=" + hasVest);
            }
            return hasVest;
        } catch (Exception e) {
            Logger.error("TFLite inference failed (string)", e.getMessage(), e);
            return false;
        }
    }

    // Resize and normalize the Bitmap according to input[0] tensor shape and dtype.
    // Uses TFLite Support library for concise, reliable preprocessing.
    private TensorImage preprocessBitmapForModel(Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("Bitmap is null");
        }

        int[] inputShape = tflite.getInputTensor(0).shape(); // e.g., [1, height, width, 3]
        if (inputShape.length < 4) {
            throw new IllegalStateException("Unexpected input tensor shape");
        }
        int targetHeight = inputShape[1];
        int targetWidth = inputShape[2];
        DataType inputType = tflite.getInputTensor(0).dataType();

        TensorImage tensorImage = new TensorImage(inputType);
        tensorImage.load(bitmap);

        ImageProcessor.Builder processorBuilder = new ImageProcessor.Builder()
            .add(new ResizeOp(targetHeight, targetWidth, ResizeOp.ResizeMethod.BILINEAR));

        // If the model expects float32, normalize pixel values from [0,255] to [0,1].
        if (inputType == DataType.FLOAT32) {
            processorBuilder.add(new NormalizeOp(0f, 255f));
        }

        ImageProcessor imageProcessor = processorBuilder.build();
        return imageProcessor.process(tensorImage);
    }
}
