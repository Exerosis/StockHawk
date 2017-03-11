package com.udacity.stockhawk.utilities;

import android.os.Parcel;
import android.os.Parcelable;

public interface Modelable extends Parcelable {
    void writeToModel(Model out);

    @Override
    void writeToParcel(Parcel out, int flags);

    interface Creator<T> extends Parcelable.Creator<T> {
        T createFromModel(Model in);

        @Override
        T createFromParcel(Parcel in);
    }
}