package info.paveway.lowest;

import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.CommonConstants.RequestCode;
import info.paveway.lowest.data.CategoryData;
import info.paveway.lowest.data.GoodsData;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.GoodsTable;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.PriceData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 最低価格
 * 商品編集画面
 *
 * @version 1.0 新規作成
 *
 */
public class GoodsEditActivity extends AbstractBaseActivity {

    /** 商品データ */
    private GoodsData mGoodsData;

    /** カテゴリ名 */
    private TextView mCategoryNameValue;

    /** 商品名 */
    private EditText mGoodsNameValue;

    /** メモ */
    private EditText mMemoValue;

    /** 価格追加ボタン */
    private Button mAddPriceButton;

    /** 価格データリスト */
    private List<PriceData> mPriceDataList;

    /** 価格リストアダプタ */
    private PriceListAdapter mPriceListAdapter;

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
        setContentView(R.layout.activity_goods_edit);

        // インテントを取得する。
        Intent intent = getIntent();
        // インテントが取得できない場合
        if (null == intent) {
            toast(R.string.illeagal_transition);
            // 終了する。
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

        // 価格データリストを取得する。
        mPriceDataList = getPriceDataList();

        // 各ウィジットを設定する。
        ((Button)findViewById(R.id.registButton)      ).setOnClickListener(new ButtonOnClickListener());
        ((Button)findViewById(R.id.clearButton)       ).setOnClickListener(new ButtonOnClickListener());
        ((Button)findViewById(R.id.categoryNameButton)).setOnClickListener(new ButtonOnClickListener());
        mAddPriceButton = (Button)findViewById(R.id.addPriceButton);
        mAddPriceButton.setOnClickListener(new ButtonOnClickListener());

        mCategoryNameValue = (TextView)findViewById(R.id.categoryNameValue);
        mCategoryNameValue.setText(mGoodsData.getCategoryName());

        mGoodsNameValue = (EditText)findViewById(R.id.cateogryNameValue);
        mMemoValue      = (EditText)findViewById(R.id.memoValue);

        // 商品データ編集の場合
        if (StringUtil.isNotNullOrEmpty(mGoodsData.getName())) {
            // 各商品データを設定する。
            mGoodsNameValue.setText(mGoodsData.getName());
            mMemoValue.setText(mGoodsData.getMemo());

            // 追加ボタンを有効にする。
            mAddPriceButton.setEnabled(true);

        // 商品データ新規登録の場合
        } else {
            // 追加ボタンを無効にする。
            mAddPriceButton.setEnabled(false);
        }

        // 価格リストビューを設定する。
        ListView priceListView = (ListView)findViewById(R.id.priceListView);
        priceListView.setOnItemClickListener(    new PriceListOnItemClickListener());
        priceListView.setOnItemLongClickListener(new PriceListOnItemLongClickListener());

        // 価格リストを設定する。
        mPriceListAdapter =  new PriceListAdapter(GoodsEditActivity.this, 0, mPriceDataList);
        priceListView.setAdapter(mPriceListAdapter);
    }

    /**
     * リスタートしたときに呼び出される。
     */
    @Override
    protected void onRestart() {
        // スーパークラスのメソッドを呼び出す。
        super.onRestart();

        // 価格リストアダプタをクリアする。
        mPriceListAdapter.clear();

        // 価格データリストを再取得する。
        mPriceDataList = getPriceDataList();

        // 価格リストアダプタに価格データリストを再設定する。
        mPriceListAdapter.addAll(mPriceDataList);

        // 価格リストアダプタを更新する。
        mPriceListAdapter.notifyDataSetChanged();
    }

    /**
     * 他の画面の呼び出しから戻った時に呼び出される。
     *
     * @param requestCode 要求コード
     * @param resultCode 結果コード
     * @param data データ
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((RequestCode.CATEGORY_LIST == requestCode) && (RESULT_OK == resultCode)) {
            CategoryData categoryData = (CategoryData)data.getSerializableExtra(ExtraKey.CATEGORY_DATA);
            if (null != categoryData) {
                mGoodsData.setCategoryId(categoryData.getId());
                String categoryName = categoryData.getName();
                mGoodsData.setCategoryName(categoryName);
                mCategoryNameValue.setText(categoryName);
            }
        }
    }

    /**
     * 価格データリストを取得する。
     *
     * @return 価格データリスト
     */
    private List<PriceData> getPriceDataList() {
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

        return priceDataList;
    }

    /**************************************************************************/
    /**
     * 価格リストアダプタークラス
     *
     */
    public class PriceListAdapter extends ArrayAdapter<PriceData> {

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
            // 価格データを取得する。
            PriceData priceData = (PriceData)getItem(position);

            // 対象のビューがない場合
            if (null == convertView) {
                // ビューを生成する。
                convertView = mLayoutInflater.inflate(R.layout.price_list_row, null);
            }

