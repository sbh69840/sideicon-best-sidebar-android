package shiv.raj.admin.sideicon;

/**
 * Created by shivraj on 15/01/2017.
 */






import java.util.LinkedList;
import java.util.List;
import java.util.Set;




import shiv.raj.admin.sideicon.constants.StandOutFlags;
import shiv.raj.admin.sideicon.ui.Window;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;

import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;


public abstract class StandOutWindow extends Service {


    static final String TAG = "StandOutWindow";

    /**
     * StandOut window id: You may use this sample id for your first window.
     */
    public static final int DEFAULT_ID = 0;

    /**
     * Special StandOut window id: You may NOT use this id for any windows.
     */
    public static final int ONGOING_NOTIFICATION_ID = -1;

    /**
     * StandOut window id: You may use this id when you want it to be
     * disregarded. The system makes no distinction for this id; it is only used
     * to improve code readability.
     */
    public static final int DISREGARD_ID = -2;

    /**
     * Intent action: Show a new window corresponding to the id.
     */
    public static final String ACTION_SHOW = "SHOW";

    /**
     * Intent action: Restore a previously hidden window corresponding to the
     * id. The window should be previously hidden with {@link #ACTION_HIDE}.
     */
    public static final String ACTION_RESTORE = "RESTORE";

    /**
     * Intent action: Close an existing window with an existing id.
     */
    public static final String ACTION_CLOSE = "CLOSE";

    /**
     * Intent action: Close all existing windows.
     */
    public static final String ACTION_CLOSE_ALL = "CLOSE_ALL";

    /**
     * Intent action: Send data to a new or existing window.
     */
    public static final String ACTION_SEND_DATA = "SEND_DATA";

    /**
     * Intent action: Hide an existing window with an existing id. To enable the
     * ability to restore this window, make sure you implement
     * {@link #getHiddenNotification(int)}.
     */
    public static final String ACTION_HIDE = "HIDE";


    public static void closeAll(Context context,
                                Class<? extends StandOutWindow> cls) {
        context.startService(getCloseAllIntent(context, cls));

    }

    /**
     * This allows windows of different applications to communicate with each
     * other.
     *
     * <p>
     * Send  data in a {@link Bundle} to a new or existing
     * windows. The implementation of the recipient window can handle what to do
     * with the data. To receive a result, provide the class and id of the
     * sender.
     *
     * @param context
     *            A Context of the application package implementing the class of
     *            the sending window.
     * @param toCls
     *            The Service's class extending {@link StandOutWindow} that is
     *            managing the receiving window.
     * @param toId
     *            The id of the receiving window, or DISREGARD_ID.
     * @param requestCode
     *            Provide a request code to declare what kind of data is being
     *            sent.
     * @param data
     *            A bundle of parceleable data to be sent to the receiving
     *            window.
     * @param fromCls
     *            Provide the class of the sending window if you want a result.
     * @param fromId
     *            Provide the id of the sending window if you want a result.
     * @see #sendData(int, Class, int, int, Bundle)
     */
    public static void sendData(Context context,
                                Class<? extends StandOutWindow> toCls, int toId, int requestCode,
                                Bundle data, Class<? extends StandOutWindow> fromCls, int fromId) {
        context.startService(getSendDataIntent(context, toCls, toId,
                requestCode, data, fromCls, fromId));
    }



    public static Intent getShowIntent(Context context,
                                       Class<? extends StandOutWindow> cls, int id) {
        boolean cached = sWindowCache.isCached(id, cls);
        String action = cached ? ACTION_RESTORE : ACTION_SHOW;
        Uri uri = cached ? Uri.parse("standout://" + cls + '/' + id) : null;
        return new Intent(context, cls).putExtra("id", id).setAction(action)
                .setData(uri);
    }






    public static Intent getCloseAllIntent(Context context,
                                           Class<? extends StandOutWindow> cls) {
        return new Intent(context, cls).setAction(ACTION_CLOSE_ALL);
    }
    public static Intent getSendDataIntent(Context context,
                                           Class<? extends StandOutWindow> toCls, int toId, int requestCode,
                                           Bundle data, Class<? extends StandOutWindow> fromCls, int fromId) {
        return new Intent(context, toCls).putExtra("id", toId)
                .putExtra("requestCode", requestCode)
                .putExtra("wei.mark.standout.data", data)
                .putExtra("wei.mark.standout.fromCls", fromCls)
                .putExtra("fromId", fromId).setAction(ACTION_SEND_DATA);
    }

    // internal map of ids to shown/hidden views
    static WindowCache sWindowCache;
    static Window sFocusedWindow;

    // static constructors
    static {
        sWindowCache = new WindowCache();
        sFocusedWindow = null;
    }

    // internal system services
    WindowManager mWindowManager;
    private NotificationManager mNotificationManager;
    LayoutInflater mLayoutInflater;

