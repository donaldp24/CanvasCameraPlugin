//
//  CanvasCamera.js
//  PhoneGap iOS Cordova Plugin to capture Camera streaming into a HTML5 Canvas or an IMG tag.
//
//  Created by Diego Araos <d@wehack.it> on 12/29/12.
//
//  MIT License

#import <Cordova/CDVPlugin.h>
#import <UIKit/UIKit.h>
#import <CoreMedia/CoreMedia.h>
#import <CoreVideo/CoreVideo.h>
#import <AVFoundation/AVFoundation.h>
#import <ImageIO/ImageIO.h>
#import <AssetsLibrary/AssetsLibrary.h>

@interface CanvasCamera : CDVPlugin <AVCaptureVideoDataOutputSampleBufferDelegate>

@property (nonatomic, strong) AVCaptureSession *session;
@property (nonatomic, strong) AVCaptureDevice *device;
@property (nonatomic, strong) AVCaptureDeviceInput *input;
@property (nonatomic, strong) AVCaptureVideoDataOutput *output;
@property (nonatomic, strong) AVCaptureStillImageOutput *stillImageOutput;


- (void)startCapture:(CDVInvokedUrlCommand *)command;
- (void)stopCapture:(CDVInvokedUrlCommand *)command;

- (void)setFlashMode:(CDVInvokedUrlCommand *)command;
- (void)setCameraPosition:(CDVInvokedUrlCommand *)command;

- (void)captureImage:(CDVInvokedUrlCommand *)command;


@end