            // 各ビューを設定する。
            TextView shopNameValue  = (TextView)convertView.findViewById(R.id.shopNameValue);
            TextView quantityValue  = (TextView)convertView.findViewById(R.id.quantityValue);
            TextView priceValue     = (TextView)convertView.findViewById(R.id.priceValue);
            TextView unitPriceValue = (TextView)convertView.findViewById(R.id.unitPriceValue);

            shopNameValue.setText(                priceData.getShopName());
            quantityValue.setText( String.valueOf(priceData.getQuantity()));
            priceValue.setText(    String.valueOf(priceData.getPrice()));
            unitPriceValue.setText(String.valueOf(priceData.getUnitPrice()));

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
            // 登録ボタンの場合
            case R.id.registButton:
                // 入力されたデータを取得する。
                String goodsName = mGoodsNameValue.getText().toString();
                String memo      = mMemoValue.getText().toString();

                // 商品名が未入力の場合
                if (StringUtil.isNullOrEmpty(goodsName)) {
                    toast("商品名が未入力です");
                    return;
                }

                // 更新日時を取得する。
                long updateTime = new Date().getTime();

                ContentValues values = new ContentValues();
                values.put(GoodsTable.NAME,          goodsName);
                values.put(GoodsTable.CATEGORY_ID,   mGoodsData.getCategoryId());
                values.put(GoodsTable.CATEGORY_NAME, mGoodsData.getCategoryName());
                values.put(GoodsTable.MEMO,          memo);
                values.put(GoodsTable.UPDATE_TIME,   updateTime);
                long goodsId = mGoodsData.getId();

                // 未登録の場合
                if (0 == goodsId) {
                    Uri result = mResolver.insert(LowestProvider.GOODS_CONTENT_URI, values);

                    // 登録に失敗した場合
                    if (null == result) {
                        toast("商品データの登録に失敗しました");

                    // 登録に成功した場合
                    } else {
                        // 商品データを設定する。
                        mGoodsData.setId(ContentUris.parseId(result));
                        mGoodsData.setName(goodsName);
                        mGoodsData.setMemo(memo);
                        mGoodsData.setUpdateTime(updateTime);

                        // 追加ボタンを有効にする。
                        mAddPriceButton.setEnabled(true);
                    }

                // 登録済みの場合
                } else {
                    String selection = GoodsTable.ID + " = ?";
                    String[] selectionArgs = {String.valueOf(goodsId)};
                    int result = mResolver.update(LowestProvider.GOODS_CONTENT_URI, values, selection, selectionArgs);

                    // 更新に失敗した場合
                    if (1 != result) {
                        toast("商品データの登録に失敗しました");

                    // 更新に成功した場合
                    } else {
                        mGoodsData.setName(goodsName);
                        mGoodsData.setUpdateTime(updateTime);

                        // 追加ボタンを有効にする。
                        mAddPriceButton.setEnabled(true);
                    }
                }

                break;

            // クリアボタンの場合
            case R.id.clearButton:
                mGoodsNameValue.setText("");
                mMemoValue.setText("");
                break;

            // カテゴリ名ボタンの場合
            case R.id.categoryNameButton: {
                Intent intent = new Intent(GoodsEditActivity.this, CategoryListActivity.class);
                startActivityForResult(intent, RequestCode.CATEGORY_LIST);
                break;
            }

            // 価格追加ボタンの場合
            case R.id.addPriceButton: {
                Intent intent = new Intent(GoodsEditActivity.this, PriceEditActivity.class);
                PriceData priceData = new PriceData();
                priceData.setCategoryId(mGoodsData.getCategoryId());
                priceData.setCategoryName(mGoodsData.getCategoryName());
                priceData.setGoodsId(mGoodsData.getId());
                priceData.setGoodsName(mGoodsData.getName());
                intent.putExtra(ExtraKey.PRICE_DATA, priceData);
                startActivity(intent);
                break;
            }

            default:
                break;
            }
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
            Intent intent = new Intent(GoodsEditActivity.this, PriceEditActivity.class);
            intent.putExtra(ExtraKey.PRICE_DATA, mPriceDataList.get(position));
            startActivity(intent);
        }
    }

    /**************************************************************************/
    /**
     * 価格リストアイテムロングクリックリスナークラス
     *
     */
    private class PriceListOnItemLongClickListener implements OnItemLongClickListener {

        /** 価格アイテムの位置 */
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
            mPosition = position;

            // 価格アイテム削除確認ダイアログを表示する。
            AlertDialog.Builder builder = new AlertDialog.Builder(GoodsEditActivity.this);
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
                // 価格データを取得する。
                PriceData priceData = mPriceDataList.get(mPosition);

                String selection = PriceTable.ID + " = ?";
                String[] selectionArgs = {String.valueOf(priceData.getId())};
                int result = mResolver.delete(LowestProvider.PRICE_CONTENT_URI, selection, selectionArgs);

                // 削除に失敗した場合
                if (1 != result) {
                    toast(R.string.pricedata_delete_error);

                } else {
                    // リストビューの内容を更新する。
                    mPriceDataList.remove(mPosition);
                    mPriceListAdapter.clear();
                    mPriceListAdapter.addAll(mPriceDataList);
                    mPriceListAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
