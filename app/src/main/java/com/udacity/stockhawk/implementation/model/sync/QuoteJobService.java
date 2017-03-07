package com.udacity.stockhawk.implementation.model.sync;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import timber.log.Timber;

public class QuoteJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters parameters) {
        Timber.d("Intent handled");
        getApplicationContext().startService(new Intent(getApplicationContext(), QuoteIntentService.class));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters parameters) {
        return false;
    }


}
