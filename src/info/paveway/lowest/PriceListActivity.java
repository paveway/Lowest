package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.GoodsData;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.PriceData;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 最低価格記録アプリ
 * 価格リスト画面クラス
 *
 * @version 1.0 新規作成
 */
public class PriceListActivity extends AbstractBaseActivity implements OnUpdateListener {

    /** ロガー */
    private Logger mLogger = new Logger(PriceListActivity.class);

    /** 商品データ */
    private GoodsData mGoodsData;

    /** 価格データリスト */
    private List<PriceData> mPriceDataList;

    /** 価格リストアダプタ */
    private PriceListAdapter mPriceListAdapter;

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
        setContentView(R.layout.activity_price_list);

        // インテントを取得する。
        Intent intent = getIntent();
        // インテントが取得できない場合
        if (null == intent) {
            // 終了する。
            toast(R.string.illeagal_transition);
            finish();
            mLogger.w("OUT(NG)");
            return;
        }

        // 商品データを取得する。
        mGoodsData = (GoodsData)intent.getSerializableExtra(ExtraKey.GOODS_DATA);
        // 商品データが取得できない場合
        if (null == mGoodsData) {
            // 終了する。
            toast(R.string.illeagal_transition);
            finish();
            mLogger.w("OUT(NG)");
            return;
        }

        // 商品名表示を設定する。
        ((TextView)findViewById(R.id.goodsNameValue)).setText(mGoodsData.getName());

        // 価格追加ボタンにリスナーを設定する。
        ((Button)findViewById(R.id.addPriceButton)).setOnClickListener(new ButtonOnClickListener());

        // 価格データリストを取得する。
        mPriceDataList = getPriceDataList();

        // 価格リストビューを設定する。
        ListView priceListView = (ListView)findViewById(R.id.priceListView);
        priceListView.setOnItemClickListener(    new PriceListOnItemClickListener());
//        priceListView.setOnItemLongClickListener(new PriceListOnItemLongClickListener());

        // 価格リストを設定する。
        mPriceListAdapter =  new PriceListAdapter(PriceListActivity.this, 0, mPriceDataList);
        priceListView.setAdapter(mPriceListAdapter);
    }

    /**
     * 価格データリストを取得する。
     *
     * @return 価格データリスト
     */
    private List<PriceData> getPriceDataList() {
        mLogger.d("IN");

        List<PriceData> priceDataList = new ArrayList<PriceData>();
        long goodsId = mGoodsData.getId();
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
                        priceData.setCategoryId(  mGoodsData.getCategoryId());
                        priceData.setCategoryName(mGoodsData.getCategoryName());
                        priceData.setGoodsId(     goodsId);
                        priceData.setGoodsName(   mGoodsData.getName());
                        priceData.setShopId(      c.getLong(  c.getColumnIndex(PriceTable.SHOP_ID)));
                        priceData.setShopName(    c.getString(c.getColumnIndex(PriceTable.SHOP_NAME)));
                        priceData.setQuantity(    c.getDouble(c.getColumnIndex(PriceTable.QUANTITY)));
                        priceData.setPrice(       c.getLong(  c.getColumnIndex(PriceTable.PRICE)));
                        priceData.setUpdateTime(  c.getLong(  c.getColumnIndex(PriceTable.UPDATE_TIME)));

                        priceDataList.add(priceData);
                    } while (c.moveToNext());
                }
            }
        } finally {
            if (null != c) {
                c.close();
            }
        }

        mLogger.d("OUT(OK)");
        return priceDataList;
    }

    /**
     * 更新された時に呼び出される。
     */
    @Override
    public void onUpdate() {
        mLogger.d("IN");

        // 価格データリストを再設定する。
        mPriceDataList = getPriceDataList();
        mPriceListAdapter.clear();
        mPriceListAdapter.addAll(mPriceDataList);
        mPriceListAdapter.notifyDataSetChanged();

        mLogger.d("OUT(OK)");
    }

    /**************************************************************************/
    /**
     * 価格リストアダプタークラス
     *
     */
    public class PriceListAdapter extends ArrayAdapter<PriceData> {

        /** ロガー */
        private Logger mLogger = new Logger(PriceListAdapter.class);

        /** レイアウトインフレーター */
        private LayoutInflater mLayoutInflater;

        /**
         * コンストラクタ
         *
         * @param context コンテキスト
         * @param textViewResourceId テキストビューリソースID
         * @param objects 商品データリスト
         */
        public PriceListAdapter(Context context, int textViewResourceId, List<PriceData> objects) {
            // スーパークラスのコンストラクタを呼び出す。
            super(context, textViewResourceId, objects);

            // レイアウトインフレーターを取得する。
            mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * ビューを返却する。
         *
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            mLogger.d("IN");

            // 価格データを取得する。
            PriceData priceData = (PriceData)getItem(position);

            // 対象のビューがない場合
            if (null == convertView) {
                // ビューを生成する。
                convertView = mLayoutInflater.inflate(R.layout.row_price_list, null);
            }

            // 各ビューを設定する。
            TextView shopNameValue  = (TextView)convertView.findViewById(R.id.shopNameValue);
            TextView quantityValue  = (TextView)convertView.findViewById(R.id.quantityValue);
            TextView priceValue     = (TextView)convertView.findViewById(R.id.priceValue);
            TextView unitPriceValue = (TextView)convertView.findViewById(R.id.unitPriceValue);

            shopNameValue.setText(                priceData.getShopName());
            quantityValue.setText( String.format("%.2f", priceData.getQuantity()));
            priceValue.setText(    NumberFormat.getCurrencyInstance().format(priceData.getPrice()));
            unitPriceValue.setText(String.format("@%.2f", priceData.getUnitPrice()));

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
            // 価格追加ボタンの場合
            case R.id.addPriceButton:
                mLogger.d("addPriceButton");

                // 価格編集ダイアログを表示する。
                PriceData priceData = new PriceData();
                priceData.setGoodsId(mGoodsData.getId());
                priceData.setGoodsName(mGoodsData.getName());

                FragmentManager manager = getSupportFragmentManager();
                PriceEditDialog priceEditDialog = PriceEditDialog.newInstance(priceData);
                priceEditDialog.show(manager, PriceEditDialog.class.getSimpleName());

                break;
            }

            mLogger.d("OUT(OK)");
        }
    }

    /**************************************************************************/
    /**
     * 価格リストアイテムクリックリスナークラス
     *
     */
    private class PriceListOnItemClickListener implements OnItemClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(PriceListOnItemClickListener.class);

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

            // 価格詳細ダイアログを表示する。
            FragmentManager manager = getSupportFragmentManager();
            PriceDetailDialog priceDetailDialog = PriceDetailDialog.newInstance(mPriceDataList.get(position));
            priceDetailDialog.show(manager, PriceDetailDialog.class.getSimpleName());

            mLogger.d("OUT(OK)");
        }
    }

