package com.udacity.stockhawk.mvc;

import android.support.annotation.IdRes;

public interface Container extends ViewBase {
    @IdRes
    int getContainerID();
}
