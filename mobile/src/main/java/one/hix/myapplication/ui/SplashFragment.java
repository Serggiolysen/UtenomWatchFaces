/*
 * Copyright (C) 2016 Google Inc. All Rights Reserved.
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
package one.hix.myapplication.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.wearable.intent.RemoteIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import one.hix.myapplication.R;

public class SplashFragment extends Fragment implements
        CapabilityClient.OnCapabilityChangedListener {

    private static final String TAG = "MainMobileActivity";
    private static final String CHECKING_MESSAGE = "Checking for Wear Devices for app...\n";
    private static final String NO_DEVICES = "You have no Wear devices linked to your phone at this time.\n";
    private static final String MISSING_ALL_MESSAGE = "You are missing the Wear app on all your Wear Devices, please click on the "
            + "button below to install it on those device(s).\n";
    private static final String INSTALLED_SOME_DEVICES_MESSAGE =
            "Wear app installed on some your device(s) (%s)!\n\nYou can now use the "
                    + "MessageApi, DataApi, etc.\n\n"
                    + "To install the Wear app on the other devices, please click on the button below.\n";
    private static final String INSTALLED_ALL_DEVICES_MESSAGE =
            "Wear app installed on all your devices (%s)!\n\n You can now use the MessageApi, DataApi, etc.";
    private static final String CAPABILITY_WEAR_APP = "verify_remote_example_wear_app";
//    private static final String PLAY_STORE_APP_URI = "market://details?id=com.google.android.wearable.app";
//    private static final String PLAY_STORE_APP_URI = "market://details?id=com.example.android.wearable.wear.wearverifyremoteapp";
    private static final String PLAY_STORE_APP_URI = "market://details?id=io.faceapp";

    private final ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == RemoteIntent.RESULT_OK) {
                System.out.println("ssss  1  ");
                Toast.makeText(getContext(), "Play Store Request to Wear device successful.", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                System.out.println("ssss  2 ");
                Toast.makeText(getContext(), "Play Store Request Failed. Wear device(s) may not support Play Store, "
                        + " that is, the Wear device may be version 1.0.", Toast.LENGTH_LONG).show();
            } else {
                System.out.println("ssss  3  ");
                throw new IllegalStateException("Unexpected result " + resultCode);
            }
        }
    };

    private TextView mInformationTextView;
    private Button mRemoteOpenButton;
    private Set<Node> mWearNodesWithApp;
    private List<Node> mAllConnectedNodes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        System.out.println("ssss  4  ");
        View root = inflater.inflate(R.layout.fragment_splash, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mInformationTextView = view.findViewById(R.id.information_text_view);
        mRemoteOpenButton = view.findViewById(R.id.remote_open_button);
        mInformationTextView.setText(CHECKING_MESSAGE);
        mRemoteOpenButton.setOnClickListener(v -> openPlayStoreOnWearDevicesWithoutApp());
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("ssss  5  ");
        Wearable.getCapabilityClient(getContext()).removeListener(this, CAPABILITY_WEAR_APP);
    }

    @Override
    public void onResume() {
        System.out.println("ssss  6 ");
        super.onResume();
        Wearable.getCapabilityClient(getContext()).addListener(this, CAPABILITY_WEAR_APP);

        findWearDevicesWithApp();
        findAllWearDevices();

        mResultReceiver.send(RemoteIntent.RESULT_OK, Bundle.EMPTY);
    }

    // Updates UI when capabilities change (install/uninstall wear app).
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        System.out.println("ssss  7  ");
        mWearNodesWithApp = capabilityInfo.getNodes();
        findAllWearDevices();
        verifyNodeAndUpdateUI();
    }

    private void findWearDevicesWithApp() {
        System.out.println("ssss  8  ");
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(getContext())
                .getCapability(CAPABILITY_WEAR_APP, CapabilityClient.FILTER_ALL);
        capabilityInfoTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                CapabilityInfo capabilityInfo = task.getResult();
                mWearNodesWithApp = capabilityInfo.getNodes();
                System.out.println("ssss  9  mWearNodesWithApp = " + mWearNodesWithApp);
                verifyNodeAndUpdateUI();
//                startActivity(new Intent(this, MainActivity.class));
            } else {
                System.out.println("ssss  10  ");
                Log.d(TAG, "Capability request failed to return any results.");
            }
        });
    }

    private void findAllWearDevices() {
        Log.d(TAG, "Capability request failed to return any results.");
        System.out.println("ssss  11  ");
        Task<List<Node>> NodeListTask = Wearable.getNodeClient(getContext()).getConnectedNodes();
        NodeListTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                mAllConnectedNodes = task.getResult();

                System.out.println("ssss  12 task.getResult() "+ mAllConnectedNodes);
            } else {
                System.out.println("ssss  13  ");
                Log.d(TAG, "Node request failed to return any results.");
            }
            verifyNodeAndUpdateUI();
        });
    }

    private void verifyNodeAndUpdateUI() {
        System.out.println("ssss  14  ");
        if ((mWearNodesWithApp == null) || (mAllConnectedNodes == null)) {
            System.out.println("ssss  15  Waiting on Results for both connected nodes and nodes with app");
            Log.d(TAG, "Waiting on Results for both connected nodes and nodes with app");
        } else if (mAllConnectedNodes.isEmpty()) {
            System.out.println("ssss  16  " + NO_DEVICES);
            Log.d(TAG, NO_DEVICES);
            mInformationTextView.setText(NO_DEVICES);
            mRemoteOpenButton.setVisibility(View.INVISIBLE);
        } else if (mWearNodesWithApp.isEmpty()) {
            System.out.println("ssss  17  " + MISSING_ALL_MESSAGE);
            Log.d(TAG, MISSING_ALL_MESSAGE);
            mInformationTextView.setText(MISSING_ALL_MESSAGE);
            mRemoteOpenButton.setVisibility(View.VISIBLE);
        } else if (mWearNodesWithApp.size() < mAllConnectedNodes.size()) {
            System.out.println("ssss  18  " + INSTALLED_SOME_DEVICES_MESSAGE);
            // TODO: Add your code to communicate with the wear app(s) via
            // Wear APIs (MessageApi, DataApi, etc.)
            String installMessage = String.format(INSTALLED_SOME_DEVICES_MESSAGE, mWearNodesWithApp);
            mInformationTextView.setText(installMessage);
            mRemoteOpenButton.setVisibility(View.VISIBLE);
        } else {
            System.out.println("ssss  19  " + INSTALLED_ALL_DEVICES_MESSAGE);
            // TODO: Add your code to communicate with the wear app(s) via
            // Wear APIs (MessageApi, DataApi, etc.)
            String installMessage = String.format(INSTALLED_ALL_DEVICES_MESSAGE, mWearNodesWithApp);
            mInformationTextView.setText(installMessage);
            mRemoteOpenButton.setVisibility(View.INVISIBLE);

        }
    }

    private void openPlayStoreOnWearDevicesWithoutApp() {
        System.out.println("ssss  20  ");
        ArrayList<Node> nodesWithoutApp = new ArrayList<>();
        for (Node node : mAllConnectedNodes) {
            System.out.println("ssss  21  ");
            if (!mWearNodesWithApp.contains(node)) {
                System.out.println("ssss  22  ");
                nodesWithoutApp.add(node);
            }
        }
        if (!nodesWithoutApp.isEmpty()) {
            System.out.println("ssss  23  ");
            Intent intent =
                    new Intent(Intent.ACTION_VIEW)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                            .setData(Uri.parse(PLAY_STORE_APP_URI));
            for (Node node : nodesWithoutApp) {
                System.out.println("ssss  24  ");
                RemoteIntent.startRemoteActivity(
                        getContext(),
                        intent,
                        mResultReceiver,
                        node.getId());
            }
        }
    }

    void isAppInstalled() {
        DataClient dataClient = Wearable.getDataClient(getContext());
        PutDataMapRequest dataRequest = PutDataMapRequest.create("abracadabra");

        dataRequest.getDataMap().putString("s1", "s2");
        PutDataRequest putDataRequest = dataRequest.asPutDataRequest();

        dataClient.putDataItem(putDataRequest);
    }
}
