package com.example.groceryapp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.groceryapp.databinding.ProgressDialogBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.UUID;

public final class Utils {
    private static AlertDialog dialog;
    private static FirebaseAuth auth;
    private static FirebaseUser user;
    private static String uid;
    public static void showToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
    public static void showDialog(Context context,String message){
        ProgressDialogBinding progress = ProgressDialogBinding.inflate(LayoutInflater.from(context));
        progress.progressMsg.setText(message);

        dialog = new AlertDialog.Builder(context).setView(progress.getRoot()).setCancelable(false).create();
        dialog.show();

    }
    public static void hideDialog(){
        if(dialog!=null && dialog.isShowing()){
            dialog.dismiss();
            dialog = null;
        }
    }

    public static FirebaseAuth getInstance(){
        if(auth==null){
            auth = FirebaseAuth.getInstance();
        }
        return auth;

    }
    public static FirebaseUser getCurrentUser(){
        if(user==null){
            user = FirebaseAuth.getInstance().getCurrentUser();
        }
        return user;
    }
    public static String getUserId(){
        if(uid==null){
        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        }
        return uid;
    }
    public static String getUniqueId() {
        return "ORD-" + UUID.randomUUID().toString();
    }

    public static <T> void observeOnce(LiveData<T> liveData, LifecycleOwner owner, Observer<T> observer) {
        liveData.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                observer.onChanged(t);
                liveData.removeObserver(this);
            }
        });
    }


}
