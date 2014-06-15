package info.paveway.lowest;

import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.CategoryData;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.CategoryTable;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * 最低価格
 * メイン画面
 *
 * @version 1.0 新規作成
 *
 */
public class MainActivity extends AbstractBaseActivity {

    /** カテゴリデータリスト */
    protected List<CategoryData> mCategoryDataList;

    /** カテゴリリストアダプタ */
    private ArrayAdapter<String> mCategoryListAdapter;

    /**
     * 生成された時に呼び出される。
     *
     * @param savedInstanceState 保存された時のインスタンスの状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // スーパークラスのメソッドを呼び出す。
        super.onCreate(savedInstanceState);

        // レイアウトを設定する。
        setContentView(R.layout.activity_main);

        // カテゴリデータリストを取得する。
        mCategoryDataList = getCategoryDataList();

        // 表示するカテゴリ名のリストを生成する。
        List<String> categoryNameList = new ArrayList<String>();
        for (CategoryData data : mCategoryDataList) {
            categoryNameList.add(data.getName());
        }

        // 各ウィジットを設定する。
        ((Button)findViewById(R.id.addCategoryButton)).setOnClickListener(new ButtonOnClickListener());

        // カテゴリリストビューを設定する。
        mCategoryListAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, categoryNameList);
        ListView categoryListView = (ListView)findViewById(R.id.categoryListView);
        categoryListView.setAdapter(mCategoryListAdapter);
        categoryListView.setOnItemClickListener(new OnItemClickListener() {
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
                // リストアイテムがクリックされた時の処理を行う。
                itemClick(parent, view, position, id);
            }
        });
        categoryListView.setOnItemLongClickListener(new CategoryListOnItemLongClickListener());
    }

    /**
     * リスタートしたときに呼び出される。
     */
    @Override
    protected void onRestart() {
        // スーパークラスのメソッドを呼び出す。
        super.onRestart();

        // カテゴリリストアダプタをクリアする。
        mCategoryListAdapter.clear();

        // カテゴリデータリストを取得する。
        mCategoryDataList = getCategoryDataList();

        // カテゴリリストアダプタにカテゴリ名を再設定する。
        for (CategoryData data : mCategoryDataList) {
            mCategoryListAdapter.add(data.getName());
        }

        // カテゴリリストアダプタを更新する。
        mCategoryListAdapter.notifyDataSetChanged();
    }

    /**
     * メニューが生成される時に呼び出される。
     *
     * @param menu メニュー
     * @return 処理結果
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * カテゴリデータリストを取得する。
     *
     * @return カテゴリデータリスト
     */
    private List<CategoryData> getCategoryDataList() {
        List<CategoryData> categoryDataList = new ArrayList<CategoryData>();
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
                        categoryDataList.add(categoryData);
                    } while (c.moveToNext());
                }
            }
        } finally {
            if (null != c) {
                c.close();
            }
        }
        return categoryDataList;
    }

    /**
     * リストアイテムがクリックされた時に呼び出される。
     * サブクラスでオーバーライドする。
     *
     * @param parent 親のビュー
     * @param view 対象のビュー
     * @param position リストの位置
     * @param id 対象のビューのID
     */
    protected void itemClick(AdapterView<?> parent, View view, int position, long id) {
        // 商品リスト画面を呼び出す。
        Intent intent = new Intent(MainActivity.this, GoodsListActivity.class);
        intent.putExtra(ExtraKey.CATEGORY_DATA, mCategoryDataList.get(position));
        startActivity(intent);
    }

    /**************************************************************************/
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
            // ボタンにより処理を判別する。
            switch (v.getId()) {
            // カテゴリ追加ボタンの場合
            case R.id.addCategoryButton:
                // カテゴリ編集画面を呼び出す。
                Intent intent = new Intent(MainActivity.this, CategoryEditActivity.class);
                CategoryData categoryData = new CategoryData();
                categoryData.setId(-1);
                intent.putExtra(ExtraKey.CATEGORY_DATA, categoryData);
                startActivity(intent);
                break;

            // 上記以外
            default:
                // 何もしない。
                break;
            }
        }
    }

    /**************************************************************************/
    /**
     * カテゴリリストアイテムロングクリックリスナークラス
     *
     */
    private class CategoryListOnItemLongClickListener implements OnItemLongClickListener {

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
            // 「カテゴリ無」データの場合
            if (0 == position) {
                // 変更不可とする。
                toast("このデータは変更できません");

            // 上記以外
            } else {
                // カテゴリ編集画面を呼び出す。
                Intent intent = new Intent(MainActivity.this, CategoryEditActivity.class);
                intent.putExtra(ExtraKey.CATEGORY_DATA, mCategoryDataList.get(position));
                startActivity(intent);
            }

            return true;
        }
    }
}
