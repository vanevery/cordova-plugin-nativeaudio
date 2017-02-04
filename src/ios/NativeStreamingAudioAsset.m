//
// 
//  NativeStreamingAudioAsset.m
//  NativeStreamingAudioAsset
//
//

#import "NativeStreamingAudioAsset.h"

@implementation NativeStreamingAudioAsset

static const CGFloat FADE_STEP = 0.05;
static const CGFloat FADE_DELAY = 0.08;

//+(void) NativeStreamingAudioAsset {
//
//}

-(id) initWithPath:(NSString*) path withVoices:(NSNumber*) numVoices withVolume:(NSNumber*) volume withFadeDelay:(NSNumber *)delay
{
    self = [super init];
    if(self) {
        voices = [[NSMutableArray alloc] init];  
        
        NSURL *pathURL = [NSURL fileURLWithPath : path];
        
        for (int x = 0; x < [numVoices intValue]; x++) {
            
            // First create an AVPlayerItem
            AVPlayerItem* playerItem = [AVPlayerItem playerItemWithURL:pathURL];

            // Subscribe to the AVPlayerItem's DidPlayToEndTime notification.
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(itemDidFinishPlaying:) name:AVPlayerItemDidPlayToEndTimeNotification object:playerItem];
            
            AVPlayer *player = [[AVPlayer alloc] initWithPlayerItem:playerItem];
            player.volume = volume.floatValue;
            
            [voices addObject:player];
            
            if(delay)
            {
                fadeDelay = delay;
            }
            else {
                fadeDelay = [NSNumber numberWithFloat:FADE_DELAY];
            }
            
            initialVolume = volume;
        }
        
        playIndex = 0;
    }
    return(self);
}

- (void) play
{
    AVPlayer * player = [voices objectAtIndex:playIndex];
    //[player setCurrentTime:0.0]; // Added
    //player.numberOfLoops = 0;
    [player play];
    playIndex += 1;
    playIndex = playIndex % [voices count];
}

- (void) playAtTime:(NSNumber*) time; // Added
{
    // Playing normal for now
    
    
    AVPlayer * player = [voices objectAtIndex:playIndex];
    //[player setCurrentTime:0.0]; // Added
    //player.numberOfLoops = 0;
    [player play];
    playIndex += 1;
    playIndex = playIndex % [voices count];
    
//    AVPlayer * player = [voices objectAtIndex:playIndex];
//    NSTimeInterval interval = [time doubleValue];
//    [player setCurrentTime:interval];
//    player.numberOfLoops = 0;
//    [player play];
//    playIndex += 1;
//    playIndex = playIndex % [voices count];
}


// The volume is increased repeatedly by the fade step amount until the last step where the audio is stopped.
// The delay determines how fast the decrease happens
- (void)playWithFade
{
    // Playing normal for now

    AVPlayer * player = [voices objectAtIndex:playIndex];
    //[player setCurrentTime:0.0]; // Added
    //player.numberOfLoops = 0;
    [player play];
    playIndex += 1;
    playIndex = playIndex % [voices count];
    
//    AVAudioPlayer * player = [voices objectAtIndex:playIndex];
//    
//    if (!player.isPlaying)
//    {
//        [player setCurrentTime:0.0];
//        player.numberOfLoops = 0;
//        player.volume = 0;
//        [player play];
//        playIndex += 1;
//        playIndex = playIndex % [voices count];
//        dispatch_async(dispatch_get_main_queue(), ^{
//            [self performSelector:@selector(playWithFade) withObject:nil afterDelay:fadeDelay.floatValue];
//        });
//    }
//    else
//    {
//        if(player.volume < initialVolume.floatValue)
//        {
//            player.volume += FADE_STEP;
//            dispatch_async(dispatch_get_main_queue(), ^{
//                [self performSelector:@selector(playWithFade) withObject:nil afterDelay:fadeDelay.floatValue];
//            });
//        }
//    }
}

// Added
- (void) pause
{
    for (int x = 0; x < [voices count]; x++) {
        AVPlayer * player = [voices objectAtIndex:x];
        [player pause];
    }
}
// Finished Adding

- (void) stop
{
    // No stop method, just pause
    
    for (int x = 0; x < [voices count]; x++) {
        AVPlayer * player = [voices objectAtIndex:x];
        [player pause];
    }
}

// The volume is decreased repeatedly by the fade step amount until the volume reaches the configured level.
// The delay determines how fast the increase happens
- (void)stopWithFade
{
    // Just pause
    
    for (int x = 0; x < [voices count]; x++) {
        AVPlayer * player = [voices objectAtIndex:x];
        [player pause];
    }
    
//    BOOL shouldContinue = NO;
//    
//    for (int x = 0; x < [voices count]; x++) {
//        AVAudioPlayer * player = [voices objectAtIndex:x];
//        
//        if (player.isPlaying && player.volume > FADE_STEP) {
//            player.volume -= FADE_STEP;
//            shouldContinue = YES;
//        } else {
//            // Stop and get the sound ready for playing again
//            [player stop];
//            player.volume = initialVolume.floatValue;
//            player.currentTime = 0;
//        }
//    }
//    
//    if(shouldContinue) {
//        [self performSelector:@selector(stopWithFade) withObject:nil afterDelay:fadeDelay.floatValue];
//    }
}

- (void) loop
{
    // Just play
    AVPlayer * player = [voices objectAtIndex:playIndex];
    //[player setCurrentTime:0.0]; // Added
    //player.numberOfLoops = 0;
    [player play];
    playIndex += 1;
    playIndex = playIndex % [voices count];
    
//    [self stop];
//    AVAudioPlayer * player = [voices objectAtIndex:playIndex];
//    [player setCurrentTime:0.0];
//    player.numberOfLoops = -1;
//    [player play];
//    playIndex += 1;
//    playIndex = playIndex % [voices count];
}

- (void) unload 
{
    [self stop];
    for (int x = 0; x < [voices count]; x++) {
        AVPlayer * player = [voices objectAtIndex:x];
        player = nil;
    }
    voices = nil;
}

- (void) setVolume:(NSNumber*) volume;
{

    for (int x = 0; x < [voices count]; x++) {
        AVPlayer * player = [voices objectAtIndex:x];

        [player setVolume:volume.floatValue];
    }
}

- (void) setCallbackAndId:(CompleteCallback)cb audioId:(NSString*)aID
{
    self->audioId = aID;
    self->finished = cb;
}

-(void)itemDidFinishPlaying:(NSNotification *) notification {
    if (self->finished) {
        self->finished(self->audioId);
    }
}

@end
