package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.ShopTable;
import info.paveway.lowest.data.ShopData;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * 最低価格記録アプリ
 * 店詳細ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class ShopEditDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(ShopEditDialog.class);

    /** 更新リスナー */
    private OnUpdateListener mOnUpdateListener;

    /** 店データ */
    private ShopData mShopData;

    /** 店名表示 */
    private EditText mShopNameValue;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static ShopEditDialog newInstance(ShopData shopData) {
        ShopEditDialog instance = new ShopEditDialog();
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
     * 生成した時に呼び出される。
     *
     * @param savedInstanceState 保存した時のインスタンスの状態
     * @return ダイアログ
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mLogger.d("IN");

        // 引数を取得する。
        mShopData = (ShopData)getArguments().getSerializable(ExtraKey.SHOP_DATA);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_shop_edit, null);

        mShopNameValue = (EditText)rootView.findViewById(R.id.shopNameValue);
        mShopNameValue.setText(mShopData.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("店編集");
        builder.setPositiveButton("登録", new DialogButtonOnClickListener());
        builder.setNegativeButton("キャンセル", new DialogButtonOnClickListener());
        builder.setView(rootView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        mLogger.d("OUT(OUT)");
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
            // 変更ボタンの場合
            case Dialog.BUTTON_POSITIVE: {
                mLogger.d("BUTTON_POSITIVE");

                // 店名を取得する。
                String shopName = mShopNameValue.getText().toString();

                // 店名が未入力の場合
                if (StringUtil.isNullOrEmpty(shopName)) {
                    // 終了する。
                    toast("店名が未入力です");
                    mLogger.w("OUT(NG)");
                    return;
                }

                ContentResolver resolver = getActivity().getContentResolver();

                {
                    // 登録済みか確認する。
                    String selection = ShopTable.NAME + " = ?";
                    String[] selectionArgs = {shopName};
                    Cursor c = resolver.query(LowestProvider.CATEGORY_CONTENT_URI, null, selection, selectionArgs, null);
                    boolean existFlg = false;
                    try {
                        // カーソルがある場合
                        if (null != c) {
                            // データがある場合
                            if (c.moveToFirst()) {
                                // 登録済みとする。
                                existFlg = true;
                            }
                        }
                    } finally {
                        if (null != c) {
                            c.close();
                        }
                    }

                    // 登録済みの場合
                    if (existFlg) {
                        // 終了する。
                        toast("登録済みです");
                        mLogger.w("OUT(NG)");
                        return;
                    }
                }

                {
                    long shopId = mShopData.getId();
                    long updateTime = new Date().getTime();
                    ContentValues values = new ContentValues();
                    values.put(ShopTable.NAME, shopName);
                    values.put(ShopTable.UPDATE_TIME, updateTime);

                    // 新規登録の場合
                    if (0 == shopId) {
                        Uri result = resolver.insert(LowestProvider.SHOP_CONTENT_URI, values);
                        if (null == result) {
                            toast("登録に失敗しました");
                            mLogger.w("OUT(NG)");
                            return;
                        }

                    // 更新の場合
                    } else {
                        String selection = ShopTable.ID + " = ?";
                        String[] selectionArgs = {String.valueOf(shopId)};
                        int result = resolver.update(LowestProvider.SHOP_CONTENT_URI, values, selection, selectionArgs);
                        if (0 == result) {
                            toast("更新に失敗しました");
                            mLogger.w("OUT(NG)");
                            return;
                        }
                    }
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
