package info.paveway.lowest.dialog;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.OnUpdateListener;
import info.paveway.lowest.R;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.LowestProvider.ShopTable;
import info.paveway.lowest.data.ShopData;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 最低価格記録アプリ
 * 店データ削除確認ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class ShopDeleteDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(ShopDetailDialog.class);

    /** 更新リスナー */
    private OnUpdateListener mOnUpdateListener;

    /** 店データ */
    private ShopData mShopData;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static ShopDeleteDialog newInstance(ShopData shopData) {
        ShopDeleteDialog instance = new ShopDeleteDialog();
        Bundle args = new Bundle();
        args.putSerializable(ExtraKey.SHOP_DATA, shopData);
        instance.setArguments(args);
        return instance;
    }

    /**
     * アクティビティにアタッチした時に呼び出される。
     *
     * @param activity アクティビティ
     */
    @Override
    public void onAttach(Activity activity) {
        mLogger.d("IN");

        // スーパークラスのメソッドを呼び出す。
        super.onAttach(activity);

        // リスナーを取得する。
        try {
            mOnUpdateListener = (OnUpdateListener)activity;
        } catch (ClassCastException e) {
            mLogger.e(e);
            throw new ClassCastException(activity.toString() + " must implement OnUpdateListener");
        }

        mLogger.d("OUT(OK)");
    }

    /**
     * ダイアログが生成された時に呼び出される。
     *
     * @param savedInstanceState 保存した時のインスタンスの状態
     * @return ダイアログ
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mLogger.d("IN");

        // 引数を取得する。
        mShopData = (ShopData)getArguments().getSerializable(ExtraKey.SHOP_DATA);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.shop_delete_dialog_title);
        String message =
                getResourceString(R.string.dialog_delete_message_prefix) +
                mShopData.getName() +
                getResourceString(R.string.dialog_delete_message_suffix);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.dialog_delete_button, null);
        builder.setNegativeButton(R.string.dialog_cancel_button, null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // ボタン押下でダイアログが閉じないようにリスナーを設定する。
        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog)dialog).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doDelete();
                    }
                });

                ((AlertDialog)dialog).getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doCancel();
                    }
                });
            }
        });

        mLogger.d("OUT(OK)");
        return dialog;
    }

    /**
     * 削除する。
     */
    private void doDelete() {
        mLogger.d("IN");

        // 操作リストを生成する。
        ArrayList<ContentProviderOperation> operationList =
                new ArrayList<ContentProviderOperation>();

        String shopId = String.valueOf(mShopData.getId());

        String selection = ShopTable.ID + " = ?";
        String[] selectionArgs = {shopId};

        // 店テーブルのデータを削除する。
        ContentProviderOperation.Builder builder =
                   ContentProviderOperation.newDelete(LowestProvider.SHOP_CONTENT_URI);
        builder.withSelection(selection, selectionArgs);
        operationList.add(builder.build());

        // 価格テーブルのデータを削除する。
        builder = ContentProviderOperation.newDelete(LowestProvider.PRICE_CONTENT_URI);
        selection = PriceTable.SHOP_ID + " = ?";
        builder.withSelection(selection, selectionArgs);
        operationList.add(builder.build());

        ContentResolver resolver = getActivity().getContentResolver();

        // バッチ処理を行う。
        try {
            resolver.applyBatch(LowestProvider.AUTHORITY, operationList);
        } catch (Exception e) {
            mLogger.e(e);
            toast(R.string.error_delete);
            mLogger.w("OUT(NG)");
            return;
        }

        // 更新を通知する。
        mOnUpdateListener.onUpdate();

        // 終了する。
        dismiss();
        mLogger.d("OUT(OK)");
    }

    /**
     * キャンセルする。
     */
    private void doCancel() {
        mLogger.d("IN");

        toast(R.string.error_cancel);

        // 終了する。
        dismiss();
        mLogger.d("OUT(OK)");
    }
}
