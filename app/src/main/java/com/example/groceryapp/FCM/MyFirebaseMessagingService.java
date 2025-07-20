package com.example.groceryapp.FCM;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.groceryapp.Activity.OrderActivity;
import com.example.groceryapp.R;
import com.example.groceryapp.utils.Utils;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        // Save token locally
        getSharedPreferences("FCM_PREF", MODE_PRIVATE)
                .edit()
                .putString("fcmToken", token)
                .apply();

        // If user phone is already available, upload token to Firebase
        String phone = Utils.getUserPhoneNumber();
        if (phone != null && !phone.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("AllUsers")
                    .child("User")
                    .child("FCMToken")
                    .child(phone)
                    .child("fcmToken")
                    .setValue(token);
        }
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "New Notification";
        String body = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "You have a new message";

        Intent intent = new Intent(this, OrderActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "fcm_channel")
                .setSmallIcon(R.drawable.logo) // white-only icon for status bar
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                "fcm_channel",
                "FCM Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationManager.createNotificationChannel(channel);

        notificationManager.notify(0, notificationBuilder.build());
    }

}
