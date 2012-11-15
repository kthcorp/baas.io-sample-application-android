
package com.kth.baasio.baassample.ui.dialog;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class DialogUtils {

    public static DefaultDialogFragment showDefaultDialog(Fragment fragment, String tag,
            String title, String message) {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
        Fragment prev = fragment.getFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DefaultDialogFragment defaultDialog = DefaultDialogFragment.newInstance();
        defaultDialog.setTitle(title);
        defaultDialog.setBody(message);
        // defaultDialog.setDialogResultListener(this);
        defaultDialog.show(ft, tag);

        return defaultDialog;
    }

    public static void dissmissDefaultDialog(Fragment fragment, String tag) {
        DefaultDialogFragment defaultDialog = (DefaultDialogFragment)fragment.getFragmentManager()
                .findFragmentByTag(tag);

        if (defaultDialog != null) {
            defaultDialog.dismiss();
        }
    }

    public static DefaultDialogFragment showDefaultDialog(FragmentActivity fragmentActivity,
            String tag, String title, String message) {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = fragmentActivity.getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DefaultDialogFragment defaultDialog = DefaultDialogFragment.newInstance();
        defaultDialog.setTitle(title);
        defaultDialog.setBody(message);
        defaultDialog.show(ft, tag);

        return defaultDialog;
    }

    public static void dissmissDefaultDialog(FragmentActivity fragmentActivity, String tag) {
        DefaultDialogFragment defaultDialog = (DefaultDialogFragment)fragmentActivity
                .getSupportFragmentManager().findFragmentByTag(tag);

        if (defaultDialog != null) {
            defaultDialog.dismiss();
        }
    }

    public static void showProgressDialog(Fragment fragment, String tag, int id, int style) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
        Fragment prev = fragment.getFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        ProgessDialogFragment progress = ProgessDialogFragment.newInstance();
        progress.setBody(fragment.getString(id));
        progress.setStyle(style);
        progress.show(ft, tag);
    }

    public static void showProgressDialog(Fragment fragment, String tag, int id) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
        Fragment prev = fragment.getFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        ProgessDialogFragment progress = ProgessDialogFragment.newInstance();
        progress.setBody(fragment.getString(id));
        progress.show(ft, tag);
    }

    public static void setProgress(Fragment fragment, String tag, int progressValue) {
        ProgessDialogFragment progress = (ProgessDialogFragment)fragment.getFragmentManager()
                .findFragmentByTag(tag);

        if (progress != null) {
            progress.setProgress(progressValue);
        }
    }

    public static void dissmissProgressDialog(Fragment fragment, String tag) {
        ProgessDialogFragment progress = (ProgessDialogFragment)fragment.getFragmentManager()
                .findFragmentByTag(tag);

        if (progress != null) {
            progress.dismiss();
        }
    }

    public static void showProgressDialog(FragmentActivity fragmentActivity, String tag, String body) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = fragmentActivity.getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        ProgessDialogFragment progress = ProgessDialogFragment.newInstance();
        progress.setBody(body);
        progress.show(ft, tag);
    }

    public static void showProgressDialog(FragmentActivity fragmentActivity, String tag,
            String body, int style) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = fragmentActivity.getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        ProgessDialogFragment progress = ProgessDialogFragment.newInstance();
        progress.setBody(body);
        progress.setStyle(style);
        progress.show(ft, tag);
    }

    public static void setProgress(FragmentActivity fragmentActivity, String tag, int progressValue) {
        ProgessDialogFragment progress = (ProgessDialogFragment)fragmentActivity
                .getSupportFragmentManager().findFragmentByTag(tag);

        if (progress != null) {
            progress.setProgress(progressValue);
        }
    }

    public static void dissmissProgressDialog(FragmentActivity fragmentActivity, String tag) {
        ProgessDialogFragment progress = (ProgessDialogFragment)fragmentActivity
                .getSupportFragmentManager().findFragmentByTag(tag);

        if (progress != null) {
            progress.dismiss();
        }
    }

    public static EntityDialogFragment showEntityDialog(Fragment fragment, String tag, int mode) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
        Fragment prev = fragment.getFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        EntityDialogFragment entityDialog = EntityDialogFragment.newInstance();
        entityDialog.setShareMode(mode);
        entityDialog.show(ft, tag);

        return entityDialog;
    }

    public static void dissmissEntityDialog(Fragment fragment, String tag) {
        EntityDialogFragment entityDialog = (EntityDialogFragment)fragment.getFragmentManager()
                .findFragmentByTag(tag);

        if (entityDialog != null) {
            entityDialog.dismiss();
        }
    }

    public static EntityDialogFragment showEntityDialog(FragmentActivity fragmentActivity,
            String tag, int mode) {
        if (mode == ShareDialogFragment.SHARE_FACBOOK) {

        } else if (mode == ShareDialogFragment.SHARE_TWITTER) {

        }

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = fragmentActivity.getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        EntityDialogFragment entityDialog = EntityDialogFragment.newInstance();
        entityDialog.setShareMode(mode);
        entityDialog.show(ft, tag);

        return entityDialog;
    }

    public static void dissmissEntityDialog(FragmentActivity fragmentActivity, String tag) {
        EntityDialogFragment entityDialog = (EntityDialogFragment)fragmentActivity
                .getSupportFragmentManager().findFragmentByTag(tag);

        if (entityDialog != null) {
            entityDialog.dismiss();
        }
    }
}
