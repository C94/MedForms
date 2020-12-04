// Copyright @ MyScript. All rights reserved.

package com.myscript.iink.getstarted;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This activity displays an error message when an uncaught exception is thrown within an activity
 * that installed the associated exception handler. Since this application targets developers it's
 * better to clearly explain what happened.
 * The code is inspired by:
 * https://trivedihardik.wordpress.com/2011/08/20/how-to-avoid-force-close-error-in-android/
 */
public class ErrorActivity extends Activity {
    public static final String INTENT_EXTRA_MESSAGE = "message";
    public static final String INTENT_EXTRA_DETAILS = "details";

    public static void start(Activity context, String message, String details) {
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.putExtra(INTENT_EXTRA_MESSAGE, message);
        intent.putExtra(INTENT_EXTRA_DETAILS, details);
        context.startActivity(intent);
    }

    public static void installHandler(Activity context) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(context));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        Button btnBackError = findViewById(R.id.btnBackError);
        btnBackError.setOnClickListener(v -> {
            // Reset built up error string after displaying the errors
            SftpUtilities.errorString = "";

            Intent intent = new Intent(ErrorActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        TextView messageView = findViewById(R.id.error_message);
        messageView.setText(getIntent().getStringExtra(INTENT_EXTRA_MESSAGE));

        TextView detailsView = findViewById(R.id.error_details);
        detailsView.setText(getIntent().getStringExtra(INTENT_EXTRA_DETAILS));
        detailsView.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }

    private static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Activity context;

        public ExceptionHandler(Activity context) {
            this.context = context;
        }

        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
            // Get the message of the root cause
            Throwable rootCause = throwable;
            while (rootCause.getCause() != null)
                rootCause = rootCause.getCause();
            String message = rootCause.getMessage();

            // Print the stack trace to a string
            StringWriter stackTraceWriter = new StringWriter();
            throwable.printStackTrace(new PrintWriter(stackTraceWriter));
            String stackTrace = stackTraceWriter.toString();

            // Launch the error activity with the message and stack trace
            start(context, message, stackTrace);

            // Kill the current activity
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }
}
