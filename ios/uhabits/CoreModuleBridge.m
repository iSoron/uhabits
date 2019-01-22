#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(CoreModule, RCTEventEmitter)
RCT_EXTERN_METHOD(createHabit:(NSString)name)
//RCT_EXTERN_METHOD(updateHabit:(int)id (NSString)name)
//RCT_EXTERN_METHOD(deleteHabit:(int)id)
RCT_EXTERN_METHOD(requestHabitList)
@end
