package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.CommonConstants.RequestCode;
import info.paveway.lowest.data.GoodsData;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.GoodsTable;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.PriceData;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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

    /** 商品データリスト */
    private List<GoodsData> mGoodsDataList;

    /** 商品リストアダプタ */
    private GoodsListAdapter mGoodsListAdapter;

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

        // 商品追加ボタンにリスナーを設定する。
        ((Button)findViewById(R.id.addGoodsButton)).setOnClickListener(new ButtonOnClickListener());

        // 商品データリストを取得する。
        mGoodsDataList = getGoodsDataList();

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
        updateGoodsList();

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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }
        return false;
    }

    /**************************************************************************/
    /*** 内部メソッド                                                       ***/
    /**************************************************************************/
    /**
     * 商品データリストを取得する。
     *
     * @return 商品データリスト
     */
    private List<GoodsData> getGoodsDataList() {
        mLogger.d("IN");

        List<GoodsData> goodsDataList = new ArrayList<GoodsData>();

        // 商品データのカーソルを取得する。
        Cursor c = mResolver.query(LowestProvider.GOODS_CONTENT_URI, null, null, null, null);
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
    private void updateGoodsList() {
        mLogger.d("IN");

        // 商品データリストを再取得する。
        mGoodsDataList = getGoodsDataList();

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

        // 商品リストを更新する。
        updateGoodsList();

        mLogger.d("OUT(OK)");
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
