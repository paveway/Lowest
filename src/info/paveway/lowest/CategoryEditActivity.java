package info.paveway.lowest;

import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.CategoryData;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.CategoryTable;
import info.paveway.lowest.data.LowestProvider.GoodsTable;
import info.paveway.lowest.data.LowestProvider.PriceTable;

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
 * カテゴリ編集画面
 *
 * @version 1.0 新規作成
 *
 */
public class CategoryEditActivity extends AbstractBaseActivity {

    /** カテゴリ名 */
    private EditText mCategoryNameValue;

    /** カテゴリデータ */
    private CategoryData mCategoryData;

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
        setContentView(R.layout.activity_category_edit);

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
        mCategoryData = (CategoryData)intent.getSerializableExtra(ExtraKey.CATEGORY_DATA);
        // 引継ぎデータが取得できない場合
        if (null == mCategoryData) {
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

        mCategoryNameValue = (EditText)findViewById(R.id.categoryNameValue);

        // カテゴリ名を取得する。
        String categoryName = mCategoryData.getName();

        // 追加の場合
        if (StringUtil.isNullOrEmpty(categoryName)) {
            // 削除ボタンを無効にする。
            deleteButton.setEnabled(false);

        // 編集の場合
        } else {
            // カテゴリ名を設定する。
            mCategoryNameValue.setText(categoryName);
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
                String categoryName = mCategoryNameValue.getText().toString();

                // 未入力かチェックする。
                if (StringUtil.isNullOrEmpty(categoryName)) {
                    toast("カテゴリ名が未入力です");
                    return;
                }

                // 登録済みかチェックする。
                {
                    String selection = CategoryTable.NAME + " = ?";
                    String[] selectionArgs = {categoryName};
                    Cursor c = mResolver.query(LowestProvider.CATEGORY_CONTENT_URI, null, selection, selectionArgs, null);
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

                // カテゴリを登録する。
                long updateTime = new Date().getTime();
                ContentValues values = new ContentValues();
                values.put(CategoryTable.NAME, categoryName);
                values.put(CategoryTable.UPDATE_TIME, updateTime);

                // 新規登録の場合
                if (-1 == mCategoryData.getId()) {
                    Uri result = mResolver.insert(LowestProvider.CATEGORY_CONTENT_URI, values);

                    // エラーの場合
                    if (null == result) {
                        toast("登録に失敗しました");
                   }


                // 更新の場合
                } else {
                    String selection = CategoryTable.ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mCategoryData.getId())};
                    int result = mResolver.update(LowestProvider.CATEGORY_CONTENT_URI, values, selection, selectionArgs);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(CategoryEditActivity.this);
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
                mCategoryNameValue.setText("");
                break;
            }
        }

        /**
         * ダイアログボタンクリックリスナークラス
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

                // カテゴリテーブルのデータを削除する。
                {
                    builder = ContentProviderOperation.newDelete(LowestProvider.CATEGORY_CONTENT_URI);
                    String selection = CategoryTable.ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mCategoryData.getId())};
                    builder.withSelection(selection, selectionArgs);
                    operationList.add(builder.build());
                }

                // 更新日時を取得する。
                long updateTime = new Date().getTime();

                // 商品テーブルのデータで該当カテゴリのデータをカテゴリ無に更新する。
                {
                    // 商品テーブルの該当カテゴリのデータを検索する。
                    String selection = GoodsTable.CATEGORY_ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mCategoryData.getId())};
                    Cursor c =
                            getContentResolver().query(
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
                                builder.withValue(GoodsTable.CATEGORY_ID, 0);
                                builder.withValue(GoodsTable.CATEGORY_NAME, LowestProvider.CATEGORY_NAME_DEFAULT_VALUE);
                                builder.withValue(GoodsTable.UPDATE_TIME, updateTime);
                                operationList.add(builder.build());
                            } while (c.moveToNext());
                        }
                    }
                }

                // 価格テーブルのデータで該当カテゴリのデータをカテゴリ無に更新する。
                {
                    // 価格テーブルの該当カテゴリのデータを検索する。
                    String selection = PriceTable.CATEGORY_ID + " = ?";
                    String[] selectionArgs = {String.valueOf(mCategoryData.getId())};
                    Cursor c =
                            getContentResolver().query(
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
                                builder.withValue(PriceTable.CATEGORY_ID, 0);
                                builder.withValue(PriceTable.CATEGORY_NAME, LowestProvider.CATEGORY_NAME_DEFAULT_VALUE);
                                builder.withValue(PriceTable.UPDATE_TIME, updateTime);
                                operationList.add(builder.build());
                            } while (c.moveToNext());
                        }
                    }
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
