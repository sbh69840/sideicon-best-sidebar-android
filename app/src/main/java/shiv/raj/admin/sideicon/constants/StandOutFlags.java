package shiv.raj.admin.sideicon.constants;

/**
 * Created by shivraj on 10/01/2017.
 */
import shiv.raj.admin.sideicon.StandOutWindow;
import shiv.raj.admin.sideicon.ui.Window;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
/*** Flags to be returned from{@link StandOutWindow#getFlags(int)}.
 * @author Shivaraj <sbh69840@gmail.com>
 *
 */

public class StandOutFlags {
    //This counter keeps track of which primary bit to set for each flag
    private static int flag_bit = 0;
    /**
     * setting this flag indicates that the window wants the system provided
     * window decorations(title bar,hide/close buttons,resize handle,etc).
     */
    public static final int FLAG_DECORATION_SYSTEM = 1 << flag_bit++;
    /**
     * setting this flag indicates that the window decorator should not provide
     * a close button.
     * <p>
     * <p>
     * This flag also sets {@link #FLAG_DECORATION_SYSTEM}.
     * </p>
     */
    public static final int FLAG_DECORATION_CLOSE_DISABLE = FLAG_DECORATION_SYSTEM | 1 << flag_bit++;
/**
 * setting this flag indicates that the window decorator should not provide
 * a resize handle.
 *
 * <p>
 *     this flag also flag also sets{@link #FLAG_DECORATION_SYSTEM}.
 * </p>
 */
    public static final int FLAG_DECORATION_RESIZE_DISABLE=FLAG_DECORATION_SYSTEM|1<<flag_bit++;
    /**
     * setting this flag indicates that the window decorator should not provide
     * a resize handle.
     *
     * <p>
     *     this flag also sets {@link #FLAG_DECORATION_SYSTEM}is set.
     * </p>
     */
    public static final int FLAG_DECORATION_MAXIMIZE_DISABLE = FLAG_DECORATION_SYSTEM
            | 1 << flag_bit++;
    public static int FLAG_DECORATION_MOVE_DISABLE=FLAG_DECORATION_SYSTEM |1<<flag_bit++;
    /**
     * setting this indicates that the window can be moved by dragging
     * body.
     *
     * <p>
     *     note that if {@link #FLAG_DECORATION_SYSTEM} is set,the window can
     *     always be moved by dragging the titlebar regardless of this flag.
     * </p>
     */
    public static final int FLAG_BODY_MOVE_ENABLE=1<<flag_bit++;
    /**
     * setting this indicates that windows are able to be hidden, that
     * {@link StandOutWindow#getHiddenIcon()},
     * {@link StandOutWindow#getHiddenNotificationTitle(int)},
     * and
     * {@link StandOutWindow#getHiddenNotificationMessage(int)}
     * are implemented and that the system window decorator should provide a hide button if
     * {@link #FLAG_DECORATION_SYSTEM}is set.
     */
    public static final int FLAG_WINDOW_HIDE_ENABLE=1<<flag_bit++;
    /**
     * setting this flag indicates that the window should be brought to the
     * front upon user interaction.
     * <p>
     *     note that if you set this flag, there is a noticeable flashing of the
     *     window during{@link MotionEvent#ACTION_UP}.this is the hack that allows
     *     the system to bring the window to the front.
     * </p>
     */
public static final int FLAG_WINDOW_BRING_TO_FRONT_ON_TOUCH=1<<flag_bit++;
    /**
     * setting this flag indicates that the window should be brought to the
     * front upon user tap.
     *
     * <p>
     *     note that if you set this flag, there is a noticeable flashing of the
     *     window during{@link MotionEvent#ACTION_UP}. This is the hack that allows
     *     the system to bring the window to the front.
     * </p>
     */
    public static final int FLAG_WINDOW_BRING_TO_FRONT_ON_TAP=1<<flag_bit++;
    /**
     * setting this indicates that the system should keep the window's
     * position within the edges of the screen.If this is not set,the
     * window will be able to be dragged off of the screen.
     * <p>
     *     If this flag is set, the window's {@link Gravity}is recommended to be
     *     {@link Gravity#TOP}|{@link Gravity#LEFT}. if the gravity is anything
     *     other than TOP|LEFT,then even though the window will be displayed within
     *     the edges, it will behave as if the user can drag it off the screen.
     * </p>
     */
    public static final int FLAG_WINDOW_EDGE_LIMITS_ENABLE=1<<flag_bit++;
    /**
     * setting this flag indicates that the windows should keep the window's
     * aspect ratio constant when resizing.
     * <p>
     *     the aspect ratio will only be enforced in
     *     . The aspect ratio will not be enforced if you set the width or height of
     *     the window's LayoutParams manually.
     *
     */
    public static final int FLAG_WINDOW_ASPECT_RATIO_ENABLE=1<<flag_bit++;
    /**
     * setting this flag indicates that the system should resize the window when
     * it detects a pinch-to-zoom gesture.
     *
     * @see Window#onInterceptTouchEvent(MotionEvent)
     */
    public static int FLAG_WINDOW_PINCH_RESIZE_ENABLE=1<<flag_bit++;
    /**
     * setting this flag indicates that the window does not need focus. If this
     * flag is set, the system will not take care of setting and unsetting the
     * focus of windows based on user touch and keyevents.
     *
     * <p>
     *     you will most likely need foucs if your window contains any of the
     *     following:Button,ListView,EditText.
     *     <p>
     *         the benefit of disabling focus is that your window will not consume any
     *         key events. Normally, focused windows will consume back and menu keys.
     *
     *         @see {@link StandOutWindow#focus(int)}
     *@see {@link StandOutWindow#unfocus(int)}
     *     </p>
     * </p>
     */
    public static final int FLAG_WINDOW_FOCUSABLE_DISABLE=1<<flag_bit++;
    /**
     * setting this flag indicates that the system should not change the
     * window's virtual state when focus is changed. If this flag is set, the
     * implementation can choose to change the visual state in
     * {@link StandOutWindow#onFocusChange(int, Window, boolean)}
     * @see {@link Window#onFocus(boolean)}
     */
    public static final int FLAG_WINDOW_FOCUS_INDICATOR_DISABLE=1<<flag_bit++;
    /**
     * setting this flag indicates that the system should disable all
     * compatibility workarounds. The default behaviour is to run
     * {@link Window#fixCompatibility(View)}  on the view returned by the
     * implementation.
     * @see {@link Window#fixCompatibility(View)}
     */
    public static final int FLAG_FIX_COMPATIBILITY_ALL_DISABLE=1<<flag_bit++;
/**
 * setting this flag indicates that the system should disable all additional
 * functionality.
 * The default behaviour is to run
 * {@link Window#addFunctionality(View)}  on the view returned by the
 * implementation
 */
    public static final int FLAG_ADD_FUNCTIONALITY_ALL_DISABLE=1<<flag_bit++;
    /**
     * setting this flag indicates that the system should disable adding the
     * resize handle functionality to a custom View R.id.corner.
     * <p>
     *     If {@link #FLAG_DECORATION_SYSTEM} is set, the user will always be able
     *     to resize the window with the default corner.
     *
     *     @see {@link Window#addFunctionality(View)}
     * </p>
     */
    public static final int FLAG_ADD_FUNCTIONALITY_RESIZE_DISABLE=1<<flag_bit++;
    /**
     * setting this flag indicates that the system should disable adding the
     * drop down menu additional functionality to a custom View
     * R.id.window_icon.
     *
     * <p>
     *   If {@link #FLAG_DECORATION_SYSTEM} is set, the user will always be able to
     *   show the drop down menu with the default window icon.
     *
     *@see {@link Window#addFunctionality(View )}
     * </p>
     */
    public static final int FLAG_ADD_FUNCTIONALITY_DROP_DOWN_DISABLE=1<<flag_bit;
}