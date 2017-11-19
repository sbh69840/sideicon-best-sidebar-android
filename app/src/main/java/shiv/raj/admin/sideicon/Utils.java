package shiv.raj.admin.sideicon;

/**
 * Created by shivraj on 13/01/2017.
 */

public class Utils {
    public static boolean isSet(int flags, int flag) {
        return (flags & flag) == flag;
    }
}
