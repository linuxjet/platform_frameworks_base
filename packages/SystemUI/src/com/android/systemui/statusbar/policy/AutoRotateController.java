/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.android.internal.view.RotationPolicy;

import android.content.Context;
import android.widget.CompoundButton;

public final class AutoRotateController implements CompoundButton.OnCheckedChangeListener {
    private final Context mContext;
    private final CompoundButton mCheckbox;
    private final RotationLockCallbacks mCallbacks;

    private boolean mAutoRotation;
    private boolean mSystemUpdate;

    private ContentObserver mAccelerometerRotationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateAccelerometerRotationCheckbox();
        }
    };


    private final RotationPolicy.RotationPolicyListener mRotationPolicyListener =
            new RotationPolicy.RotationPolicyListener() {
        @Override
        public void onChange() {
            updateState();
        }
    };

    public AutoRotateController(Context context, CompoundButton checkbox,
            RotationLockCallbacks callbacks) {
        mContext = context;
        mCheckbox = checkbox;
        mCallbacks = callbacks;

        mCheckbox.setOnCheckedChangeListener(this);

        RotationPolicy.registerRotationPolicyListener(context, mRotationPolicyListener);
        updateState();
    }

    public void onCheckedChanged(CompoundButton view, boolean checked) {
        if (mSystemUpdate) return;

        if (checked != mAutoRotation) {
            mAutoRotation = checked;
            RotationPolicy.setRotationLock(mContext, !checked);
        }
    }

    private void updateAccelerometerRotationCheckbox() {
        mSystemUpdate = true;
        mCheckbox.setChecked(Settings.System.getInt(
                mContext.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) != 0);
        mSystemUpdate = false;
    }

    public void release() {
        RotationPolicy.unregisterRotationPolicyListener(mContext,
                mRotationPolicyListener);
    }

    private void updateState() {
        mAutoRotation = !RotationPolicy.isRotationLocked(mContext);
        mCheckbox.setChecked(mAutoRotation);

        boolean visible = RotationPolicy.isRotationLockToggleVisible(mContext);
        mCallbacks.setRotationLockControlVisibility(visible);
        mCheckbox.setEnabled(visible);
    }

    public interface RotationLockCallbacks {
        void setRotationLockControlVisibility(boolean show);
    }
}
