package deltazero.amarok.AppHider;

import android.content.Context;
import android.util.Log;

import java.util.Set;

public class NoneAppHider extends IAppHider {
    
    // Active when no AppHider is available.

    public NoneAppHider(Context context) {
        super(context);
    }

    @Override
    public void hide(Set<String> pkgNames) {
        Log.w("AppHider", "Skip app hiding: hider disabled");
    }

    @Override
    public void unhide(Set<String> pkgNames) {
        Log.w("AppHider", "Skip app unhiding: hider disabled");
    }

    @Override
    public void tryToActivate(ActivationCallbackListener activationCallbackListener) {
        activationCallbackListener.onActivateCallback(getClass(), true, 0);
    }

    @Override
    public String getName() {
        return "None";
    }
}
