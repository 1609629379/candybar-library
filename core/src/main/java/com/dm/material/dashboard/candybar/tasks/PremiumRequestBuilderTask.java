package com.dm.material.dashboard.candybar.tasks;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danimahardhika.android.helpers.core.FileHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.fragments.dialog.IntentChooserFragment;
import com.dm.material.dashboard.candybar.helpers.DeviceHelper;
import com.dm.material.dashboard.candybar.helpers.RequestHelper;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.dm.material.dashboard.candybar.utils.listeners.RequestListener;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class PremiumRequestBuilderTask extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;
    private Database mDatabase;
    private PremiumRequestBuilderCallback mCallback;
    private String mEmailBody;
    private LogUtil.Error mError;

    private PremiumRequestBuilderTask(Context context, PremiumRequestBuilderCallback callback) {
        mContext = context;
        mDatabase = Database.get(context);
        mCallback = callback;
    }

    public static AsyncTask start(@NonNull Context context, @Nullable PremiumRequestBuilderCallback callback) {
        return start(context, callback, SERIAL_EXECUTOR);
    }

    public static AsyncTask start(@NonNull Context context, @Nullable PremiumRequestBuilderCallback callback,
                                  @NonNull Executor executor) {
        return new PremiumRequestBuilderTask(context, callback).executeOnExecutor(executor);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        while (!isCancelled()) {
            try {
                Thread.sleep(1);
                if (CandyBarApplication.sRequestProperty == null) {
                    mError = LogUtil.Error.ICON_REQUEST_PROPERTY_NULL;
                    return false;
                }

                if (CandyBarApplication.sRequestProperty.getComponentName() == null) {
                    mError = LogUtil.Error.ICON_REQUEST_PROPERTY_COMPONENT_NULL;
                    return false;
                }

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(DeviceHelper.getDeviceInfo(mContext));

                List<Request> requests = mDatabase.getPremiumRequest(null);

                for (int i = 0; i < requests.size(); i++) {

                    stringBuilder.append("\n\n")
                            .append(requests.get(i).getName())
                            .append("\n")
                            .append(requests.get(i).getActivity())
                            .append("\n")
                            .append("https://play.google.com/store/apps/details?id=")
                            .append(requests.get(i).getPackageName())
                            .append("\n")
                            .append("Order Id: ")
                            .append(requests.get(i).getOrderId())
                            .append("\n")
                            .append("Product Id: ")
                            .append(requests.get(i).getProductId());
                }

                mEmailBody = stringBuilder.toString();
                return true;
            } catch (Exception e) {
                mDatabase.close();
                CandyBarApplication.sRequestProperty = null;
                LogUtil.e(Log.getStackTraceString(e));
                return false;
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            try {
                if (mCallback != null) mCallback.onFinished();

                RequestListener listener = (RequestListener) mContext;
                listener.onRequestBuilt(getIntent(CandyBarApplication.sRequestProperty.getComponentName(), mEmailBody),
                        IntentChooserFragment.REBUILD_ICON_REQUEST);
            } catch (Exception e) {
                LogUtil.e(Log.getStackTraceString(e));
            }
        } else {
            if (mError != null) {
                LogUtil.e(mError.getMessage());
                mError.showToast(mContext);
            }
        }
    }

    @Nullable
    private Intent getIntent(ComponentName name, String emailBody) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent = addIntentExtra(intent, emailBody);
            intent.setComponent(name);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        } catch (IllegalArgumentException e) {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent = addIntentExtra(intent, emailBody);
                return intent;
            } catch (ActivityNotFoundException e1) {
                LogUtil.e(Log.getStackTraceString(e1));
            }
        }
        return null;
    }

    private Intent addIntentExtra(@NonNull Intent intent, String emailBody) {
        intent.setType("message/rfc822");
        File zip = new File(mContext.getCacheDir(), RequestHelper.REBUILD_ZIP);
        if (zip.exists()) {
            Uri uri = FileHelper.getUriFromFile(mContext, mContext.getPackageName(), zip);
            if (uri == null) uri = Uri.fromFile(zip);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        String subject = "Rebuild Premium Request " +mContext.getResources().getString(R.string.app_name);

        intent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{mContext.getResources().getString(R.string.dev_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, emailBody);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return intent;
    }

    public interface PremiumRequestBuilderCallback {
        void onFinished();
    }
}
