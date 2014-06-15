package info.paveway.lowest;

import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.LowestProvider.ShopTable;
import info.paveway.lowest.data.ShopData;

import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * 最低価格
 * 店編集画面
 *
 * @version 1.0 新規作成
 *
 */
public class ShopEditActivity extends AbstractBaseActivity {

    /** 店名 */
    private EditText mShopNameValue;

    /** 店データ */
    private ShopData mShopData;

    /**
     * 生成された時に呼び出される。
     *
     * @param savendInstanceState 保存した時のインスタンスの状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // スーパークラスのメソッドを呼び出す。
        super.onCreate(savedInstanceState);

        // レイアウトを設定する。
        setContentView(R.layout.activity_shop_edit);

        // インテントを取得する。
        Intent intent = getIntent();
        // インテントが取得できない場合
        if (null == intent) {
            // 終了する。
            toast(R.string.illeagal_transition);
            finish();
            return;
        }

        // 引継ぎデータを取得する。
        mShopData = (ShopData)intent.getSerializableExtra(ExtraKey.SHOP_DATA);
        // 引継ぎデータが取得できない場合
        if (null == mShopData) {
            // 終了する。
            toast(R.string.illeagal_transition);
            finish();
            return;
        }

        // 各ウィジットを設定する。
        ((Button)findViewById(R.id.registButton)).setOnClickListener(new ButtonOnClickListener());
        Button deleteButton = (Button)findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new ButtonOnClickListener());
        ((Button)findViewById(R.id.clearButton)).setOnClickListener(new ButtonOnClickListener());

        mShopNameValue = (EditText)findViewById(R.id.shopNameValue);

        // 店名を取得する。
        String shopName = mShopData.getName();

        // 追加の場合
        if (StringUtil.isNullOrEmpty(shopName)) {
            // 削除ボタンを無効にする。
            deleteButton.setEnabled(false);

        // 編集の場合
        } else {
            // 店名を設定する。
            mShopNameValue.setText(shopName);
        }

    }

    /******************************************************************************************************************/
    /**
     * ボタンクリックリスナークラス
     *
     */
    private class ButtonOnClickListener implements OnClickListener {

        /**
         * ボタンがクリックされた時に呼び出される。
         *
         * @param v クリックされたボタン
         */
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            // 登録ボタンの場合
            case R.id.registButton:
                // 入力値を取得する。
                String shopName = mShopNameValue.getText().toString();

                // 未入力かチェックする。
                if (StringUtil.isNullOrEmpty(shopName)) {
                    toast("カテゴリ名が未入力です");
                    return;
                }

                // 登録済みかチェックする。
                {
                    String selection = ShopTable.NAME + " = ?";
                    String[] selectionArgs = {shopName};
                    Cursor c = mResolver.query(LowestProvider.SHOP_CONTENT_URI, null, selection, selectionArgs, null);
                    try {
                        if (null != c) {
                            if (c.moveToFirst()) {
                                toast("登録済みです");
                                return;
                            }
                        }
                    } finally {
                        c.close();
                    }
                }

                // 店を登録する。
                long updateTime = new Date().getTime();
                ContentValues values = new ContentValues();
                values.put(ShopTable.NAME, shopName);
                values.put(ShopTable.UPDATE_TIME, updateTime);

                // 新規登録の場合
                if (-1 == mShopData.getId()) {
                    Uri result = mResolver.insert(LowestProvider.SHOP_CONTENT_URI, values);

                    // エラーの場合
                    if (null == result) {
                        toast("登録に失敗しました");
                   }


                // 更新の場合
                } else {
                    String selection = ShopTable.ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mShopNameValue.getId())};
                    int result = mResolver.update(LowestProvider.SHOP_CONTENT_URI, values, selection, selectionArgs);
                    if (1 != result) {
                        toast("更新に失敗しました");
                    }
                }

                // 終了する。
                finish();
                return;

            // 削除ボタンの場合
            case R.id.deleteButton:
                // 削除確認ダイアログを表示する。
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopEditActivity.this);
                builder.setTitle(R.string.delete_dialog_title);
                builder.setMessage(R.string.delete_dialog_message);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setPositiveButton(R.string.delete_dialog_positive_button, new DialogButtonOnClickListener());
                builder.setNegativeButton(R.string.delete_dialog_negative_button, null);
                builder.setCancelable(true);
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                break;

            // クリアボタンの場合
            case R.id.clearButton:
                mShopNameValue.setText("");
                break;
            }
        }

        /**
         * ボタンクリックリスナークラス
         */
        private class DialogButtonOnClickListener implements DialogInterface.OnClickListener {

            /**
             * ボタンがクリックされた時に呼び出される。
             *
             * @param dialog ダイアログ
             * @param which クリックされたボタン
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 操作リストを生成する。
                ArrayList<ContentProviderOperation> operationList =
                        new ArrayList<ContentProviderOperation>();
                ContentProviderOperation.Builder builder = null;

                // 店テーブルのデータを削除する。
                {
                    builder = ContentProviderOperation.newDelete(LowestProvider.SHOP_CONTENT_URI);
                    String selection = ShopTable.ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mShopNameValue.getId())};
                    builder.withSelection(selection, selectionArgs);
                    operationList.add(builder.build());
                }

                // 価格テーブルのデータで該当の店データを削除する。
                {
                    builder = ContentProviderOperation.newDelete(LowestProvider.PRICE_CONTENT_URI);
                    String selection = PriceTable.SHOP_ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mShopNameValue.getId())};
                    builder.withSelection(selection, selectionArgs);
                    operationList.add(builder.build());
                }

                // バッチ処理を行う。
                try {
                    mResolver.applyBatch(LowestProvider.AUTHORITY, operationList);
                } catch (Exception e) {
                    toast("削除に失敗しました");
                }

                // 終了する。
                finish();
            }
        }
    }
}
