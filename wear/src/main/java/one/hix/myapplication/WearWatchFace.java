

package one.hix.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class WearWatchFace extends CanvasWatchFaceService {

    private static final long INTERACTIVE_UPDATE_RATE_MS = 1000;
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        onCreateEngine();
    }

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements IWatchFaceConfig {

        // IWatchfaceConfig
        @Override
        public Calendar getCalendar() {
            return mCalendar;
        }

        @Override
        public boolean isLightTheme() {
            return mLightTheme;
        }

        @Override
        public boolean isAmbient() {
            return mAmbient;
        }

        @Override
        public boolean isLowBitAmbient() {
            return mLowBitAmbient;
        }

        @Override
        public boolean isRound() {
            return mIsRound;
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mIsRound = insets.isRound();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        WatchFaceDrawer mWatchfaceDrawer;

        boolean mAmbient = false;
        boolean mLowBitAmbient = false;
        boolean mIsRound = false;
        GregorianCalendar mCalendar = new GregorianCalendar();

        final Handler mUpdateTimeHandler = new EngineHandler(this);

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getTimeZone(intent.getStringExtra("time-zone")));
                mCalendar.setTimeInMillis(System.currentTimeMillis());
            }
        };
        boolean mRegisteredTimeZoneReceiver = false;

        boolean mLightTheme = true;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            mWatchfaceDrawer = new WatchFaceDrawer();
            setNewWatchFaceStyle();
            invalidate();
        }

        private void setNewWatchFaceStyle() {
            WatchFaceStyle.Builder watchfaceStyleBuilder = new WatchFaceStyle.Builder(WearWatchFace.this)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_VISIBLE)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false);

            if (mLightTheme) {
                watchfaceStyleBuilder = watchfaceStyleBuilder.setViewProtectionMode(WatchFaceStyle.PROTECT_STATUS_BAR | WatchFaceStyle.PROTECT_HOTWORD_INDICATOR);
            }
            setWatchFaceStyle(watchfaceStyleBuilder.build());
        }


        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                mWatchfaceDrawer.onAmbientModeChanged(getApplicationContext(), this);
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            mWatchfaceDrawer.onDraw(getApplicationContext(), this, canvas, bounds);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                mCalendar.setTimeInMillis(System.currentTimeMillis());
            } else {
                unregisterReceiver();
            }

            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            WearWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            WearWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.*/
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /** Returns whether the {@link #mUpdateTimeHandler} timer should be running.
         * The timer should only run when we're visible and in interactive mode.*/
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

    }

    private static class EngineHandler extends Handler {
        private final WeakReference<Engine> mWeakReference;
        public EngineHandler(Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            Engine engine = mWeakReference.get();
            if (engine != null) {
                if (msg.what == MSG_UPDATE_TIME) {
                    engine.handleUpdateTimeMessage();
                }
            }
        }
    }


    class WatchFaceDrawer {
        private boolean mIsMobilePreview = false;
        Paint paint = new Paint();

        public void onAmbientModeChanged(Context context, IWatchFaceConfig config) {
            if (config.isLowBitAmbient()) {
                Resources res = context.getResources();
                final boolean inAmbientMode = config.isAmbient();
            }
        }

        public void onDraw(Context context, IWatchFaceConfig config, Canvas canvas, Rect bounds) {

            final Calendar calendar = config.getCalendar();
            final boolean isAmbient = config.isAmbient();
            final boolean isRound = config.isRound();
            final boolean useLightTheme = !isAmbient && config.isLightTheme();

            final int width = bounds.width();
            final int height = bounds.height();
            float centerX = width / 2f;
            float centerY = height / 2f;

            if (useLightTheme) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.preview_analog);
                canvas.drawBitmap( Bitmap.createScaledBitmap(bitmap, width, height, true), 0, 0, paint);
            } else {
                Bitmap bitmapBlackBack = BitmapFactory.decodeResource(getResources(),
                        R.drawable.black_back);
                canvas.drawBitmap(bitmapBlackBack, 0, 0, paint);
            }

            paint.setTextSize(66);
            paint.setARGB(255, 255, 255, 255);
            paint.setAntiAlias(true);
            paint.setTypeface(Typeface.createFromAsset(getAssets(), "font/OpenSans-Regular.ttf"));
            canvas.drawText( String.format("%02d", calendar.get(Calendar.HOUR)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)), centerX, centerY*0.9f, paint);

            String dayOfWeek = "";
            switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
                case Calendar.MONDAY : dayOfWeek = "MON";
                break;
                case Calendar.TUESDAY : dayOfWeek = "TUE";
                break;
                case Calendar.WEDNESDAY : dayOfWeek = "WED";
                break;
                case Calendar.THURSDAY : dayOfWeek = "THU";
                break;
                case Calendar.FRIDAY : dayOfWeek = "FRI";
                break;
                case Calendar.SATURDAY : dayOfWeek = "SAT";
                break;
                case Calendar.SUNDAY : dayOfWeek = "SUN";
                break;
            }

            paint.setTextSize(35);
            paint.setARGB(255, 255, 255, 255);
            paint.setAntiAlias(true);
            paint.setTypeface(Typeface.createFromAsset(getAssets(), "font/OpenSans-Regular.ttf"));
            canvas.drawText(dayOfWeek + " " + calendar.get(Calendar.DAY_OF_MONTH), centerX, centerY*0.55f, paint);
        }
    }

    interface IWatchFaceConfig {
        Calendar getCalendar();
        boolean isAmbient();
        boolean isLowBitAmbient();
        boolean isRound();
        // put your watch face options here
        boolean isLightTheme();
    }


}
