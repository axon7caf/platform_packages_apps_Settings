/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.settings.core.lifecycle;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.SettingsRobolectricTestRunner;
import com.android.settings.TestConfig;
import com.android.settings.core.lifecycle.events.OnAttach;
import com.android.settings.core.lifecycle.events.OnCreateOptionsMenu;
import com.android.settings.core.lifecycle.events.OnDestroy;
import com.android.settings.core.lifecycle.events.OnOptionsItemSelected;
import com.android.settings.core.lifecycle.events.OnPause;
import com.android.settings.core.lifecycle.events.OnPrepareOptionsMenu;
import com.android.settings.core.lifecycle.events.OnResume;
import com.android.settings.core.lifecycle.events.OnStart;
import com.android.settings.core.lifecycle.events.OnStop;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;
import org.robolectric.util.FragmentController;

import static com.google.common.truth.Truth.assertThat;

@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class LifecycleTest {

    public static class TestDialogFragment extends ObservableDialogFragment {

        final TestObserver mFragObserver;

        public TestDialogFragment() {
            mFragObserver = new TestObserver();
            mLifecycle.addObserver(mFragObserver);
        }
    }

    public static class TestFragment extends ObservableFragment {

        final TestObserver mFragObserver;

        public TestFragment() {
            mFragObserver = new TestObserver();
            getLifecycle().addObserver(mFragObserver);
        }
    }

    public static class TestActivity extends ObservableActivity {

        final TestObserver mActObserver;

        public TestActivity() {
            mActObserver = new TestObserver();
            getLifecycle().addObserver(mActObserver);
        }

    }

    public static class TestObserver implements LifecycleObserver, OnAttach, OnStart, OnResume,
            OnPause, OnStop, OnDestroy, OnCreateOptionsMenu, OnPrepareOptionsMenu,
            OnOptionsItemSelected {

        boolean mOnAttachObserved;
        boolean mOnAttachHasContext;
        boolean mOnStartObserved;
        boolean mOnResumeObserved;
        boolean mOnPauseObserved;
        boolean mOnStopObserved;
        boolean mOnDestroyObserved;
        boolean mOnCreateOptionsMenuObserved;
        boolean mOnPrepareOptionsMenuObserved;
        boolean mOnOptionsItemSelectedObserved;

        @Override
        public void onAttach(Context context) {
            mOnAttachObserved = true;
            mOnAttachHasContext = context != null;
        }

        @Override
        public void onStart() {
            mOnStartObserved = true;
        }

        @Override
        public void onPause() {
            mOnPauseObserved = true;
        }

        @Override
        public void onResume() {
            mOnResumeObserved = true;
        }

        @Override
        public void onStop() {
            mOnStopObserved = true;
        }

        @Override
        public void onDestroy() {
            mOnDestroyObserved = true;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            mOnCreateOptionsMenuObserved = true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem menuItem) {
            mOnOptionsItemSelectedObserved = true;
            return true;
        }

        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            mOnPrepareOptionsMenuObserved = true;
        }
    }

    @Test
    public void runThroughActivityLifecycles_shouldObserveEverything() {
        ActivityController<TestActivity> ac = Robolectric.buildActivity(TestActivity.class);
        TestActivity activity = ac.get();

        ac.start();
        assertThat(activity.mActObserver.mOnStartObserved).isTrue();
        ac.resume();
        assertThat(activity.mActObserver.mOnResumeObserved).isTrue();
        activity.onCreateOptionsMenu(null);
        assertThat(activity.mActObserver.mOnCreateOptionsMenuObserved).isTrue();
        activity.onPrepareOptionsMenu(null);
        assertThat(activity.mActObserver.mOnPrepareOptionsMenuObserved).isTrue();
        activity.onOptionsItemSelected(null);
        assertThat(activity.mActObserver.mOnOptionsItemSelectedObserved).isTrue();
        ac.pause();
        assertThat(activity.mActObserver.mOnPauseObserved).isTrue();
        ac.stop();
        assertThat(activity.mActObserver.mOnStopObserved).isTrue();
        ac.destroy();
        assertThat(activity.mActObserver.mOnDestroyObserved).isTrue();
    }

    @Test
    public void runThroughDialogFragmentLifecycles_shouldObserveEverything() {
        FragmentController<TestDialogFragment> fragmentController =
                Robolectric.buildFragment(TestDialogFragment.class);
        TestDialogFragment fragment = fragmentController.get();

        fragmentController.attach().create().start().resume();
        fragment.onCreateOptionsMenu(null, null);
        fragment.onPrepareOptionsMenu(null);
        fragment.onOptionsItemSelected(null);
        fragmentController.pause().stop().destroy();

        assertThat(fragment.mFragObserver.mOnAttachObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnAttachHasContext).isTrue();
        assertThat(fragment.mFragObserver.mOnStartObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnResumeObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnPauseObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnStopObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnDestroyObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnCreateOptionsMenuObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnPrepareOptionsMenuObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnOptionsItemSelectedObserved).isTrue();
    }

    @Test
    public void runThroughFragmentLifecycles_shouldObserveEverything() {
        FragmentController<TestFragment> fragmentController =
                Robolectric.buildFragment(TestFragment.class);
        TestFragment fragment = fragmentController.get();

        fragmentController.attach().create().start().resume();
        fragment.onCreateOptionsMenu(null, null);
        fragment.onPrepareOptionsMenu(null);
        fragment.onOptionsItemSelected(null);
        fragmentController.pause().stop().destroy();

        assertThat(fragment.mFragObserver.mOnAttachObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnAttachHasContext).isTrue();
        assertThat(fragment.mFragObserver.mOnStartObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnResumeObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnPauseObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnStopObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnDestroyObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnCreateOptionsMenuObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnPrepareOptionsMenuObserved).isTrue();
        assertThat(fragment.mFragObserver.mOnOptionsItemSelectedObserved).isTrue();
    }

    private static class OptionItemAccepter implements LifecycleObserver, OnOptionsItemSelected {
        public boolean wasCalled = false;

        @Override
        public boolean onOptionsItemSelected(MenuItem menuItem) {
            wasCalled = true;
            return false;
        }
    }

    @Test
    public void onOptionItemSelectedShortCircuitsIfAnObserverHandlesTheMenuItem() {
        FragmentController<TestFragment> fragmentController =
                Robolectric.buildFragment(TestFragment.class);
        TestFragment fragment = fragmentController.get();
        OptionItemAccepter accepter = new OptionItemAccepter();
        fragment.getLifecycle().addObserver(accepter);

        fragmentController.attach().create().start().resume();
        fragment.onCreateOptionsMenu(null, null);
        fragment.onPrepareOptionsMenu(null);
        fragment.onOptionsItemSelected(null);
        fragmentController.pause().stop().destroy();

        assertThat(accepter.wasCalled).isFalse();
    }
}
