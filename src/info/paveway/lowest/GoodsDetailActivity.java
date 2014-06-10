package info.paveway.lowest;

import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.GoodsData;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.PriceData;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
 * 品物詳細画面
 *
 * @version 1.0 新規作成
 *
 */
public class GoodsDetailActivity extends AbstractBaseActivity {

    /** 品物データ */
    private GoodsData mGoodsData;

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
        setContentView(R.layout.activity_goods_detail);

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
        mGoodsData = (GoodsData)intent.getSerializableExtra(ExtraKey.GOODS_DATA);
        // 引継ぎデータが取得できない場合
        if (null == mGoodsData) {
            // 終了する。
            toast(R.string.illeagal_transition);
            finish();
            return;
        }

        // 各ウィジットを設定する。
        ((Button)findViewById(R.id.editButton)).setOnClickListener(new ButtonOnClickListener());
        ((Button)findViewById(R.id.deleteButton)).setOnClickListener(new ButtonOnClickListener());

        ((TextView)findViewById(R.id.categoryNameValue)).setText(mGoodsData.getCategoryName());
        ((TextView)findViewById(R.id.cateogryNameValue   )).setText(mGoodsData.getName());
        ((TextView)findViewById(R.id.memoValue        )).setText(mGoodsData.getMemo());

        // 価格リストビューを設定する。
        ListView priceListView = (ListView)findViewById(R.id.priceListView);
        priceListView.setAdapter(
                new PriceArrayAdapter(GoodsDetailActivity.this, 0, mGoodsData.getPriceDataList()));
        priceListView.setOnItemClickListener(new PriceListOnItemClickListener());
        priceListView.setOnItemLongClickListener(new PriceListOnItemLongClickListener());

    }

    /**************************************************************************/
    /**
     * 価格配列アダプタークラス
     *
     */
    public class PriceArrayAdapter extends ArrayAdapter<PriceData> {

        /** レイアウトインフレーター */
        private LayoutInflater mLayoutInflater;

        /**
         * コンストラクタ
         *
         * @param context コンテキスト
         * @param textViewResourceId テキストビューリソースID
         * @param objects 品物データリスト
         */
        public PriceArrayAdapter(Context context, int textViewResourceId, List<PriceData> objects) {
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
            PriceData priceData = (PriceData)getItem(position);

            if (null == convertView) {
                convertView = mLayoutInflater.inflate(R.layout.price_list_row, null);
            }

            TextView shopNameValue  = (TextView)convertView.findViewById(R.id.shopNameValue);
            TextView quantityValue  = (TextView)convertView.findViewById(R.id.quantityValue);
            TextView priceValue      = (TextView)convertView.findViewById(R.id.priceValue);
            TextView unitPriceValue = (TextView)convertView.findViewById(R.id.unitPriceValue);

            shopNameValue.setText(                priceData.getName());
            quantityValue.setText( String.valueOf(priceData.getQuantity()));
            priceValue.setText(    String.valueOf(priceData.getPrice()));
            unitPriceValue.setText(String.valueOf(priceData.getUnitPrice()));

            return convertView;
        }
    }

    /**************************************************************************/
    /**
     * 価格リストアイテムクリックリスナークラス
     *
     */
    private class PriceListOnItemClickListener implements OnItemClickListener {

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
            // 価格編集画面に遷移する。
            PriceData priceData = mGoodsData.getPriceDataList().get(position);
            Intent intent = new Intent(GoodsDetailActivity.this, PriceEditActivity.class);
            intent.putExtra(ExtraKey.PRICE_DATA, priceData);
            startActivity(intent);
        }
    }

    /**************************************************************************/
    /**
     * 価格リストアイテムロングクリックリスナークラス
     *
     */
    private class PriceListOnItemLongClickListener implements OnItemLongClickListener {

        // リストの位置を保存する。
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

            // 価格削除確認ダイアログを表示する。
            AlertDialog.Builder builder = new AlertDialog.Builder(GoodsDetailActivity.this);
            builder.setTitle("価格削除確認");
            builder.setMessage("この価格のデータを削除しますか");
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton("削除", new ButtonOnClickListener());
            builder.setNegativeButton("キャンセル", null);
            builder.setCancelable(true);
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
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
                // 価格データを取得する。
                PriceData priceData = mGoodsData.getPriceDataList().get(mPosition);

                // 価格データを削除する。
                String selection = PriceTable.ID + " = ?";
                String[] selectionArgs = {String.valueOf(priceData.getId())};
                int result = mResolver.delete(LowestProvider.PRICE_CONTENT_URI, selection, selectionArgs);
                // エラーの場合
                if (1 != result) {
                    toast("価格データが削除できませんでした");
                }
            }
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
            // 編集ボタンの場合
            case R.id.editButton:
                // 商品編集画面に遷移する。
                Intent intent = new Intent(GoodsDetailActivity.this, GoodsEditActivity.class);
                intent.putExtra(ExtraKey.GOODS_DATA, mGoodsData);
                startActivity(intent);
                break;

            // 削除ボタンの場合
            case R.id.deleteButton:
                break;

            // 上記以外
            default:
                // 何もしない。
                break;
            }
        }
    }
}
