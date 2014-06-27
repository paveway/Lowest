package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.CommonConstants.RequestCode;
import info.paveway.lowest.data.CategoryData;
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
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 最低価格記録アプリ
 * 商品編集ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class GoodsEditDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(GoodsEditDialog.class);

    /** 更新リスナー */
    private OnUpdateListener mOnUpdateListener;

    /** 商品データ */
    private GoodsData mGoodsData;

    /** カテゴリ表示 */
    private TextView mCategoryNameValue;

    /** 商品名入力 */
    private EditText mGoodsNameValue;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static GoodsEditDialog newInstance(GoodsData goodsData) {
        GoodsEditDialog instance = new GoodsEditDialog();
        Bundle args = new Bundle();
        args.putSerializable(ExtraKey.GOODS_DATA, goodsData);
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
     * 生成した時に呼び出される。
     *
     * @param savedInstanceState 保存した時のインスタンスの状態
     * @return ダイアログ
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mLogger.d("IN");

        // 引数を取得する。
        mGoodsData = (GoodsData)getArguments().getSerializable(ExtraKey.GOODS_DATA);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_goods_edit, null);

        // カテゴリ名ボタンにリスナーを設定する。
        ((Button)rootView.findViewById(R.id.categoryNameButton)).setOnClickListener(new ButtonOnClickListener());

        mCategoryNameValue = (TextView)rootView.findViewById(R.id.categoryNameValue);
        mGoodsNameValue = (EditText)rootView.findViewById(R.id.goodsNameValue);

        // 更新の場合
        if (StringUtil.isNotNullOrEmpty(mGoodsData.getName())) {
            mCategoryNameValue.setText(mGoodsData.getCategoryName());
            mGoodsNameValue.setText(mGoodsData.getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("商品編集");
        builder.setPositiveButton("登録", new DialogButtonOnClickListener());
        builder.setNegativeButton("キャンセル", new DialogButtonOnClickListener());
        builder.setView(rootView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        mLogger.d("OUT(OUT)");
        return dialog;
    }

    /**
     * 他の画面の呼び出しから戻った時に呼び出される。
     *
     * @param requestCode 要求コード
     * @param resultCode 結果コード
     * @param data データ
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mLogger.d("IN");

        // 要求コードがカテゴリリストかつ正常終了の場合
        if ((RequestCode.CATEGORY_LIST == requestCode) && (Activity.RESULT_OK == resultCode)) {
            // データがある場合
            if (null != data) {
                // カテゴリデータを取得する。
                CategoryData categoryData = (CategoryData)data.getSerializableExtra(ExtraKey.CATEGORY_DATA);

                // カテゴリデータが取得できた場合
                if (null != categoryData) {
                    // 商品データに設定する。
                    mGoodsData.setCategoryId(categoryData.getId());
                    mGoodsData.setCategoryName(categoryData.getName());

                    // カテゴリ名表示にカテゴリ名を設定する。
                    mCategoryNameValue.setText(mGoodsData.getCategoryName());
                }
            }
        }

        mLogger.d("OUT(OUT)");
    }

    /**************************************************************************/
    /**
     * ダイアログボタンクリックリスナークラス
     *
     */
    private class ButtonOnClickListener implements OnClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(ButtonOnClickListener.class);

        /**
         * ボタンがクリックされた時に呼び出される。
         *
         * @param v クリックされたボタン
         */
        @Override
        public void onClick(View v) {
            mLogger.d("IN");

            // ボタンにより処理を判別する。
            switch (v.getId()) {
            // カテゴリ名ボタンの場合
            case R.id.categoryNameButton:
                mLogger.d("categoryNameButton");

                // カテゴリリスト画面を表示する。
                Intent intent = new Intent(getActivity(), CategoryListActivity.class);
                startActivityForResult(intent, RequestCode.CATEGORY_LIST);
                break;
            }

            mLogger.d("OUT(OUT)");
        }
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
            // 登録ボタンの場合
            case Dialog.BUTTON_POSITIVE: {
                mLogger.d("BUTTON_POSITIVE");

                // 入力値を取得する。
                String goodsNameValue = mGoodsNameValue.getText().toString();

                // 未入力の場合
                if (StringUtil.isNullOrEmpty(goodsNameValue)) {
                    toast("商品名が未入力です");
                    mLogger.w("OUT(NG)");
                    return;
                }

                ContentResolver resolver = getActivity().getContentResolver();

                {
                    // 新規登録の場合
                    if (0 == mGoodsData.getId()) {
                        // 登録済みか確認する。
                        String selection = GoodsTable.NAME + " = ?";
                        String[] selectionArgs = {goodsNameValue};
                        Cursor c = resolver.query(LowestProvider.GOODS_CONTENT_URI, null, selection, selectionArgs, null);
                        try {
                            // カーソルがある場合
                            if (null != c) {
                                // データがある場合
                                if (c.moveToFirst()) {
                                    toast("登録済みです");
                                    mLogger.w("OUT(NG)");
                                    return;
                                }
                            }
                        } finally {
                            if (null != c) {
                                c.close();
                            }
                        }
                    }
                }

                {
                    ContentValues values = new ContentValues();
                    values.put(GoodsTable.CATEGORY_ID,   mGoodsData.getCategoryId());
                    values.put(GoodsTable.CATEGORY_NAME, mGoodsData.getCategoryName());
                    values.put(GoodsTable.NAME,          goodsNameValue);

                    // 商品データIDを取得する。
                    long goodsId = mGoodsData.getId();

                    // 未登録の場合
                    if (0 == goodsId) {
                        Uri result = resolver.insert(LowestProvider.GOODS_CONTENT_URI, values);

                        // 登録に失敗した場合
                        if (null == result) {
                            toast("登録に失敗しました");
                            mLogger.w("OUT(NG)");
                            return;

                        // 登録に成功した場合
                        } else {
                            mGoodsData.setId(ContentUris.parseId(result));
                            mGoodsData.setName(goodsNameValue);
                        }

                    // 登録済みの場合
                    } else {
                        String selection = GoodsTable.ID + " = ?";
                        String[] selectionArgs = {String.valueOf(goodsId)};
                        int result =
                                resolver.update(LowestProvider.GOODS_CONTENT_URI, values, selection, selectionArgs);

                        // 更新に失敗した場合
                        if (1 != result) {
                            toast("更新に失敗しました");
                            mLogger.w("OUT(NG)");
                            return;

                        // 更新に成功した場合
                        } else {
                            mGoodsData.setName(goodsNameValue);
                        }
                    }
                }

                // 更新を通知する。
                mOnUpdateListener.onUpdate();

                // 終了する。
                dismiss();
                break;
            }

            // 削除ボタンの場合
            case Dialog.BUTTON_NEUTRAL: {
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
                    toast("商品データの削除に失敗しました");
                    mLogger.w("OUT(NG)");
                    return;
                }

                // 更新を通知する。
                mOnUpdateListener.onUpdate();

                // 終了する。
                dismiss();
                break;
            }

            // キャンセルボタンの場合
            case Dialog.BUTTON_NEGATIVE:
                mLogger.d("BUTTON_NEGATIVE");

                toast("キャンセルします");

                // 終了する。
                dismiss();
                break;
            }

            mLogger.d("OUT(OUT)");
        }
    }
}
