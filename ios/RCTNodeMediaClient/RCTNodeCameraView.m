//
//  RCTNodeCameraView.m
//  
//
//  Created by Mingliang Chen on 2017/12/12.
//  Copyright © 2017年 NodeMedia. All rights reserved.
//

#import "RCTNodeMediaClient.h"
#import "RCTNodeCameraView.h"
#import <React/RCTUIManager.h>
#import <AVFoundation/AVFoundation.h>

@interface RCTNodeCameraView()

@property (strong,nonatomic) NodePublisher *np;

@end

@implementation RCTNodeCameraView

-(id)initWithBridge:(RCTBridge *)bridge{
    self = [super init];
    if(self) {
        _np = [[NodePublisher alloc] initWithPremium:[RCTNodeMediaClient premium]];
        [_np setNodePublisherDelegate:self];
        _autopreview = NO;
        _outputUrl = nil;
        _camera = nil;
        _audio = nil;
        _video = nil;
    }
    [self setBridge:bridge];
    return self;
}

-(void)onEventCallback:(id)sender event:(int)event msg:(NSString *)msg{
    NSDictionary *data = @{@"code": [NSNumber numberWithInt:event],  @"message": msg,
                           @"target": self.reactTag};
    [self.bridge.eventDispatcher sendInputEventWithName:@"topChange" body:data];
}

-(void)setOutputUrl:(NSString *)outputUrl {
  [_np setOutputUrl:outputUrl];
}

-(void)setAutopreview:(BOOL)autopreview {
  _autopreview = autopreview;
  if(_camera && _video && autopreview) {
    [_np startPreview];
  }
}
- (void)setCamera:(NSDictionary *)camera {
  _camera = camera;
  int cameraId = [[camera objectForKey:@"cameraId"] intValue];
  BOOL cameraFrontMirror = [[camera objectForKey:@"cameraFrontMirror"] boolValue];
  [_np setCameraPreview:self cameraId:cameraId frontMirror:cameraFrontMirror];
  if(_autopreview && _video) {
    [_np startPreview];
  }
}

- (void)setAudio:(NSDictionary *)audio {
  _audio = audio;
  int audioBitrate = [[audio objectForKey:@"bitrate"] intValue];
  int audioProfile = [[audio objectForKey:@"profile"] intValue];
  int audioSamplerate = [[audio objectForKey:@"samplerate"] intValue];
   
  if (audioSamplerate == 0){
      AVAudioSession* session = [AVAudioSession sharedInstance];
      BOOL success;
      NSError* error = nil;
      double preferredSampleRate = 48000;
      success  = [session setPreferredSampleRate:preferredSampleRate error:&error];
      if (success) {
          audioSamplerate = preferredSampleRate;
      } else {
          NSLog (@"error setting sample rate %@", error);
      }
  }
  [_np setAudioParamBitrate:audioBitrate profile:audioProfile sampleRate:audioSamplerate];
}

- (void)setVideo:(NSDictionary *)video {
  _video = video;
  int videoPreset = [[video objectForKey:@"preset"] intValue];
  int videoFPS = [[video objectForKey:@"fps"] intValue];
  int videoBitrate = [[video objectForKey:@"bitrate"] intValue];
  int videoProfile = [[video objectForKey:@"profile"] intValue];
  BOOL videoFrontMirror = [[video objectForKey:@"videoFrontMirror"] boolValue];
  [_np setVideoParamPreset:videoPreset fps:videoFPS bitrate:videoBitrate profile:videoProfile frontMirror:videoFrontMirror];
  if(_autopreview && _camera) {
    [_np startPreview];
  }
}

- (void)setDenoise:(BOOL)denoise {
  _denoise = denoise;
  [_np setDenoiseEnable:denoise];
}

- (void)setSmoothSkinLevel:(NSInteger)smoothSkinLevel {
  _smoothSkinLevel = smoothSkinLevel;
  [_np setBeautyLevel:smoothSkinLevel];
}

- (void)setFlashEnable:(BOOL)flashEnable {
  [_np setFlashEnable:flashEnable];
}

-(void)captureCurrentFrame:(float)quality{
    [_np capturePicture:^(UIImage * _Nullable image) {
        if(image){
            NSString * base64 = [UIImageJPEGRepresentation(image,quality) base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
            [self.bridge.eventDispatcher sendDeviceEventWithName:@"currentFrameUpdate" body:@{@"base64": base64}];
        }
        
     }
     ];
}
-(int)startprev {
  return [_np startPreview];
}

-(int)stopprev {
  return [_np stopPreview];
}

-(int)start {
    int res = 0;
    @try{
      res =[_np start];
    }
    @catch(NSException * ex){
        NSLog(@"%@",ex.reason);
    }
    return res;
}

-(int)stop {
  return [_np stop];
}

-(int)switchCamera {
  return [_np switchCamera];
}

@end
