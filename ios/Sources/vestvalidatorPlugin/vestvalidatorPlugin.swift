import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(vestvalidatorPlugin)
public class vestvalidatorPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "vestvalidatorPlugin"
    public let jsName = "vestvalidator"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "checkHasVest", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = vestvalidator()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }

    @objc func checkHasVest(_ call: CAPPluginCall) {
        guard let imageBase64 = call.getString("image") else {
            call.reject("Missing required parameters: image (base64)")
            return
        }
        let isShowLogs = call.getBool("showLogs") ?? false
        let hasVest = implementation.checkHasVest(imageBase64: imageBase64, showLogs: isShowLogs)
        call.resolve(["hasVest": hasVest])
    }
}
