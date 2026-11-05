
package com.example.bonycallgreetings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Locale;

public class CallReceiver extends BroadcastReceiver {

    private static final String TAG = "BonyGreets";
    private static final String PREFS = "BonySettings";
    private static final String ENABLED = "greeting_enabled";
    private static boolean hasGreeted = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String state = intent.getStringExtra(
                TelephonyManager.EXTRA_STATE);

        if (state == null) return;

        SharedPreferences prefs =
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Reset when the call ends.
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            hasGreeted = false;
            return;
        }

        // Only greet when a new incoming call starts ringing.
        if (!TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            return;
        }

        if (!prefs.getBoolean(ENABLED, true) || hasGreeted) {
            return;
        }

        hasGreeted = true;

        final PendingResult pending = goAsync();
        final Context appContext = context.getApplicationContext();

        final TextToSpeech[] engine = new TextToSpeech[1];

        try {
            engine[0] = new TextToSpeech(appContext, status -> {
                try {
                    TextToSpeech tts = engine[0];

                    if (status != TextToSpeech.SUCCESS || tts == null) {
                        Log.e(TAG, "Text-to-Speech initialization failed");
                        return;
                    }

                    int languageResult = tts.setLanguage(Locale.US);

                    if (languageResult ==
                            TextToSpeech.LANG_MISSING_DATA ||
                            languageResult ==
                                    TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "English voice is unavailable");
                        return;
                    }

                    tts.setSpeechRate(0.95f);
                    tts.setPitch(1.2f);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.setAudioAttributes(
                                new AudioAttributes.Builder()
                                        .setUsage(
                                                AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                                        .setContentType(
                                                AudioAttributes.CONTENT_TYPE_SPEECH)
                                        .build());
                    }

                    tts.setOnUtteranceProgressListener(
                            new UtteranceProgressListener() {

                                @Override
                                public void onStart(String utteranceId) {
                                    Log.d(TAG, "Greeting started");
                                }

                                @Override
                                public void onDone(String utteranceId) {
                                    Log.d(TAG, "Greeting completed");
                                    finishTts(engine[0], pending);
                                }

                                @Override
                                public void onError(String utteranceId) {
                                    Log.e(TAG, "Greeting playback failed");
                                    finishTts(engine[0], pending);
                                }
                            });

                    int result = tts.speak(
                            "Bony, your phone is calling!",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "BonyGreeting");

                    if (result == TextToSpeech.ERROR) {
                        finishTts(tts, pending);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Greeting error", e);
                    finishTts(engine[0], pending);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Could not create Text-to-Speech", e);
            finishTts(engine[0], pending);
        }
    }

    private static void finishTts(
            TextToSpeech tts, PendingResult pending) {

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        pending.finish();
    }
}
