package com.grantech.plugins.installprompt;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;

import com.google.android.gms.instantapps.InstantApps;
import com.google.android.gms.instantapps.InstantAppsClient;
import com.google.android.gms.instantapps.PackageManagerCompat;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.review.model.ReviewErrorCode;

import java.util.Objects;

import io.flutter.embedding.android.FlutterActivity;

/**
 * InstallPrompt
 */
public class InstallPrompt implements FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private FlutterActivity activity;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "installprompt");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        //here we have access to activity
        activity = (FlutterActivity) binding.getActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {}
    @Override
    public void onDetachedFromActivity() {}
    @Override
    public void onDetachedFromActivityForConfigChanges() {}

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        String packageName = activity.getApplicationContext().getPackageName();
        switch (call.method) {
            case "showInstallPrompt":
                String referrer = call.argument("referrer");
                PackageManagerCompat packageManagerCompat = InstantApps.getPackageManagerCompat(activity.getApplicationContext());
                boolean isInstantApp = packageManagerCompat.isInstantApp();
                Log.d("DART/NATIVE", "referrer " + referrer + " packageName " + packageName);
                if (isInstantApp) {
                    Intent postInstallIntent = new Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_DEFAULT)
                            .setPackage(packageName);
                    InstantApps.showInstallPrompt(activity, postInstallIntent, 7, referrer);
                    result.success(true);
                } else {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                    result.success(true);
                }
                break;
            case "showUpdatePrompt":
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                result.success(true);
                break;
            case "showReviewPrompt":
                ReviewManager manager = ReviewManagerFactory.create(activity.getApplicationContext());
                Task<ReviewInfo> request = manager.requestReviewFlow();
                request.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ReviewInfo reviewInfo = task.getResult();
                        result.success(true);
                    } else {
                        Log.d("DART/NATIVE", Objects.requireNonNull(task.getException()).getMessage());
                        result.success(false);
                    }
                });
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
