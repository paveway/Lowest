package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.CommonConstants.RequestCode;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.PriceData;
import info.paveway.lowest.data.ShopData;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
 * 価格編集ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class PriceEditDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(PriceEditDialog.class);

    /** 更新リスナー */
    private OnUpdateListener mOnUpdateListener;

    /** 価格データ */
    private PriceData mPriceData;

    /** 店名表示 */
    private TextView mShopNameValue;

    /** 数量入力 */
    private EditText mQuantityValue;

    /** 価格入力 */
    private EditText mPriceValue;

    /** メモ入力 */
    private EditText mMemoValue;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static PriceEditDialog newInstance(PriceData priceData) {
        PriceEditDialog instance = new PriceEditDialog();
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
     * 生成した時に呼び出される。
     *
     * @param savedInstanceState 保存した時のインスタンスの状態
     * @return ダイアログ
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mLogger.d("IN");

        // 引数を取得する。
        mPriceData = (PriceData)getArguments().getSerializable(ExtraKey.PRICE_DATA);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_price_edit, null);

        // 店名ボタンにリスナーを設定する。
        ((Button)rootView.findViewById(R.id.shopNameButton)).setOnClickListener(new ButtonOnClickListener());

        mShopNameValue = (TextView)rootView.findViewById(R.id.shopNameValue);
        mQuantityValue = (EditText)rootView.findViewById(R.id.quantityValue);
        mPriceValue    = (EditText)rootView.findViewById(R.id.priceValue);
        mMemoValue     = (EditText)rootView.findViewById(R.id.memoValue);

        // 更新の場合
        if (StringUtil.isNotNullOrEmpty(mPriceData.getShopName())) {
            mShopNameValue.setText(mPriceData.getShopName());
            mQuantityValue.setText(String.format("%.2f", mPriceData.getQuantity()));
            mPriceValue.setText(String.format("%d", mPriceData.getPrice()));
            mMemoValue.setText(mPriceData.getMemo());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("価格編集");
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

        // 要求コードが店リストかつ正常終了の場合
        if ((RequestCode.SHOP_LIST == requestCode) && (Activity.RESULT_OK == resultCode)) {
            // データがある場合
            if (null != data) {
                // 店データを取得する。
                ShopData shopData = (ShopData)data.getSerializableExtra(ExtraKey.SHOP_DATA);

                // 店データが取得できた場合
                if (null != shopData) {
                    // 価格データに設定する。
                    mPriceData.setShopId(shopData.getId());
                    mPriceData.setShopName(shopData.getName());

                    // 店名表示に店名を設定する。
                    mShopNameValue.setText(mPriceData.getShopName());
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
            // 店名ボタンの場合
            case R.id.shopNameButton:
                mLogger.d("shopNameButton");

                // 店リスト画面を表示する。
                Intent intent = new Intent(getActivity(), ShopListActivity.class);
                startActivityForResult(intent, RequestCode.SHOP_LIST);
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
            case Dialog.BUTTON_POSITIVE:
                mLogger.d("BUTTON_POSITIVE");

                // 入力値を取得する。
                String shopName       = mShopNameValue.getText().toString();
                String quantityString = mQuantityValue.getText().toString();
                String priceString    = mPriceValue.getText().toString();
                String memo           = mMemoValue.getText().toString();
                // 未入力の項目がある場合
                if ((null == shopName      ) || "".equals(shopName      ) ||
                    (null == quantityString) || "".equals(quantityString) ||
                    (null == priceString   ) || "".equals(priceString   )) {
                    toast("全て入力して下さい");
                    mLogger.w("OUT(NG)");
                    return;
                }

                // 数量を取得する。
                double quantity = 0.0D;
                try {
                    quantity = Double.parseDouble(quantityString);
                } catch (Exception e) {
                    mLogger.e(e);
                    toast("数量が不正な値です");
                    mLogger.w("OUT(NG)");
                    return;
                }

                // 価格を取得する。
                long price = 0L;
                try {
                    price = Long.parseLong(priceString);
                } catch (Exception e) {
                    mLogger.e(e);
                    toast("価格が不正な値です");
                    mLogger.w("OUT(NG)");
                    return;
                }

                ContentResolver resolver = getActivity().getContentResolver();

                {
                    // 登録済みか確認する。
                    String selection = PriceTable.SHOP_ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mPriceData.getShopId())};
                    Cursor c = resolver.query(LowestProvider.PRICE_CONTENT_URI, null, selection, selectionArgs, null);
                    try {
                        // カーソルがある場合
                        if (null != c) {
                            // データがある場合
                            if (c.moveToFirst()) {
                                // 価格IDを変更し、更新データとする。
                                mPriceData.setId(c.getLong(c.getColumnIndex(PriceTable.ID)));
                            }
                        }
                    } finally {
                        if (null != c) {
                            c.close();
                        }
                    }
                }

                {
                    ContentValues values = new ContentValues();
                    values.put(PriceTable.CATEGORY_ID,   mPriceData.getCagetoryId());
                    values.put(PriceTable.CATEGORY_NAME, mPriceData.getCategoryName());
                    values.put(PriceTable.GOODS_ID,      mPriceData.getGoodsId());
                    values.put(PriceTable.GOODS_NAME,    mPriceData.getGoodsName());
                    values.put(PriceTable.SHOP_ID,       mPriceData.getShopId());
                    values.put(PriceTable.SHOP_NAME,     mPriceData.getShopName());
                    values.put(PriceTable.QUANTITY,      quantity);
                    values.put(PriceTable.PRICE,         price);
                    values.put(PriceTable.MEMO,          memo);

                    // 価格データIDを取得する。
                    long priceId = mPriceData.getId();

                    // 未登録の場合
                    if (0 == priceId) {
                        Uri result = resolver.insert(LowestProvider.PRICE_CONTENT_URI, values);

                        // 登録に失敗した場合
                        if (null == result) {
                            toast("登録に失敗しました");
                            mLogger.w("OUT(NG)");
                            return;

                        // 登録に成功した場合
                        } else {
                            mPriceData.setId(ContentUris.parseId(result));
                            mPriceData.setQuantity(quantity);
                            mPriceData.setPrice(price);
                            mPriceData.setMemo(memo);
                        }

                    // 登録済みの場合
                    } else {
                        String selection = PriceTable.ID + " = ?";
                        String[] selectionArgs = {String.valueOf(priceId)};
                        int result =
                                resolver.update(LowestProvider.PRICE_CONTENT_URI, values, selection, selectionArgs);

                        // 更新に失敗した場合
                        if (1 != result) {
                            toast("更新に失敗しました");
                            mLogger.w("OUT(NG)");
                            return;

                        // 更新に成功した場合
                        } else {
                            mPriceData.setQuantity(quantity);
                            mPriceData.setPrice(price);
                            mPriceData.setMemo(memo);
                        }
                    }
                }

                // 更新を通知する。
                mOnUpdateListener.onUpdate();

                // 終了する。
                dismiss();
                break;

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
