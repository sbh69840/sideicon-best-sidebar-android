package shiv.raj.admin.sideicon.ui;

/**
 * Created by shivraj on 14/01/2017.
 */



import java.util.Locale;

/**
 * This class holds temporal touch and gesture information. Mainly used to hold
 * temporary data for onTouchEvent(MotionEvent).
 *
 *
 */
public class TouchInfo {
    /**
     * The state of the window.
     */
    public int firstX, firstY, lastX, lastY;
    double dist, scale, firstWidth, firstHeight;
    float ratio;

    /**
     * Whether we're past the move threshold already.
     */
    public boolean moving;

    @Override
    public String toString() {
        return String
                .format(Locale.US,
                        "WindowTouchInfo { firstX=%d, firstY=%d,lastX=%d, lastY=%d, firstWidth=%s, firstHeight=%s }",
                        firstX, firstY, lastX, lastY, firstWidth, firstHeight);
    }
}