//    /**************************************************************************/
//    /**
//     * 価格リストアイテムロングクリックリスナークラス
//     *
//     */
//    private class PriceListOnItemLongClickListener implements OnItemLongClickListener {
//
//        /** 価格アイテムの位置 */
//        private int mPosition;
//
//        /**
//         * リストアイテムがロングクリックされた時に呼び出される。
//         *
//         * @param parent 親のビュー
//         * @param view 対象のビュー
//         * @param position リストの位置
//         * @param id 対象のビューのID
//         * @return 処理結果
//         */
//        @Override
//        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//            mPosition = position;
//
//            // 価格アイテム削除確認ダイアログを表示する。
//            AlertDialog.Builder builder = new AlertDialog.Builder(PriceListActivity.this);
//            builder.setTitle(R.string.delete_dialog_title);
//            builder.setMessage(R.string.delete_dialog_message);
//            builder.setIcon(android.R.drawable.ic_dialog_alert);
//            builder.setPositiveButton(R.string.delete_dialog_positive_button, new DialogButtonOnClickListener());
//            builder.setNegativeButton(R.string.delete_dialog_negative_button, null);
//            builder.setCancelable(true);
//            AlertDialog dialog = builder.create();
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.show();
//            return true;
//        }
//
//        /**
//         * ダイアログボタンクリックリスナークラス
//         */
//        private class DialogButtonOnClickListener implements DialogInterface.OnClickListener {
//
//            /**
//             * ボタンがクリックされた時に呼び出される。
//             *
//             * @param dialog ダイアログ
//             * @param which クリックされたボタン
//             */
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // 価格データを取得する。
//                PriceData priceData = mPriceDataList.get(mPosition);
//
//                String selection = PriceTable.ID + " = ?";
//                String[] selectionArgs = {String.valueOf(priceData.getId())};
//                int result = mResolver.delete(LowestProvider.PRICE_CONTENT_URI, selection, selectionArgs);
//
//                // 削除に失敗した場合
//                if (1 != result) {
//                    toast(R.string.pricedata_delete_error);
//
//                } else {
//                    // リストビューの内容を更新する。
//                    mPriceDataList.remove(mPosition);
//                    mPriceListAdapter.clear();
//                    mPriceListAdapter.addAll(mPriceDataList);
//                    mPriceListAdapter.notifyDataSetChanged();
//                }
//            }
//        }
//    }
}
