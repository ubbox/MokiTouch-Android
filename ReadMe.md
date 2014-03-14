MokiTouch
=========
**Important: An active subscription to [Tracker](https://www.mokimanage.com/) to connect Tracker services.**

MokiTouch is a kiosk application developed by MokiMobility to demonstrate the power of our MokiManage SDK. Our Application Settings Management tools are used to provide a variety of kiosk customization to users across multiple accounts. Our support feature set is also included. All settings can be managed remotely from our web interface on a per device or per device group basis. This is a brief overview on how MokiManage is integrated with MokiTouch and to show how to easily add MokiManage into any app.

AndroidManifest.xml
-------------------
The MokiManageSDK requires some broadcast receivers, services, and permissions to work correctly. These are all defined in the AndroidManifest.xml file within MokiTouch with a few additions specific to MokiTouch.  The full list of required broadcast receivers, services, and permissions can be seen [here](http://mokimobility.github.io/MokiManageSDK/index.htm#android_aem.htm%3FTocPath%3DGetting%20Started%20with%20MokiManage%20on%20Android|_____1)
Resources
---------
`SettingsSchema.json` is a file you must include that defines the structure and defaults of all ASM options you can use to customize the settings for the application from your account on MokiManage.

Implementation
--------------
###SDK###
The MMManager class shows one way to initialize and access the MOkiManage object.  With this all that is required to access the features of the MokiManage SDK are a valid context and a call to 'MMManager.getMokiManage(Context)'.  If implementing the MokiManage SDK in this way you need to be aware that broadcast receivers will aquire a wakelock in the background unless 'MokiManage.pause()' is called, for this reason it is recommeneded that you tie your accessing of the MokiManage SDK into your application lifecycle calling 'MokiManage.pause' and 'MokiManage.resume' in your applications 'onPause()' and 'onResume()' functions like so:

    public class CustomApplication extends Application implements Application.ActivityLifecycleCallbacks {

        private final boolean enableASM = false;
        private final boolean enableAEM = true;
        private final boolean enableCompliance = false;
        private final String appKey = "<YOUR APP KEY HERE>";
        private final String appID = "<YOUR APP ID HERE>";

        ...

        public void onCreate() {
            super.onCreate();
            mmanage = MokiManage.sharedInstance(appKey, appID, context, enableASM, enableAEM, enableCompliance);
            registerActivityLifecycleCallbacks(this);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            mmanage.resume();
        }

            @Override
        public void onActivityStopped(Activity activity) {
            mmanage.pause();
        }

        public MokiManage mokiManage(){
            return mmanage;
        }
        
        ...

    }

With this implementation the MokiManage SDK is tied to your lifecycle and will shut down background processes accordingly as well as offer an easy way to access the MokiManage object.

###Push Notifications###
Inside the PlaylistActivity we register several broadcast receivers to responde to these push notifications.

    
    @Override
    protected void onResume() {
        super.onResume();
        ...
        
        IntentFilter settingsFilter = new IntentFilter(MokiASM.NOTIFICATION_SETTINGS_SAVED);
        registerReceiver(settingsSaveReceiver, settingsFilter);

        ...
    }

    protected BroadcastReceiver settingsSaveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            savedStateValid = false;
            refreshKiosk();
        }
    };

Here is the registration of a receiver that listens for the 'MokiASM.NOTIFICATION_SETTINGS_SAVED' broadcast that indicates settings have been saved. We do this to update the kiosk after the application has received new settings pushed down from the web. Along with 'MokiASM.NOTIFICATION_SETTINGS_SAVED' there are the broadcasts for 'MokiASM.NOTIFICATION_PULL_FINISHED' 'MokiASM.NOTIFICATION_PULL_FINISHED' broadcasts.

    @Override
    protected void onResume() {
        super.onResume();

        ...

        IntentFilter screenShotFilter = new IntentFilter(SCREENSHOT_INTENT);
        //A category with the apps package name needs to be added to receive this intent
        screenShotFilter.addCategory(this.getPackageName());
        registerReceiver(screenShotReceiver, screenShotFilter);

        ...

        IntentFilter followMeFilter = new IntentFilter(MokiManage.FOLLOW_ME_INTENT);
        followMeFilter.addCategory(getPackageName());
        registerReceiver(supportReceiver, followMeFilter);
        
        ...
    }

    protected BroadcastReceiver screenShotReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MokiManage.takeScreenShot(getWindow(), context);
        }
    };

    protected BroadcastReceiver supportReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MokiManage.sharedInstance().openFollowMeDialog(PlaylistActivity.this, intent);
            Log.i("support requested", "requested");
        }
    };

Here we are registering and responding to the screenshot and follow me broadcasts.  Both of these intent receivers need to have your package name added as a category to receive the broadcast.