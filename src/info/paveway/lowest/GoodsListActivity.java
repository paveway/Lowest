package info.paveway.lowest;

import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.CategoryData;
import info.paveway.lowest.data.GoodsData;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.GoodsTable;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.PriceData;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
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
 * 最低価格
 * 商品リスト画面
 *
 * @version 1.0 新規作成
 *
 */
public class GoodsListActivity extends AbstractBaseActivity {

    /** カテゴリデータ */
    private CategoryData mCategoryData;

    /** 商品データリスト */
    private List<GoodsData> mGoodsDataList;

    /** 商品リストアダプタ */
    private GoodsListAdapter mGoodsListAdapter;

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
        setContentView(R.layout.activity_goods_list);

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

        // 商品リストを取得する。
        mGoodsDataList = getGoodsDataList();

        // 各ウィジットを設定する。
        ((Button)findViewById(R.id.addGoodsButton)).setOnClickListener(new ButtonOnClickListener());
        ((TextView)findViewById(R.id.categoryNameValue)).setText(mCategoryData.getName());

        // 商品リストビューを設定する。
        mGoodsListAdapter = new GoodsListAdapter(GoodsListActivity.this, 0, mGoodsDataList);
        ListView goodsListView = (ListView)findViewById(R.id.goodsListView);
        goodsListView.setAdapter(mGoodsListAdapter);
        goodsListView.setOnItemClickListener(    new GoodsListOnItemClickListener());
        goodsListView.setOnItemLongClickListener(new GoodsListOnItemLongClickListener());
    }

    /**
     * リスタートしたときに呼び出される。
     */
    @Override
    protected void onRestart() {
        // スーパークラスのメソッドを呼び出す。
        super.onRestart();

        // 商品リストアダプタをクリアする。
        mGoodsListAdapter.clear();

        // 商品データリストを取得する。
        mGoodsDataList = getGoodsDataList();

        // 商品リストアダプタに商品データを再設定する。
        mGoodsListAdapter.addAll(mGoodsDataList);

        // 商品リストアダプタを更新する。
        mGoodsListAdapter.notifyDataSetChanged();
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
     * 商品データリストを取得する。
     *
     * @return 商品データリスト
     */
    private List<GoodsData> getGoodsDataList() {
        List<GoodsData> goodsDataList = new ArrayList<GoodsData>();
        // 商品データのカーソルを取得する。
        String selection = GoodsTable.CATEGORY_ID + " = ?";
        long categoryId = mCategoryData.getId();
        String[] selectionArgs = new String[]{String.valueOf(categoryId)};
        Cursor c = mResolver.query(LowestProvider.GOODS_CONTENT_URI, null, selection, selectionArgs, null);
        try {
            // カーソルが取得できた場合
            if (null != c) {
                // データがある場合
                if (c.moveToFirst()) {
                    String categoryName = mCategoryData.getName();
                    do {
                        // 商品データを生成し、データを設定する。
                        GoodsData goodsData = new GoodsData();
                        goodsData.setId(          c.getLong(  c.getColumnIndex(GoodsTable.ID)));
                        goodsData.setCategoryId(  categoryId);
                        goodsData.setCategoryName(categoryName);
                        goodsData.setName(        c.getString(c.getColumnIndex(GoodsTable.NAME)));
                        goodsData.setMemo(        c.getString(c.getColumnIndex(GoodsTable.MEMO)));
                        goodsData.setUpdateTime(  c.getLong(  c.getColumnIndex(GoodsTable.UPDATE_TIME)));

                        // 価格データを設定する。
                        setPriceData(goodsData);

                        // 商品データリストに追加する。
                        goodsDataList.add(goodsData);
                    } while (c.moveToNext());
                }
            }
        } finally {
            if (null != c) {
                c.close();
            }
        }
        return goodsDataList;
    }

    /**
     * 価格データを設定する。
     *
     * @param goodsData 商品データ
     */
    private void setPriceData(GoodsData goodsData) {
        long goodsId = goodsData.getId();
        String selection = PriceTable.GOODS_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(goodsId)};
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
                }
            }
        } finally {
            if (null != c) {
                c.close();
            }
        }
    }

    /**************************************************************************/
    /**
     * 商品リストアダプタークラス
     *
     */
    public class GoodsListAdapter extends ArrayAdapter<GoodsData> {

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

            // レイアウトインフレーターを取得する。
            mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            // リスト位置の商品データを取得する。
            GoodsData goodsData = (GoodsData)getItem(position);

            // リストに設定されるビューが無い場合
            if (null == convertView) {
                // 新しく生成する。
                convertView = mLayoutInflater.inflate(R.layout.goods_list_row, null);
            }

            // 各ウィジットを設定する。
            TextView goodsNameValue = (TextView)convertView.findViewById(R.id.goodsNameValue);
            TextView unitPriceValue = (TextView)convertView.findViewById(R.id.unitPriceValue);
            TextView shopNameValue  = (TextView)convertView.findViewById(R.id.shopNameValue);

            goodsNameValue.setText(goodsData.getName());
            PriceData priceData = goodsData.getLowestPriceData();
            if (null != priceData) {
                unitPriceValue.setText(String.valueOf(priceData.getUnitPrice()));
                shopNameValue.setText(priceData.getName());
            }

            return convertView;
        }
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
            // 商品追加ボタンの場合
            case R.id.addGoodsButton:
                // 商品編集画面を呼び出す。
                Intent intent = new Intent(GoodsListActivity.this, GoodsEditActivity.class);
                GoodsData goodsData = new GoodsData();
                goodsData.setCategoryId(  mCategoryData.getId());
                goodsData.setCategoryName(mCategoryData.getName());
                intent.putExtra(ExtraKey.GOODS_DATA, goodsData);
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
     * 商品リストアイテムクリックリスナークラス
     *
     */
    private class GoodsListOnItemClickListener implements OnItemClickListener {

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
            // 商品編集画面を呼び出す。
            Intent intent = new Intent(GoodsListActivity.this, GoodsEditActivity.class);
            intent.putExtra(ExtraKey.GOODS_DATA, mGoodsDataList.get(position));
            startActivity(intent);
        }
    }

    /**************************************************************************/
    /**
     * 商品リストアイテムロングクリックリスナークラス
     *
     */
    private class GoodsListOnItemLongClickListener implements OnItemLongClickListener {

        /** 商品アイテムの位置 */
        private int mPosition;

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
            // リストの位置を保存する。
            mPosition = position;

            // 商品削除確認ダイアログを表示する。
            AlertDialog.Builder builder = new AlertDialog.Builder(GoodsListActivity.this);
            builder.setTitle(R.string.delete_dialog_title);
            builder.setMessage(R.string.delete_dialog_message);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton(R.string.delete_dialog_positive_button, new ButtonOnClickListener());
            builder.setNegativeButton(R.string.delete_dialog_negative_button, null);
            builder.setCancelable(true);
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            return true;
        }

        /**
         * ボタンクリックリスナークラス
         */
        private class ButtonOnClickListener implements DialogInterface.OnClickListener {

            /**
             * ボタンがクリックされた時に呼び出される。
             *
             * @param dialog ダイアログ
             * @param which クリックされたボタン
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 商品データを取得する。
                GoodsData goodsData = mGoodsDataList.get(mPosition);

                // 操作リストを生成する。
                ArrayList<ContentProviderOperation> operationList =
                        new ArrayList<ContentProviderOperation>();
                ContentProviderOperation.Builder builder = null;

                // 商品テーブルのデータを削除する。
                {
                    builder = ContentProviderOperation.newDelete(LowestProvider.GOODS_CONTENT_URI);
                    String selection = GoodsTable.ID + " = ?";
                    String[] selectionArgs = {String.valueOf(goodsData.getId())};
                    builder.withSelection(selection, selectionArgs);
                    operationList.add(builder.build());
                }

                   // 商品データに関連する価格データを削除する。
                {
                    String selection = PriceTable.ID + " = ?";
                    for (PriceData priceData : goodsData.getPriceDataList()) {
                        // 価格テーブルのデータを削除する。
                        builder = ContentProviderOperation.newDelete(LowestProvider.PRICE_CONTENT_URI);
                        String[] selectionArgs = {String.valueOf(priceData.getId())};
                        builder.withSelection(selection, selectionArgs);
                        operationList.add(builder.build());
                    }
                }

                // バッチ処理を行う。
                try {
                    mResolver.applyBatch(LowestProvider.AUTHORITY, operationList);
                } catch (Exception e) {
                    toast("削除に失敗しました");
                    return;
                }

                // 商品リストアダプタを再設定する。
                mGoodsDataList.remove(mPosition);
                mGoodsListAdapter.clear();
                mGoodsListAdapter.addAll(mGoodsDataList);
                mGoodsListAdapter.notifyDataSetChanged();
            }
        }
    }
}
