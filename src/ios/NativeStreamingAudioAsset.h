//
//
//  NativeStreamingAudioAsset.h
//  NativeStreamingAudioAsset
//
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVPlayer.h>
#import <AVFoundation/AVPlayerItem.h>

typedef void (^CompleteCallback)(NSString*);

@interface NativeStreamingAudioAsset {
    NSMutableArray* voices;
    int playIndex;
    NSString* audioId;
    CompleteCallback finished;
    NSNumber *initialVolume;
    NSNumber *fadeDelay;
}

- (id) initWithPath:(NSString*) path withVoices:(NSNumber*) numVoices withVolume:(NSNumber*) volume withFadeDelay:(NSNumber *)delay;
- (void) play;
- (void) playWithFade;
- (void) playAtTime:(NSNumber*) time; // Added
- (void) pause; // Added
- (void) stop;
- (void) stopWithFade;
- (void) loop;
- (void) unload;
- (void) setVolume:(NSNumber*) volume;
- (void) setCallbackAndId:(CompleteCallback)cb audioId:(NSString*)audioId;
- (void) itemDidFinishPlaying:(NSNotification *) notification;
@end
