import 'package:flutter/services.dart';

class InstallPrompt {
  static const MethodChannel _channel = const MethodChannel('installprompt');

  static void showInstallPrompt({String? referrer}) {
    _channel.invokeMethod('showInstallPrompt', {"referrer": referrer ?? null});
  }

  static void showUpdatePrompt() {
    _channel.invokeMethod('showUpdatePrompt');
  }

  static void showReviewPrompt() {
    _channel.invokeMethod('showReviewPrompt');
  }
}
