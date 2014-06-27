package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.CommonConstants.RequestCode;
import info.paveway.lowest.data.CategoryData;
import info.paveway.lowest.data.GoodsData;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.CategoryTable;
import info.paveway.lowest.data.LowestProvider.GoodsTable;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.PriceData;
import info.paveway.lowest.dialog.GoodsDetailDialog;
import info.paveway.lowest.dialog.GoodsEditDialog;
import info.paveway.lowest.dialog.InfoDialog;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 最低価格記録アプリ
 * メイン画面クラス
 *
 * @version 1.0 新規作成
 */
public class MainActivity extends AbstractBaseActivity implements OnUpdateListener {

    /** ロガー */
    private Logger mLogger = new Logger(MainActivity.class);

    /** カテゴリデータリスト */
    private List<CategoryData> mCategoryDataList;

    /** 商品データリスト */
    private List<GoodsData> mGoodsDataList;

    /** 商品リストアダプタ */
    private GoodsListAdapter mGoodsListAdapter;

    private int mCategoryFilterPostion;

    /**
     * 生成された時に呼び出される。
     *
     * @param savedInstanceState 保存した時のインスタンスの状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLogger.d("IN");

        // スーパークラスのメソッドを呼び出す。
        super.onCreate(savedInstanceState);

        // レイアウトを設定する。
        setContentView(R.layout.activity_main);

        // カテゴリデータリストを取得する。
        getCategoryDataList();

        String[] categoryDatas = new String[mCategoryDataList.size()];
        for (int i = 0; i < mCategoryDataList.size(); i++) {
            categoryDatas[i] = mCategoryDataList.get(i).getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, categoryDatas);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(adapter, new CategoryFilterOnNavigationListener());

        // 商品追加ボタンにリスナーを設定する。
        ((Button)findViewById(R.id.addGoodsButton)).setOnClickListener(new ButtonOnClickListener());

        // 商品データリストを取得する。
        mCategoryFilterPostion = 0;
        mGoodsDataList = getGoodsDataList(mCategoryFilterPostion);

        // 商品リストビューを設定する。
        mGoodsListAdapter = new GoodsListAdapter(this, 0, mGoodsDataList);
        ListView goodsListView = (ListView)findViewById(R.id.goodsListView);
        goodsListView.setAdapter(mGoodsListAdapter);
        goodsListView.setOnItemClickListener(    new GoodsListOnItemClickListener());
        goodsListView.setOnItemLongClickListener(new GoodsListOnItemLongClickListener());

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

        // 商品リストを更新する。
        updateGoodsList(mCategoryFilterPostion);

        mLogger.d("OUT(OK)");
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
     * メニューアイテムが選択された時に呼び出される。
     *
     * @param item メニューアイテム
     * @return 処理結果
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // メニューにより処理を判別する。
        switch (item.getItemId()) {
        // バージョン情報の場合
        case R.id.menu_info:
            // バージョン情報ダイアログを表示する。
            FragmentManager manager = getSupportFragmentManager();
            InfoDialog infoDialog = InfoDialog.newInstance();
            infoDialog.show(manager, InfoDialog.class.getSimpleName());
            break;
        }
        return false;
    }

    /**************************************************************************/
    /*** 内部メソッド                                                       ***/
    /**************************************************************************/
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
     * 商品データリストを取得する。
     *
     * @return 商品データリスト
     */
    private List<GoodsData> getGoodsDataList(long categoryId) {
        mLogger.d("IN");

        List<GoodsData> goodsDataList = new ArrayList<GoodsData>();

        String selection = null;
        String[] selectionArgs = null;
        if (1 < categoryId) {
            selection = GoodsTable.CATEGORY_ID + " = ?";
            selectionArgs = new String[]{String.valueOf(categoryId)};
        }

        // 商品データのカーソルを取得する。
        Cursor c = mResolver.query(LowestProvider.GOODS_CONTENT_URI, null, selection, selectionArgs, null);
        try {
            // カーソルが取得できた場合
            if (null != c) {
                // データがある場合
                if (c.moveToFirst()) {
                    do {
                        // 商品データを生成し、データを設定する。
                        GoodsData goodsData = new GoodsData();
                        goodsData.setId(          c.getLong(  c.getColumnIndex(GoodsTable.ID)));
                        goodsData.setCategoryId(  c.getLong(  c.getColumnIndex(GoodsTable.CATEGORY_ID)));
                        goodsData.setCategoryName(c.getString(c.getColumnIndex(GoodsTable.CATEGORY_NAME)));
                        goodsData.setName(        c.getString(c.getColumnIndex(GoodsTable.NAME)));
                        goodsData.setMemo(        c.getString(c.getColumnIndex(GoodsTable.MEMO)));
                        goodsData.setUpdateTime(  c.getLong(  c.getColumnIndex(GoodsTable.UPDATE_TIME)));

                        // 価格データを設定する。
                        setPriceData(goodsData);

                        // 商品データリストに追加する。
                        goodsDataList.add(goodsData);
                    } while (c.moveToNext());

                } else {
                    mLogger.w("No category data.");
                }

            } else {
                mLogger.w("Cursor is null.");
            }
        } finally {
            if (null != c) {
                c.close();
            }
        }

        mLogger.d("OUT(OK)");
        return goodsDataList;
    }

