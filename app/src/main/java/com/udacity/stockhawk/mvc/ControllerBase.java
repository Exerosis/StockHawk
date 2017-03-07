package com.udacity.stockhawk.mvc;

public interface ControllerBase <T extends ViewBase> {
    T getRootView();
}
