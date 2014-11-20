package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.CategoryData;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.CategoryTable;
import info.paveway.lowest.dialog.CategoryDetailDialog;
import info.paveway.lowest.dialog.CategoryEditDialog;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * 最低価格記録アプリ
 * カテゴリリスト画面クラス
 *
 * @version 1.0 新規作成
 */
public class CategoryListActivity extends AbstractBaseActivity implements OnUpdateListener {

    /** ロガー */
    private Logger mLogger = new Logger(CategoryListActivity.class);

    /** カテゴリデータリスト */
    private List<CategoryData> mCategoryDataList;

    /** カテゴリリストアダプタ */
    private ArrayAdapter<String> mCategoryListAdapter;

    /**
     * 生成された時に呼び出される。
     *
     * @param savedInstanceState 保存した時のインスタンスの状態
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mLogger.d("IN");

        // スーパークラスのメソッドを呼び出す。
        super.onCreate(savedInstanceState);

        // レイアウトを設定する。
        setContentView(R.layout.activity_category_list);

        // ADビューをロードする。
        loadAdView();

        // カテゴリデータリストを取得する。
        getCategoryDataList();

        // 表示するカテゴリ名のリストを生成する。
        List<String> categoryNameList = new ArrayList<String>();
        for (CategoryData categoryData : mCategoryDataList) {
            categoryNameList.add(categoryData.getName());
        }

        // カテゴリリストビューを設定する。
        mCategoryListAdapter =
                new ArrayAdapter<String>(
                        CategoryListActivity.this, android.R.layout.simple_list_item_1, categoryNameList);
        ListView categoryListView = (ListView)findViewById(R.id.categoryListView);
        categoryListView.setAdapter(mCategoryListAdapter);
        categoryListView.setOnItemClickListener(new CategoryListOnItemClickListener());
        categoryListView.setOnItemLongClickListener(new CategoryListOnItemLongClickListener());

        // リスナーを設定する。
        ((Button)findViewById(R.id.addCategoryButton)).setOnClickListener(new ButtonOnClickListener());

        mLogger.d("OUT(OK)");
    }

    /**
     * リスタートしたときに呼び出される。
     */
    @Override
    protected void onRestart() {
        mLogger.d("IN");

        // スーパークラスのメソッドを呼び出す。
        super.onRestart();

        // カテゴリリストを更新する。
        updateCategoryList();

        mLogger.d("OUT(OK)");
    }

    /**
     * カテゴリリストを更新する。
     */
    private void updateCategoryList() {
        mLogger.d("IN");

        // カテゴリリストアダプタをクリアする。
        mCategoryListAdapter.clear();

        // カテゴリデータリストを取得する。
        getCategoryDataList();

        // カテゴリリストアダプタにカテゴリ名を再設定する。
        for (CategoryData categoryData : mCategoryDataList) {
            mCategoryListAdapter.add(categoryData.getName());
        }

        // カテゴリリストアダプタを更新する。
        mCategoryListAdapter.notifyDataSetChanged();

        mLogger.d("OUT(OK)");
    }

    /**
     * カテゴリデータリストを取得する。
     *
     */
    private void getCategoryDataList() {
        mLogger.d("IN");

        // カテゴリデータリストを生成する。
        mCategoryDataList = new ArrayList<CategoryData>();

        // カテゴリデータのカーソルを取得する。
        Cursor c = mResolver.query(LowestProvider.CATEGORY_CONTENT_URI, null, null, null, null);
        try {
            // カーソルが取得できた場合
            if (null != c) {
                // データがある場合
                if (c.moveToFirst()) {
                    do {
                        // カテゴリデータを生成し、データを設定する。
                        CategoryData categoryData = new CategoryData();
                        categoryData.setId(        c.getLong(  c.getColumnIndex(CategoryTable.ID)));
                        categoryData.setName(      c.getString(c.getColumnIndex(CategoryTable.NAME)));
                        categoryData.setUpdateTime(c.getLong(  c.getColumnIndex(CategoryTable.UPDATE_TIME)));

                        // カテゴリデータリストに追加する。
                        mCategoryDataList.add(categoryData);
                    } while (c.moveToNext());
                }
            }
        } finally {
            if (null != c) {
                c.close();
            }
        }

        mLogger.d("OUT(OK)");
    }

    /**
     * 更新された時に呼び出される。
     */
    @Override
    public void onUpdate() {
        mLogger.d("IN");

        // カテゴリリストを更新する。
        updateCategoryList();

        mLogger.d("OUT(OK)");
    }

    /**************************************************************************/
    /**
     * ボタンクリックリスナークラス
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

            // クリックされたボタンにより処理を判別する。
            switch (v.getId()) {
            // 追加ボタンの場合
            case R.id.addCategoryButton:
                // カテゴリ編集ダイアログを表示する。
                CategoryData categoryData = new CategoryData();
                FragmentManager manager = getSupportFragmentManager();
                CategoryEditDialog categoryEditDialog = CategoryEditDialog.newInstance(categoryData);
                categoryEditDialog.show(manager, CategoryEditDialog.class.getSimpleName());
                break;
            }

            mLogger.d("OUT(OK)");
        }
    }

    /**************************************************************************/
    /**
     * カテゴリリストアイテムクリックリスナークラス
     *
     */
    private class CategoryListOnItemClickListener implements OnItemClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(CategoryListOnItemClickListener.class);

        /**
         * リストアイテムがクリックされた時に呼び出される。
         *
         * @param parent 親のビュー
         * @param view 対象のビュー
         * @param position リストの位置
         * @param id 対象のビューのID
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mLogger.d("IN");

            // 呼び出し元画面に戻る。
            Intent intent = new Intent();
            intent.putExtra(ExtraKey.CATEGORY_DATA, mCategoryDataList.get(position));
            setResult(RESULT_OK, intent);
            finish();

            mLogger.d("OUT(OK)");
        }
    }

    /**************************************************************************/
    /**
     * カテゴリリストアイテムロングクリックリスナークラス
     *
     */
    private class CategoryListOnItemLongClickListener implements OnItemLongClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(CategoryListOnItemLongClickListener.class);

        /**
         * リストアイテムがロングクリックされた時に呼び出される。
         *
         * @param parent 親のビュー
         * @param view 対象のビュー
         * @param position リストの位置
         * @param id 対象のビューのID
         * @return 処理結果
         */
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            mLogger.d("IN");

            // 「未指定」データの場合
            if (0 == position) {
                // 変更不可とする。
                toast(R.string.error_change);

            // 上記以外
            } else {
                // カテゴリ詳細ダイアログを表示する。
                FragmentManager manager = getSupportFragmentManager();
                CategoryDetailDialog categoryDetailDialog = CategoryDetailDialog.newInstance(mCategoryDataList.get(position));
                categoryDetailDialog.show(manager, CategoryDetailDialog.class.getSimpleName());
            }

            mLogger.d("OUT(OK)");
            return true;
        }
    }


}