    /**
     * 価格データを設定する。
     *
     * @param goodsData 商品データ
     */
    private void setPriceData(GoodsData goodsData) {
        mLogger.d("IN");

        // 商品IDを取得する。
        long goodsId = goodsData.getId();
        mLogger.d("goodsId=[" + goodsId + "]");

        // 検索条件を設定する。
        String selection = PriceTable.GOODS_ID + " = ?";
        String[] selectionArgs = {String.valueOf(goodsId)};

        // 価格データのカーソルを取得する。
        Cursor c = mResolver.query(LowestProvider.PRICE_CONTENT_URI, null, selection, selectionArgs, null);
        try {
            // カーソルが取得できた場合
            if (null != c) {
                // データがある場合
                if (c.moveToFirst()) {
                    do {
                        // 価格データを生成し、データを設定する。
                        PriceData priceData = new PriceData();
                        priceData.setId(          c.getLong(  c.getColumnIndex(PriceTable.ID)));
                        priceData.setCategoryId(  goodsData.getCategoryId());
                        priceData.setCategoryName(goodsData.getCategoryName());
                        priceData.setGoodsId(     goodsId);
                        priceData.setGoodsName(   goodsData.getName());
                        priceData.setShopId(      c.getLong(  c.getColumnIndex(PriceTable.SHOP_ID)));
                        priceData.setShopName(    c.getString(c.getColumnIndex(PriceTable.SHOP_NAME)));
                        priceData.setQuantity(    c.getDouble(c.getColumnIndex(PriceTable.QUANTITY)));
                        priceData.setPrice(       c.getLong(  c.getColumnIndex(PriceTable.PRICE)));
                        priceData.setUpdateTime(  c.getLong(  c.getColumnIndex(PriceTable.UPDATE_TIME)));

                        // 商品データに価格データを追加する。
                        goodsData.addPriceData(priceData);
                    } while (c.moveToNext());

                } else {
                    mLogger.w("No category data.");
                }

            } else {
                mLogger.w("Cursor is null.");
            }
        } finally {
            if (null != c) {
                c.close();
            }
        }

        mLogger.d("OUT(OK)");
    }

    /**
     * 商品リストを更新する。
     */
    private void updateGoodsList(long categoryId) {
        mLogger.d("IN");

        // 商品データリストを再取得する。
        mGoodsDataList = getGoodsDataList(categoryId);

        // 商品リストアダプタを再設定する。
        mGoodsListAdapter.clear();
        mGoodsListAdapter.addAll(mGoodsDataList);
        mGoodsListAdapter.notifyDataSetChanged();

        mLogger.d("OUT(OK)");
    }

    /**
     * 更新された時に呼び出される。
     */
    @Override
    public void onUpdate() {
        mLogger.d("IN");

        // カテゴリリストを取得する。
        getCategoryDataList();

        // カテゴリIDを取得する。
        long categoryId = 0;
        // カテゴリが削除された場合を考慮してチェックする。
        if (mCategoryFilterPostion < mCategoryDataList.size()) {
            // カテゴリIDを取得する(ただしカテゴリが削除された場合、前回選択したカテゴリとは限らない)
            categoryId = mCategoryDataList.get(mCategoryFilterPostion).getId();
        }

        // 商品リストを更新する。
        updateGoodsList(categoryId);

        mLogger.d("OUT(OK)");
    }

    /**************************************************************************/
    /**
     * カテゴリフィルターナビゲーションリスナークラス
     *
     */
    public class CategoryFilterOnNavigationListener implements ActionBar.OnNavigationListener {

        /** ロガー */
        private Logger mLogger = new Logger(CategoryFilterOnNavigationListener.class);

