import Foundation
import UIKit
import TensorFlowLite

// Core implementation for iOS. Mirrors Android class in intent: provide
// echo() for testing and checkHasVest() for model inference.
@objc public class vestvalidator: NSObject {
    // Lazily-initialized TFLite interpreter
    private var interpreter: Interpreter?

    /// Simple echo helper used by the sample.
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }

    @objc public func checkHasVest(imageBase64: String, showLogs: Bool) -> Bool {
        do {
            if interpreter == nil {
                interpreter = try loadInterpreter()
                try interpreter?.allocateTensors()
            }

            guard let interp = interpreter else { return false }

            // Validate input tensor type – expect STRING to mirror Android path
            let inputTensorInfo = try interp.input(at: 0)
            if inputTensorInfo.dataType != .string {
                if showLogs { print("Input[0] is not STRING: \(inputTensorInfo.dataType)") }
                return false
            }

            // Copy STRING input (base64 image) – TensorFlowLiteSwift handles string tensors via Data
            guard let stringData = imageBase64.data(using: .utf8) else { return false }
            try interp.copy(stringData, toInputAt: 0)

            // Inference
            try interp.invoke()

            // Read output[0] and interpret as Bool/Float/UInt8
            let outputTensor = try interp.output(at: 0)
            let outType = outputTensor.dataType
            let data = outputTensor.data

            let hasVest: Bool
            switch outType {
            case .bool:
                hasVest = data.first != 0
            case .uInt8:
                hasVest = (data.first ?? 0) > 127
            case .float32:
                let val = data.withUnsafeBytes { ptr -> Float in
                    guard let p = ptr.baseAddress?.assumingMemoryBound(to: Float.self) else { return 0 }
                    return p.pointee
                }
                hasVest = val > 0.5
            default:
                if showLogs { print("Unsupported output dtype: \(outType)") }
                return false
            }

            if showLogs { print("checkHasVest(iOS) len=\(imageBase64.count) result=\(hasVest)") }
            return hasVest
        } catch {
            if showLogs { print("TFLite inference failed: \(error)") }
            return false
        }
    }

    private func loadInterpreter() throws -> Interpreter {
        #if SWIFT_PACKAGE
        let bundle = Bundle.module
        #else
        // Fallbacks for CocoaPods resource inclusion
        let bundle = Bundle(for: type(of: self))
        #endif
        if let url = bundle.url(forResource: "model", withExtension: "tflite") {
            return try Interpreter(modelPath: url.path)
        }
        // Also try main bundle if resources were copied there
        if let path = Bundle.main.path(forResource: "model", ofType: "tflite") {
            return try Interpreter(modelPath: path)
        }
        throw NSError(domain: "vestvalidator", code: -1, userInfo: [NSLocalizedDescriptionKey: "model.tflite not found in bundle"])
    }
    }
}
