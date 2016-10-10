package net.abraksis.appusagetracker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

public class Application implements Parcelable {

    private int workTimeMilliSec;
    private String packageName;

    public Application(String packageName, int workTimeMilliSec) {

        this.packageName = packageName;
        this.workTimeMilliSec = workTimeMilliSec;
    }

    private Application(Parcel in) {

        setPackageName(in.readString());
        setWorkTimeMilliSec(in.readInt());
    }

    public int getWorkTimeMilliSec() {
        return workTimeMilliSec;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setWorkTimeMilliSec(int appWorkTimeMilliSec) {

        this.workTimeMilliSec = appWorkTimeMilliSec;
    }

    public void setPackageName(String packageName) {

        this.packageName = packageName;
    }

    public void increaseWorkingTime(long timeAmountMillisec) {

        workTimeMilliSec += timeAmountMillisec;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(getPackageName());
        dest.writeLong(getWorkTimeMilliSec());
    }

    public static final Parcelable.Creator<Application> CREATOR = new Parcelable.Creator<Application>() {

        @Override
        public Application createFromParcel(Parcel source) {
            return new Application (source);
        }

        @Override
        public Application[] newArray(int size) {
            return new Application[size];
        }
    };
}