        /**
         * ナビゲーションアイテムが選択された時に呼び出される。
         *
         * @param itemPosition 選択されたアイテムの位置
         * @param itemId 選択されたアイテムのID
         */
        @Override
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            mLogger.d("IN");

            // 選択位置を保存する。
            mCategoryFilterPostion = itemPosition;

            // カテゴリリストを取得する。
            getCategoryDataList();

            // 商品データリストを更新する。
            updateGoodsList(mCategoryDataList.get(itemPosition).getId());

            mLogger.d("OUT(OK)");
            return true;
        }
    }

    /**************************************************************************/
    /**
     * 商品リストアダプタークラス
     *
     */
    public class GoodsListAdapter extends ArrayAdapter<GoodsData> {

        /** ロガー */
        private Logger mLogger = new Logger(GoodsListAdapter.class);

        /** レイアウトインフレーター */
        private LayoutInflater mLayoutInflater;

        /**
         * コンストラクタ
         *
         * @param context コンテキスト
         * @param textViewResourceId テキストビューリソースID
         * @param objects 商品データリスト
         */
        public GoodsListAdapter(Context context, int textViewResourceId, List<GoodsData> objects) {
            // スーパークラスのコンストラクタを呼び出す。
            super(context, textViewResourceId, objects);

            mLogger.d("IN");

            // レイアウトインフレーターを取得する。
            mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            mLogger.d("OUT(OK)");
        }

        /**
         * ビューを返却する。
         *
         * @param position リストの位置
         * @param convertView リストに設定されるビュー
         * @param parent 親のビュー
         * @return 変更されたビュー
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            mLogger.d("IN");

            // リスト位置の商品データを取得する。
            GoodsData goodsData = (GoodsData)getItem(position);

            // リストに設定されるビューが無い場合
            if (null == convertView) {
                // 新しく生成する。
                convertView = mLayoutInflater.inflate(R.layout.row_goods_list, null);
            }

            // 各ウィジットを設定する。
            TextView goodsNameValue = (TextView)convertView.findViewById(R.id.goodsNameValue);
            TextView unitPriceValue = (TextView)convertView.findViewById(R.id.unitPriceValue);
            TextView shopNameValue  = (TextView)convertView.findViewById(R.id.shopNameValue);

            goodsNameValue.setText(goodsData.getName());

            // 価格データを取得する。
            PriceData priceData = goodsData.getLowestPriceData();

            // 価格データが取得できた場合
            if (null != priceData) {
                unitPriceValue.setText(String.format("@%.2f", priceData.getUnitPrice()));
                shopNameValue.setText(priceData.getShopName());
            }

            mLogger.d("OUT(OK)");
            return convertView;
        }
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

            // ボタンにより処理を判別する。
            switch (v.getId()) {
            // 商品追加ボタンの場合
            case R.id.addGoodsButton:
                mLogger.d("addGoodsButton");

                // 商品編集ダイアログを表示する。
                GoodsData goodsData = new GoodsData();
                FragmentManager manager = getSupportFragmentManager();
                GoodsEditDialog goodsEditDialog = GoodsEditDialog.newInstance(goodsData);
                goodsEditDialog.show(manager, GoodsEditDialog.class.getSimpleName());
                break;
            }

            mLogger.d("OUT(OK)");
        }
    }

    /**************************************************************************/
    /**
     * 商品リストアイテムクリックリスナークラス
     *
     */
    private class GoodsListOnItemClickListener implements OnItemClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(GoodsListOnItemClickListener.class);

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

            // 価格リスト画面を呼び出す。
            Intent intent = new Intent(MainActivity.this, PriceListActivity.class);
            intent.putExtra(ExtraKey.GOODS_DATA, mGoodsDataList.get(position));
            startActivityForResult(intent, RequestCode.PRICE_LIST);

            mLogger.d("OUT(OK)");
        }
    }

    /**************************************************************************/
    /**
     * 商品リストアイテムロングクリックリスナークラス
     *
     */
    private class GoodsListOnItemLongClickListener implements OnItemLongClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(GoodsListOnItemLongClickListener.class);

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

            // 商品詳細ダイアログを表示する。
            FragmentManager manager = getSupportFragmentManager();
            GoodsDetailDialog goodsDetailDialog = GoodsDetailDialog.newInstance(mGoodsDataList.get(position));
            goodsDetailDialog.show(manager, GoodsDetailDialog.class.getSimpleName());

            mLogger.d("OUT(OK)");
            return true;
        }
    }
}
