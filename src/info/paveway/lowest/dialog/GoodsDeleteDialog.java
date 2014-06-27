package info.paveway.lowest.dialog;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.OnUpdateListener;
import info.paveway.lowest.R;
import info.paveway.lowest.data.GoodsData;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.GoodsTable;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.PriceData;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * 最低価格記録アプリ
 * 商品データ削除確認ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class GoodsDeleteDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(GoodsDeleteDialog.class);

    /** 更新リスナー */
    private OnUpdateListener mOnUpdateListener;

    /** 商品データ */
    private GoodsData mGoodsData;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static GoodsDeleteDialog newInstance(GoodsData categoryData) {
        GoodsDeleteDialog instance = new GoodsDeleteDialog();
        Bundle args = new Bundle();
        args.putSerializable(ExtraKey.GOODS_DATA, categoryData);
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
        mGoodsData = (GoodsData)getArguments().getSerializable(ExtraKey.GOODS_DATA);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.goods_delete_dialog_title);
        String message =
                getResourceString(R.string.dialog_delete_message_prefix) +
                mGoodsData.getName() +
                getResourceString(R.string.dialog_delete_message_suffix);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.dialog_delete_button, new DialogButtonOnClickListener());
        builder.setNegativeButton(R.string.dialog_cancel_button, new DialogButtonOnClickListener());
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        mLogger.d("OUT(OK)");
        return dialog;
    }

    /**************************************************************************/
    /**
     * ダイアログボタンクリックリスナークラス
     *
     */
    private class DialogButtonOnClickListener implements DialogInterface.OnClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(DialogButtonOnClickListener.class);

        /**
         * ボタンがクリックされた時に呼び出される。
         *
         * @param dialog ダイアログ
         * @param which クリックされたボタン
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mLogger.d("IN");

            // ボタンにより処理を判別する。
            switch (which) {
            // 削除ボタンの場合
            case Dialog.BUTTON_POSITIVE:
                mLogger.d("BUTTON_POSITIVE");

                // 操作リストを生成する。
                ArrayList<ContentProviderOperation> operationList =
                        new ArrayList<ContentProviderOperation>();
                ContentProviderOperation.Builder builder = null;

                // 商品テーブルのデータを削除する。
                {
                    builder = ContentProviderOperation.newDelete(LowestProvider.GOODS_CONTENT_URI);
                    String selection = GoodsTable.ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mGoodsData.getId())};
                    builder.withSelection(selection, selectionArgs);
                    operationList.add(builder.build());
                }

                // 商品データに関連する価格データを削除する。
                {
                    String selection = PriceTable.ID + " = ?";
                    for (PriceData priceData : mGoodsData.getPriceDataList()) {
                        // 価格テーブルのデータを削除する。
                        builder = ContentProviderOperation.newDelete(LowestProvider.PRICE_CONTENT_URI);
                        String[] selectionArgs = {String.valueOf(priceData.getId())};
                        builder.withSelection(selection, selectionArgs);
                        operationList.add(builder.build());
                    }
                }

                // バッチ処理を行う。
                try {
                    getActivity().getContentResolver().applyBatch(LowestProvider.AUTHORITY, operationList);
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
                break;

            // キャンセルボタンの場合
            case Dialog.BUTTON_NEGATIVE:
                mLogger.d("BUTTON_NEGATIVE");

                toast(R.string.error_cancel);

                // 終了する。
                dismiss();
                break;
            }

            mLogger.d("OUT(OUT)");
        }
    }
}
