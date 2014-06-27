package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.CategoryData;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.CategoryTable;
import info.paveway.lowest.data.LowestProvider.GoodsTable;
import info.paveway.lowest.data.LowestProvider.PriceTable;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;

/**
 * 最低価格記録アプリ
 * カテゴリデータ削除確認ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class CategoryDeleteDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(CategoryDeleteDialog.class);

    /** 更新リスナー */
    private OnUpdateListener mOnUpdateListener;

    /** カテゴリデータ */
    private CategoryData mCategoryData;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static CategoryDeleteDialog newInstance(CategoryData categoryData) {
        CategoryDeleteDialog instance = new CategoryDeleteDialog();
        Bundle args = new Bundle();
        args.putSerializable(ExtraKey.CATEGORY_DATA, categoryData);
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
        mCategoryData = (CategoryData)getArguments().getSerializable(ExtraKey.CATEGORY_DATA);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("削除確認");
        builder.setMessage("「" + mCategoryData.getName() + "」を削除しますか");
        builder.setPositiveButton("削除", new DialogButtonOnClickListener());
        builder.setNegativeButton("キャンセル", new DialogButtonOnClickListener());
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

                // カテゴリテーブルのデータを削除する。
                {
                    builder = ContentProviderOperation.newDelete(LowestProvider.CATEGORY_CONTENT_URI);
                    String selection = CategoryTable.ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mCategoryData.getId())};
                    builder.withSelection(selection, selectionArgs);
                    operationList.add(builder.build());
                }

                ContentResolver resolver = getActivity().getContentResolver();
                String defaultValue = getActivity().getResources().getString(R.string.default_value);

                // 更新日時を取得する。
                long updateTime = new Date().getTime();

                // 商品テーブルのデータで該当カテゴリのデータをカテゴリ無に更新する。
                {
                    // 商品テーブルの該当カテゴリのデータを検索する。
                    String selection = GoodsTable.CATEGORY_ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mCategoryData.getId())};
                    Cursor c =
                            resolver.query(
                                    LowestProvider.GOODS_CONTENT_URI, null, selection, selectionArgs, null);

                    // カーソルが取得できた場合
                    if (null != c) {
                        // カーソルを先頭に移動できた場合
                        if (c.moveToFirst()) {
                            // データがある間繰り返す。
                            do {
                                builder = ContentProviderOperation.newUpdate(LowestProvider.GOODS_CONTENT_URI);
                                String updateSelection = GoodsTable.ID + " = ?";
                                String[] updateSelectionArgs = {String.valueOf(c.getLong(c.getColumnIndex(GoodsTable.ID)))};
                                builder.withSelection(updateSelection, updateSelectionArgs);
                                builder.withValue(GoodsTable.CATEGORY_ID, 1);
                                builder.withValue(GoodsTable.CATEGORY_NAME, defaultValue);
                                builder.withValue(GoodsTable.UPDATE_TIME, updateTime);
                                operationList.add(builder.build());
                            } while (c.moveToNext());
                        }
                    }
                }

                // 価格テーブルのデータで該当カテゴリを未指定に更新する。
                {
                    // 価格テーブルの該当カテゴリのデータを検索する。
                    String selection = PriceTable.CATEGORY_ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mCategoryData.getId())};
                    Cursor c =
                            resolver.query(
                                    LowestProvider.PRICE_CONTENT_URI, null, selection, selectionArgs, null);

                    // カーソルが取得できた場合
                    if (null != c) {
                        // カーソルを先頭に移動できた場合
                        if (c.moveToFirst()) {
                            // データがある間繰り返す。
                            do {
                                builder = ContentProviderOperation.newUpdate(LowestProvider.PRICE_CONTENT_URI);
                                String updateSelection = PriceTable.ID + " = ?";
                                String[] updateSelectionArgs = {String.valueOf(c.getLong(c.getColumnIndex(PriceTable.ID)))};
                                builder.withSelection(updateSelection, updateSelectionArgs);
                                builder.withValue(PriceTable.CATEGORY_ID, 1);
                                builder.withValue(PriceTable.CATEGORY_NAME, defaultValue);
                                builder.withValue(PriceTable.UPDATE_TIME, updateTime);
                                operationList.add(builder.build());
                            } while (c.moveToNext());
                        }
                    }
                }

                // バッチ処理を行う。
                try {
                    resolver.applyBatch(LowestProvider.AUTHORITY, operationList);
                } catch (Exception e) {
                    mLogger.e(e);
                    toast("削除に失敗しました");
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
                toast("キャンセルします");

                // 終了する。
                dismiss();
                break;
            }

            mLogger.d("OUT(OK)");
        }
    }
}