    // internal state variables
    private boolean startedForeground;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        startedForeground = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // intent should be created with
        // getShowIntent(), getHideIntent(), getCloseIntent()
        if (intent != null) {
            String action = intent.getAction();
            int id = intent.getIntExtra("id", DEFAULT_ID);

            // this will interfere with getPersistentNotification()
            if (id == ONGOING_NOTIFICATION_ID) {
                throw new RuntimeException(
                        "ID cannot equals StandOutWindow.ONGOING_NOTIFICATION_ID");
            }

            if (ACTION_SHOW.equals(action) || ACTION_RESTORE.equals(action)) {
                show(id);
            } else if (ACTION_HIDE.equals(action)) {
                hide(id);
            } else if (ACTION_CLOSE.equals(action)) {
                close(id);
            } else if (ACTION_CLOSE_ALL.equals(action)) {
                closeAll();
            } else if (ACTION_SEND_DATA.equals(action)) {
                if (!isExistingId(id) && id != DISREGARD_ID) {
                    Log.w(TAG,
                            "Sending data to non-existant window. If this is not intended, make sure toId is either an existing window's id or DISREGARD_ID.");
                }
                Bundle data = intent.getBundleExtra("wei.mark.standout.data");
                int requestCode = intent.getIntExtra("requestCode", 0);
                @SuppressWarnings("unchecked")
                Class<? extends StandOutWindow> fromCls = (Class<? extends StandOutWindow>) intent
                        .getSerializableExtra("wei.mark.standout.fromCls");
                int fromId = intent.getIntExtra("fromId", DEFAULT_ID);
                onReceiveData(id, requestCode, data, fromCls, fromId);
            }
        } else {
            Log.w(TAG, "Tried to onStartCommand() with a null intent.");
        }

