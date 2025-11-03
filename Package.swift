// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CreateCapacitorPlugin",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CreateCapacitorPlugin",
            targets: ["vestvalidatorPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "vestvalidatorPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/vestvalidatorPlugin",
            resources: [
                .process("Resources")
            ]),
        .testTarget(
            name: "vestvalidatorPluginTests",
            dependencies: ["vestvalidatorPlugin"],
            path: "ios/Tests/vestvalidatorPluginTests")
    ]
)