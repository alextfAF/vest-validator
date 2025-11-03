require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name = 'CreateCapacitorPlugin'
  s.version = package['version']
  s.summary = package['description']
  s.license = package['license']
  s.homepage = package['repository']['url']
  s.author = package['author']
  s.source = { :git => package['repository']['url'], :tag => s.version.to_s }
  s.source_files = 'ios/Sources/**/*.{swift,h,m,c,cc,mm,cpp}'
  s.ios.deployment_target = '14.0'
  s.dependency 'Capacitor'
  s.swift_version = '5.1'
  # TensorFlow Lite for Swift runtime; include SelectTfOps if your model uses Flex ops
  s.dependency 'TensorFlowLiteSwift', '~> 2.13.0'
  s.dependency 'TensorFlowLiteSelectTfOps', '~> 2.13.0'
  # Ensure model resources are bundled into the app when using CocoaPods
  s.resources = 'ios/Sources/vestvalidatorPlugin/Resources/**/*'
end
