package info.paveway.lowest.dialog;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.OnUpdateListener;
import info.paveway.lowest.R;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.PriceData;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 最低価格記録アプリ
 * 価格データ削除確認ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class PriceDeleteDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(ShopDetailDialog.class);

    /** 更新リスナー */
    private OnUpdateListener mOnUpdateListener;

    /** 価格データ */
    private PriceData mPriceData;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static PriceDeleteDialog newInstance(PriceData priceData) {
        PriceDeleteDialog instance = new PriceDeleteDialog();
        Bundle args = new Bundle();
        args.putSerializable(ExtraKey.PRICE_DATA, priceData);
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
        mPriceData = (PriceData)getArguments().getSerializable(ExtraKey.PRICE_DATA);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.price_delete_dialog_title);
        String message =
                getResourceString(R.string.dialog_delete_message_prefix) +
                mPriceData.getShopName() +
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

        String selection = PriceTable.ID + " = ?";
        String[] selectionArgs = {String.valueOf(mPriceData.getId())};
        int result = getActivity().getContentResolver().delete(LowestProvider.PRICE_CONTENT_URI, selection, selectionArgs);
        if (0 == result) {
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