        // the service is started in foreground in show()
        // so we don't expect Android to kill this service
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // closes all windows
        closeAll();
        stopSelf();
    }

    /**
     * Return the name of every window in this implementation. The name will
     * appear in the default implementations of the system window decoration
     * title and notification titles.
     *
     * @return The name.
     */
    public abstract String getAppName();
    public abstract int getAppIcon();

    public abstract void createAndAttachView(int id, FrameLayout frame);

    /**
     * Return the  for the corresponding id.
     * The system will set the layout params on the view for this StandOut
     * window. The layout params may be reused.
     *
     *
     * @param id
     *            The id of the window.
     * @param window
     *            The window corresponding to the id. Given as courtesy, so you
     *            may get the existing layout params.
     * @return The  corresponding to the id.
     *         The layout params will be set on the window. The layout params
     *         returned will be reused whenever possible, minimizing the number
     *         of times getParams() will be called.
     */
    public abstract StandOutLayoutParams getParams(int id, Window window);

    /**
     * Implement this method to change modify the behavior and appearance of the
     * window corresponding to the id.
     *
     * <p>
     * You may use any of the flags defined in {@link StandOutFlags}. This
     * method will be called many times, so keep it fast.
     *
     * <p>
     * Use bitwise OR (|) to set flags, and bitwise XOR (^) to unset flags. To
     * test if a flag is set, use {@link Utils#isSet(int, int)}.
     *
     * @param id
     *            The id of the window.
     * @return A combination of flags.
     */
    public int getFlags(int id) {
        return 0;
    }

    /**
     * Implement this method to set a custom title for the window corresponding
     * to the id.
     *
     * @param id
     *            The id of the window.
     * @return The title of the window.
     */
    public String getTitle(int id) {
        return getAppName();
    }

    /**
     * Implement this method to set a custom icon for the window corresponding
     * to the id.
     *
     * @param id
     *            The id of the window.
     * @return The icon of the window.
     */


    /**
     * Return the title for the persistent notification. This is called every
     * time {@link #show(int)} is called.
     *
     * @param id
     *            The id of the window shown.
     * @return The title for the persistent notification.
     */
    public String getPersistentNotificationTitle(int id) {
        return getAppName() + " Running";
    }

    /**
     * Return the message for the persistent notification. This is called every
     * time {@link #show(int)} is called.
     *
     * @param id
     *            The id of the window shown.
     * @return The message for the persistent notification.
     */
    public String getPersistentNotificationMessage(int id) {
        return "";
    }

    /**
     * Return the intent for the persistent notification. This is called every
     * time {@link #show(int)} is called.
     *
     * <p>
     * The returned intent will be packaged into a {@link PendingIntent} to be
     * invoked when the user clicks the notification.
     *
     * @param id
     *            The id of the window shown.
     * @return The intent for the persistent notification.
     */
    public Intent getPersistentNotificationIntent(int id) {
        return null;
    }

    /**
     * Return the icon resource for every hidden window in this implementation.
     * The icon will appear in the default implementations of the hidden
     * notifications.
     *
     * @return The icon.
     */
    public int getHiddenIcon() {
        return getAppIcon();
    }

    /**
     * Return the title for the hidden notification corresponding to the window
     * being hidden.
     *
     * @param id
     *            The id of the hidden window.
     * @return The title for the hidden notification.
     */
    public String getHiddenNotificationTitle(int id) {
        return getAppName() + " Hidden";
    }

    /**
     * Return the message for the hidden notification corresponding to the
     * window being hidden.
     *
     * @param id
     *            The id of the hidden window.
     * @return The message for the hidden notification.
     */
    public String getHiddenNotificationMessage(int id) {
        return "";
    }


    public Intent getHiddenNotificationIntent(int id) {
        return null;
    }


    public Notification getPersistentNotification(int id) {
        // basic notification stuff
        // http://developer.android.com/guide/topics/ui/notifiers/notifications.html
        int icon = getAppIcon();
        long when = System.currentTimeMillis();
        Context c = getApplicationContext();
        String contentTitle = getPersistentNotificationTitle(id);
        String contentText = getPersistentNotificationMessage(id);
        String tickerText = String.format("%s: %s", contentTitle, contentText);

        // getPersistentNotification() is called for every new window
        // so we replace the old notification with a new one that has
        // a bigger id
        Intent notificationIntent = getPersistentNotificationIntent(id);

        PendingIntent contentIntent = null;

        if (notificationIntent != null) {
            contentIntent = PendingIntent.getService(this, 0,
                    notificationIntent,
                    // flag updates existing persistent notification
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return new Notification.Builder(c).setContentIntent(contentIntent).setAutoCancel(false).setContentTitle(contentTitle).setContentText(contentText).setWhen(when).setSmallIcon(icon).setTicker(tickerText).build();
    }


    public Notification getHiddenNotification(int id) {
        // same basics as getPersistentNotification()
        int icon = getHiddenIcon();
        long when = System.currentTimeMillis();
        Context c = getApplicationContext();
        String contentTitle = getHiddenNotificationTitle(id);
        String contentText = getHiddenNotificationMessage(id);
        String tickerText = String.format("%s: %s", contentTitle, contentText);

        // the difference here is we are providing the same id
        Intent notificationIntent = getHiddenNotificationIntent(id);

        PendingIntent contentIntent = null;

        if (notificationIntent != null) {
            contentIntent = PendingIntent.getService(this, 0,
                    notificationIntent,
                    // flag updates existing persistent notification
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return new Notification.Builder(c).setContentIntent(contentIntent).setAutoCancel(false).setSmallIcon(icon).setTicker(tickerText).setContentTitle(contentTitle).setContentText(contentText).setSmallIcon(icon).setWhen(when).build();
    }


    public Animation getShowAnimation(int id) {
        return AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
    }


    public Animation getHideAnimation(int id) {
        return AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
    }


    public Animation getCloseAnimation(int id) {
        return AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
    }


    public int getThemeStyle() {
        return 0;
    }

    public void getDropDown(final int id) {
        final List<DropDownListItem> items;

        items = getDropDownItems(id);

for(DropDownListItem item:items) {
    item.action.run();
}






    }

    /**
     * Implement this method to populate the drop down menu when the user clicks
     * on the icon of the window corresponding to the id. The icon is only shown
     * when {@link StandOutFlags#FLAG_DECORATION_SYSTEM} is set.
     *
     * @param id
     *            The id of the window.
     * @return The list of items to show in the drop down menu, or null or empty
     *         to have no dropdown menu.
     */
    public List<DropDownListItem> getDropDownItems(int id) {
        return null;
    }

    /**
     * Implement this method to be alerted to touch events in the body of the
     * window corresponding to the id.
     *
     * <p>
     * Note that even if you set , you will not
     * receive touch events from the system window decorations.
     *
     * @see {@link View.OnTouchListener#onTouch(View, MotionEvent)}
     * @param id
     *            The id of the view, provided as a courtesy.
     * @param window
     *            The window corresponding to the id, provided as a courtesy.
     * @param view
     *            The view where the event originated from.
     * @param event
     *            See linked method.
     */
    public boolean onTouchBody(int id, Window window, View view,
                               MotionEvent event) {
        return false;
    }

    /**
     * Implement this method to be alerted to when the window corresponding to
     * the id is moved.
     *
     * @param id
     *            The id of the view, provided as a courtesy.
     * @param window
     *            The window corresponding to the id, provided as a courtesy.
     * @param view
     *            The view where the event originated from.
     * @param event
     *            See linked method.
     * @see {@link #onTouchHandleMove(int, Window, View, MotionEvent)}
     */
    public void onMove(int id, Window window, View view, MotionEvent event) {
    }

    /**
     * Implement this method to be alerted to when the window corresponding to
     * the id is resized.
     *
     * @param id
     *            The id of the view, provided as a courtesy.
     * @param window
     *            The window corresponding to the id, provided as a courtesy.
     * @param view
     *            The view where the event originated from.
     * @param event
     *            See linked method.
     * @see {@link #onTouchHandleResize(int, Window, View, MotionEvent)}
     */
    public void onResize(int id, Window window, View view, MotionEvent event) {
    }

    /**
     * Implement this callback to be alerted when a window corresponding to the
     * id is about to be shown. This callback will occur before the view is
     * added to the window manager.
     *
     * @param id
     *            The id of the view, provided as a courtesy.
     * @param window
     *            The view about to be shown.
     * @return Return true to cancel the view from being shown, or false to
     *         continue.
     * @see #show(int)
     */
    public boolean onShow(int id, Window window) {
        return false;
    }

    /**
     * Implement this callback to be alerted when a window corresponding to the
     * id is about to be hidden. This callback will occur before the view is
     * removed from the window manager and {@link #getHiddenNotification(int)}
     * is called.
     *
     * @param id
     *            The id of the view, provided as a courtesy.
     * @param window
     *            The view about to be hidden.
     * @return Return true to cancel the view from being hidden, or false to
     *         continue.
     * @see #hide(int)
     */
    public boolean onHide(int id, Window window) {
        return false;
    }

    /**
     * Implement this callback to be alerted when a window corresponding to the
     * id is about to be closed. This callback will occur before the view is
     * removed from the window manager.
     *
     * @param id
     *            The id of the view, provided as a courtesy.
     * @param window
     *            The view about to be closed.
     * @return Return true to cancel the view from being closed, or false to
     *         continue.
     * @see #close(int)
     */
    public boolean onClose(int id, Window window) {
        return false;
    }

    /**
     * Implement this callback to be alerted when all windows are about to be
     * closed. This callback will occur before any views are removed from the
     * window manager.
     *
     * @return Return true to cancel the views from being closed, or false to
     *         continue.
     * @see #closeAll()
     */
    public boolean onCloseAll() {
        return false;
    }

    /**
     * Implement this callback to be alerted when a window corresponding to the
     * id has received some data. The sender is described by fromCls and fromId
     * if the sender wants a result. To send a result, use
     * {@link #sendData(int, Class, int, int, Bundle)}.
     *
     * @param id
     *            The id of your receiving window.
     * @param requestCode
     *            The sending window provided this request code to declare what
     *            kind of data is being sent.
     * @param data
     *            A bundle of parceleable data that was sent to your receiving
     *            window.
     * @param fromCls
     *            The sending window's class. Provided if the sender wants a
     *            result.
     * @param fromId
     *            The sending window's id. Provided if the sender wants a
     *            result.
     */
    public void onReceiveData(int id, int requestCode, Bundle data,
                              Class<? extends StandOutWindow> fromCls, int fromId) {
    }

    /**
     * Implement this callback to be alerted when a window corresponding to the
     * id is about to be updated in the layout. This callback will occur before
     * the view is updated by the window manager.
     *
     * @param id
     *            The id of the window, provided as a courtesy.
     * @param window
     *            The window about to be updated.
     * @param params
     *            The updated layout params.
     * @return Return true to cancel the window from being updated, or false to
     *         continue.
     * @see #updateViewLayout(int, StandOutLayoutParams)
     */
    public boolean onUpdate(int id, Window window, StandOutLayoutParams params) {
        return false;
    }

    /**
     * Implement this callback to be alerted when a window corresponding to the
     * id is about to be bought to the front. This callback will occur before
     * the window is brought to the front by the window manager.
     *
     * @param id
     *            The id of the window, provided as a courtesy.
     * @param window
     *            The window about to be brought to the front.
     * @return Return true to cancel the window from being brought to the front,
     *         or false to continue.
     * @see #bringToFront(int)
     */
    public boolean onBringToFront(int id, Window window) {
        return false;
    }

    /**
     * Implement this callback to be alerted when a window corresponding to the
     * id is about to have its focus changed. This callback will occur before
     * the window's focus is changed.
     *
     * @param id
     *            The id of the window, provided as a courtesy.
     * @param window
     *            The window about to be brought to the front.
     * @param focus
     *            Whether the window is gaining or losing focus.
     * @return Return true to cancel the window's focus from being changed, or
     *         false to continue.
     * @see #focus(int)
     */
    public boolean onFocusChange(int id, Window window, boolean focus) {
        return false;
    }

    /**
     * Implement this callback to be alerted when a window corresponding to the
     * id receives a key event. This callback will occur before the window
     * handles the event with {@link Window#dispatchKeyEvent(KeyEvent)}.
     *
     * @param id
     *            The id of the window, provided as a courtesy.
     * @param window
     *            The window about to receive the key event.
     * @param event
     *            The key event.
     * @return Return true to cancel the window from handling the key event, or
     *         false to let the window handle the key event.
     * @see {@link Window#dispatchKeyEvent(KeyEvent)}
     */
    public boolean onKeyEvent(int id, Window window, KeyEvent event) {
        return false;
    }

    /**
     * Show or restore a window corresponding to the id. Return the window that
     * was shown/restored.
     *
     * @param id
     *            The id of the window.
     * @return The window shown.
     */
    public final synchronized Window show(int id) {
        // get the window corresponding to the id
        Window cachedWindow = getWindow(id);
        final Window window;

        // check cache first
        if (cachedWindow != null) {
            window = cachedWindow;
        } else {
            window = new Window(this, id);
        }

        // alert callbacks and cancel if instructed
        if (onShow(id, window)) {
            Log.d(TAG, "Window " + id + " show cancelled by implementation.");
            return null;
        }

        // focus an already shown window
        if (window.visibility == Window.VISIBILITY_VISIBLE) {
            Log.d(TAG, "Window " + id + " is already shown.");
            focus(id);
            return window;
        }

        window.visibility = Window.VISIBILITY_VISIBLE;

        // get animation
        Animation animation = getShowAnimation(id);

        // get the params corresponding to the id
        StandOutLayoutParams params = window.getLayoutParams();

        try {
            // add the view to the window manager
            mWindowManager.addView(window, params);

            // animate
            if (animation != null) {
                window.getChildAt(0).startAnimation(animation);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // add view to internal map
        sWindowCache.putCache(id, getClass(),window);

        // get the persistent notification
        Notification notification = getPersistentNotification(id);

        // show the notification
        if (notification != null) {
            notification.flags = notification.flags
                    | Notification.FLAG_NO_CLEAR;

            // only show notification if not shown before
            if (!startedForeground) {
                // tell Android system to show notification
                startForeground(
                        getClass().hashCode() + ONGOING_NOTIFICATION_ID,
                        notification);
                startedForeground = true;
            } else {
                // update notification if shown before
                mNotificationManager.notify(getClass().hashCode()
                        + ONGOING_NOTIFICATION_ID, notification);
            }
        } else {
            // notification can only be null if it was provided before
            if (!startedForeground) {
                throw new RuntimeException("Your StandOutWindow service must"
                        + "provide a persistent notification."
                        + "The notification prevents Android"
                        + "from killing your service in low"
                        + "memory situations.");
            }
        }

        focus(id);

        return window;
    }

    /**
     * Hide a window corresponding to the id. Show a notification for the hidden
     * window.
     *
     * @param id
     *            The id of the window.
     */
    public final synchronized void hide(int id) {
        // get the view corresponding to the id
        final Window window = getWindow(id);

        if (window == null) {
            throw new IllegalArgumentException("Tried to hide(" + id
                    + ") a null window.");
        }

        // alert callbacks and cancel if instructed
        if (onHide(id, window)) {
            Log.d(TAG, "Window " + id + " hide cancelled by implementation.");
            return;
        }

        // ignore if window is already hidden
        if (window.visibility == Window.VISIBILITY_GONE) {
            Log.d(TAG, "Window " + id + " is already hidden.");
        }

        // check if hide enabled
        if (Utils.isSet(window.flags, StandOutFlags.FLAG_WINDOW_HIDE_ENABLE)) {
            window.visibility = Window.VISIBILITY_TRANSITION;

            // get the hidden notification for this view
            Notification notification = getHiddenNotification(id);

            // get animation
            Animation animation = getHideAnimation(id);

            try {
                // animate
                if (animation != null) {
                    animation.setAnimationListener(new AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            // remove the window from the window manager
                            mWindowManager.removeView(window);
                            window.visibility = Window.VISIBILITY_GONE;
                        }
                    });
                    window.getChildAt(0).startAnimation(animation);
                } else {
                    // remove the window from the window manager
                    mWindowManager.removeView(window);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // display the notification
            notification.flags = notification.flags
                    | Notification.FLAG_NO_CLEAR
                    | Notification.FLAG_AUTO_CANCEL;

            mNotificationManager.notify(getClass().hashCode() + id,
                    notification);

        } else {
            // if hide not enabled, close window
            close(id);
        }
    }

    /**
     * Close a window corresponding to the id.
     *
     * @param id
     *            The id of the window.
     */
    public final synchronized void close(final int id) {
        // get the view corresponding to the id
        final Window window = getWindow(id);

        if (window == null) {
            throw new IllegalArgumentException("Tried to close(" + id
                    + ") a null window.");
        }

        if (window.visibility == Window.VISIBILITY_TRANSITION) {
            return;
        }

        // alert callbacks and cancel if instructed
        if (onClose(id, window)) {
            Log.w(TAG, "Window " + id + " close cancelled by implementation.");
            return;
        }

        // remove hidden notification
        mNotificationManager.cancel(getClass().hashCode() + id);

        unfocus(window);

        window.visibility = Window.VISIBILITY_TRANSITION;

        // get animation
        Animation animation = getCloseAnimation(id);

        // remove window
        try {
            // animate
            if (animation != null) {
                animation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // remove the window from the window manager
                        mWindowManager.removeView(window);
                        window.visibility = Window.VISIBILITY_GONE;

                        // remove view from internal map
                        sWindowCache.removeCache(id,
                                StandOutWindow.this.getClass());

                        // if we just released the last window, quit
                        if (getExistingIds().size() == 0) {
                            // tell Android to remove the persistent
                            // notification
                            // the Service will be shutdown by the system on low
                            // memory
                            startedForeground = false;
                            stopForeground(true);
                        }
                    }
                });
                window.getChildAt(0).startAnimation(animation);
            } else {
                // remove the window from the window manager
                mWindowManager.removeView(window);

                // remove view from internal map
                sWindowCache.removeCache(id, getClass());

                // if we just released the last window, quit
                if (sWindowCache.getCacheSize(getClass()) == 0) {
                    // tell Android to remove the persistent notification
                    // the Service will be shutdown by the system on low memory
                    startedForeground = false;
                    stopForeground(true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Close all existing windows.
     */
    public final synchronized void closeAll() {
        // alert callbacks and cancel if instructed
        if (onCloseAll()) {
            Log.w(TAG, "Windows close all cancelled by implementation.");
            return;
        }

        // add ids to temporary set to avoid concurrent modification
        LinkedList<Integer> ids = new LinkedList<>();
        for (int id : getExistingIds()) {
            ids.add(id);
        }

        // close each window
        for (int id : ids) {
            close(id);
        }
    }

    /**
     * Send  data in a {@link Bundle} to a new or existing
     * windows. The implementation of the recipient window can handle what to do
     * with the data. To receive a result, provide the id of the sender.
     *
     * @param fromId
     *            Provide the id of the sending window if you want a result.
     * @param toCls
     *            The Service's class extending {@link StandOutWindow} that is
     *            managing the receiving window.
     * @param toId
     *            The id of the receiving window.
     * @param requestCode
     *            Provide a request code to declare what kind of data is being
     *            sent.
     * @param data
     *            A bundle of parceleable data to be sent to the receiving
     *            window.
     */
    public final void sendData(int fromId,
                               Class<? extends StandOutWindow> toCls, int toId, int requestCode,
                               Bundle data) {
        StandOutWindow.sendData(this, toCls, toId, requestCode, data,
                getClass(), fromId);
    }

    /**
     * Bring the window corresponding to this id in front of all other windows.
     * The window may flicker as it is removed and restored by the system.
     *
     * @param id
     *            The id of the window to bring to the front.
     */
    public final synchronized void bringToFront(int id) {
        Window window = getWindow(id);
        if (window == null) {
            throw new IllegalArgumentException("Tried to bringToFront(" + id
                    + ") a null window.");
        }

        if (window.visibility == Window.VISIBILITY_GONE) {
            throw new IllegalStateException("Tried to bringToFront(" + id
                    + ") a window that is not shown.");
        }

        if (window.visibility == Window.VISIBILITY_TRANSITION) {
            return;
        }

        // alert callbacks and cancel if instructed
        if (onBringToFront(id, window)) {
            Log.w(TAG, "Window " + id
                    + " bring to front cancelled by implementation.");
            return;
        }

        StandOutLayoutParams params = window.getLayoutParams();

        // remove from window manager then add back
        try {
            mWindowManager.removeView(window);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            mWindowManager.addView(window, params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Request focus for the window corresponding to this id. A maximum of one
     * window can have focus, and that window will receive all key events,
     * including Back and Menu.
     *
     * @param id
     *            The id of the window.
     * @return True if focus changed successfully, false if it failed.
     */
    public final synchronized boolean focus(int id) {
        // check if that window is focusable
        final Window window = getWindow(id);
        if (window == null) {
            throw new IllegalArgumentException("Tried to focus(" + id
                    + ") a null window.");
        }

        if (!Utils.isSet(window.flags,
                StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE)) {
            // remove focus from previously focused window
            if (sFocusedWindow != null) {
                unfocus(sFocusedWindow);
            }

            return window.onFocus(true);
        }

        return false;
    }

    /**
     * Remove focus for the window corresponding to this id. Once a window is
     * unfocused, it will stop receiving key events.
     *
     * @param id
     *            The id of the window.
     * @return True if focus changed successfully, false if it failed.
     */
    public final synchronized boolean unfocus(int id) {
        Window window = getWindow(id);
        return unfocus(window);
    }


    public final boolean isExistingId(int id) {
        return sWindowCache.isCached(id, getClass());
    }

    /**
     * Return the ids of all shown or hidden windows.
     *
     * @return A set of ids, or an empty set.
     */
    public final Set<Integer> getExistingIds() {
        return sWindowCache.getCacheIds(getClass());
    }

    /**
     * Return the window corresponding to the id, if it exists in cache. The
     * window will not be created with
     * {@link #createAndAttachView(int, FrameLayout)} . This means the returned
     * value will be null if the window is not shown or hidden.
     *
     * @param id
     *            The id of the window.
     * @return The window if it is shown/hidden, or null if it is closed.
     */
    public final Window getWindow(int id) {
        return sWindowCache.getCache(id, getClass());
    }

    /**
     * Return the window that currently has focus.
     *
     * @return The window that has focus.
     */
    public final Window getFocusedWindow() {
        return sFocusedWindow;
    }

    /**
     * Sets the window that currently has focus.
     */
    public final void setFocusedWindow(Window window) {
        sFocusedWindow = window;
    }



    /**
     * Internal touch handler for handling moving the window.
     *
     * @see {@link View#onTouchEvent(MotionEvent)}
     *

     */
    public boolean onTouchHandleMove(int id, Window window, View view,
                                     MotionEvent event) {
        StandOutLayoutParams params = window.getLayoutParams();

        // how much you have to move in either direction in order for the
        // gesture to be a move and not tap

        int totalDeltaX = window.touchInfo.lastX - window.touchInfo.firstX;
        int totalDeltaY = window.touchInfo.lastY - window.touchInfo.firstY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();

                window.touchInfo.firstX = window.touchInfo.lastX;
                window.touchInfo.firstY = window.touchInfo.lastY;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) event.getRawX() - window.touchInfo.lastX;
                int deltaY = (int) event.getRawY() - window.touchInfo.lastY;

                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();

                if (window.touchInfo.moving
                        || Math.abs(totalDeltaX) >= params.threshold
                        || Math.abs(totalDeltaY) >= params.threshold) {
                    window.touchInfo.moving = true;

                    // if window is moveable
                    if (Utils.isSet(window.flags,
                            StandOutFlags.FLAG_BODY_MOVE_ENABLE)) {

                        // update the position of the window
                        if (event.getPointerCount() == 1) {
                            params.x += deltaX;
                            params.y += deltaY;
                        }

                        window.edit().setPosition(params.x, params.y).commit();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                window.touchInfo.moving = false;

                if (event.getPointerCount() == 1) {

                    // bring to front on tap
                    boolean tap = Math.abs(totalDeltaX) < params.threshold
                            && Math.abs(totalDeltaY) < params.threshold;
                    if (tap
                            && Utils.isSet(
                            window.flags,
                            StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP)) {
                        StandOutWindow.this.bringToFront(id);
                    }
                }

                // bring to front on touch
                else if (Utils.isSet(window.flags,
                        StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TOUCH)) {
                    StandOutWindow.this.bringToFront(id);
                }

                break;
        }

        onMove(id, window, view, event);

        return true;
    }

    /**
     * Internal touch handler for handling resizing the window.
     *
     * @see {@link View#onTouchEvent(MotionEvent)}
     *

     */
    public boolean onTouchHandleResize(int id, Window window, View view,
                                       MotionEvent event) {
        StandOutLayoutParams params =  window
                .getLayoutParams();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();

                window.touchInfo.firstX = window.touchInfo.lastX;
                window.touchInfo.firstY = window.touchInfo.lastY;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) event.getRawX() - window.touchInfo.lastX;
                int deltaY = (int) event.getRawY() - window.touchInfo.lastY;

                // update the size of the window
                params.width += deltaX;
                params.height += deltaY;

                // keep window between min/max width/height
                if (params.width >= params.minWidth
                        && params.width <= params.maxWidth) {
                    window.touchInfo.lastX = (int) event.getRawX();
                }

                if (params.height >= params.minHeight
                        && params.height <= params.maxHeight) {
                    window.touchInfo.lastY = (int) event.getRawY();
                }

                window.edit().setSize(params.width, params.height).commit();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        onResize(id, window, view, event);

        return true;
    }

    /**
     * Remove focus for the window, which could belong to another application.
     * Since we don't allow windows from different applications to directly
     * interact with each other, except for
     * {@link #sendData(Context, Class, int, int, Bundle, Class, int)}, this
     * method is private.
     *
     * @param window
     *            The window to unfocus.
     * @return True if focus changed successfully, false if it failed.
     */
    public synchronized boolean unfocus(Window window) {
        if (window == null) {
            throw new IllegalArgumentException(
                    "Tried to unfocus a null window.");
        }
        return window.onFocus(false);
    }

    /**
     * Update the window corresponding to this id with the given params.
     *
     * @param id
     *            The id of the window.
     * @param params
     *            The updated layout params to apply.
     */
    public void updateViewLayout(int id, StandOutLayoutParams params) {
        Window window = getWindow(id);

        if (window == null) {
            throw new IllegalArgumentException("Tried to updateViewLayout("
                    + id + ") a null window.");
        }

        if (window.visibility == Window.VISIBILITY_GONE) {
            return;
        }

        if (window.visibility == Window.VISIBILITY_TRANSITION) {
            return;
        }

        // alert callbacks and cancel if instructed
        if (onUpdate(id, window, params)) {
            Log.w(TAG, "Window " + id + " update cancelled by implementation.");
            return;
        }

        try {
            window.setLayoutParams(params);
            mWindowManager.updateViewLayout(window, params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * LayoutParams specific to floating StandOut windows.
     *
     * @author Mark Wei <markwei@gmail.com>
     *
     */
    public class StandOutLayoutParams extends WindowManager.LayoutParams {
        /**
         * Special value for x position that represents the left of the screen.
         */
        public static final int LEFT = 0;
        /**
         * Special value for y position that represents the top of the screen.
         */
        public static final int TOP = 0;
        /**
         * Special value for x position that represents the right of the screen.
         */
        static final int RIGHT = Integer.MAX_VALUE;
        /**
         * Special value for y position that represents the bottom of the
         * screen.
         */
        static final int BOTTOM = Integer.MAX_VALUE;
        /**
         * Special value for x or y position that represents the center of the
         * screen.
         */
        static final int CENTER = Integer.MIN_VALUE;
        /**
         * Special value for x or y position which requests that the system
         * determine the position.
         */
        static final int AUTO_POSITION = Integer.MIN_VALUE + 1;

        /**
         * The distance that distinguishes a tap from a drag.
         */
        int threshold;

        /**
         * Optional constraints of the window.
         */
        public int minWidth, minHeight, maxWidth, maxHeight;

        /**
         * @param id
         *            The id of the window.
         */
        StandOutLayoutParams(int id) {
            super(200, 200, TYPE_PHONE,
                    StandOutLayoutParams.FLAG_NOT_TOUCH_MODAL
                            | StandOutLayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);

            int windowFlags = getFlags(id);

            setFocusFlag(false);

            if (!Utils.isSet(windowFlags,
                    StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE)) {
                // windows may be moved beyond edges
                flags |= FLAG_LAYOUT_NO_LIMITS;
            }

            x = getX(id, width);
            y = getY(id, height);

           gravity = Gravity.TOP | Gravity.START;

            threshold = 10;
            minWidth = minHeight = 0;
            maxWidth = maxHeight = Integer.MAX_VALUE;
        }

        /**
         * @param id
         *            The id of the window.
         * @param w
         *            The width of the window.
         * @param h
         *            The height of the window.
         */
        StandOutLayoutParams(int id, int w, int h) {
            this(id);
            width = w;
            height = h;
        }

        /**
         * @param id
         *            The id of the window.
         * @param w
         *            The width of the window.
         * @param h
         *            The height of the window.
         * @param xpos
         *            The x position of the window.
         * @param ypos
         *            The y position of the window.
         */
        public StandOutLayoutParams(int id, int w, int h, int xpos, int ypos) {
            this(id, w, h);

            if (xpos != AUTO_POSITION) {
                x = xpos;
            }
            if (ypos != AUTO_POSITION) {
                y = ypos;
            }

            Display display = mWindowManager.getDefaultDisplay();
            Point size=new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            if (x == RIGHT) {
                x = width - w;
            } else if (x == CENTER) {
                x = (width - w) / 2;
            }

            if (y == BOTTOM) {
                y = height - h;
            } else if (y == CENTER) {
                y = (height - h) / 2;
            }
        }
        // helper to create cascading windows
        private int getX(int id, int width) {
            Display display = mWindowManager.getDefaultDisplay();
            Point size=new Point();
            display.getSize(size);
            int displayWidth = size.x;

            int types = sWindowCache.size();

            int initialX = 100 * types;
            int variableX = 100 * id;
            int rawX = initialX + variableX;

            return rawX % (displayWidth - width);
        }

        // helper to create cascading windows
        private int getY(int id, int height) {
            Display display = mWindowManager.getDefaultDisplay();
            Point size=new Point();
            display.getSize(size);
            int displayWidth = size.x;
            int displayHeight = size.y;

            int types = sWindowCache.size();

            int initialY = 100 * types;
            int variableY = x + 200 * (100 * id) / (displayWidth - width);

            int rawY = initialY + variableY;

            return rawY % (displayHeight - height);
        }

        public void setFocusFlag(boolean focused) {
            if (focused) {
                flags = flags ^ StandOutLayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                flags = flags | StandOutLayoutParams.FLAG_NOT_FOCUSABLE;
            }
        }
    }

    protected class DropDownListItem {

        Runnable action;

        public DropDownListItem( Runnable action) {
            super();

            this.action = action;
        }


    }
}
