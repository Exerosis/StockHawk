package com.udacity.stockhawk.mvc;

import android.os.Bundle;
import android.view.View;

public interface ViewBase {
    View getRoot();

    Bundle getViewState();
}
