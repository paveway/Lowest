package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.LowestProvider.ShopTable;
import info.paveway.lowest.data.ShopData;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
 * 店リスト画面
 *
 * @version 1.0 新規作成
 *
 */
public class ShopListActivity extends AbstractBaseActivity {

    /** 店データリスト */
    private List<ShopData> mShopDataList;

    /** 店リストアダプタ */
    private ArrayAdapter<String> mShopListAdapter;

    /**
     * 生成された時に呼び出される。
     *
     * @param savedInstanceState 保存された時のインスタンスの状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // スーパークラスのメソッド
        super.onCreate(savedInstanceState);

        // レイアウトを設定する。
        setContentView(R.layout.activity_shop_list);

        // コンテントリゾルバを取得する。
        mResolver = getContentResolver();

        ((Button)findViewById(R.id.addShopButton)).setOnClickListener(new ButtonOnClickListener());

        // 店データリストを生成する。
        mShopDataList = new ArrayList<ShopData>();
        List<String> shopNameList = getShopNameList();

        // 店リストアダプタを生成する。
        mShopListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        for (String shopName : shopNameList) {
            mShopListAdapter.add(shopName);
        }

        // 店リストビューを生成する。
        ListView shopListView = (ListView)findViewById(R.id.shopListView);
        shopListView.setAdapter(mShopListAdapter);
        shopListView.setOnItemClickListener(new ShopListOnItemClickListener());
        shopListView.setOnItemLongClickListener(new ShopListOnItemLongClickListener());
    }

    /**
     * リスタートしたときに呼び出される。
     */
    @Override
    protected void onRestart() {
        // スーパークラスのメソッドを呼び出す。
        super.onRestart();

        // 店リストアダプタをクリアする。
        mShopListAdapter.clear();

        // 店名リストを取得する。
        List<String> shopNameList = getShopNameList();

        // 店リストアダプタに店名を再設定する。
        for (String shopName : shopNameList) {
            mShopListAdapter.add(shopName);
        }

        // 店リストアダプタを更新する。
        mShopListAdapter.notifyDataSetChanged();
    }

    /**
     * 店名リストを取得する。
     *
     * @return 店名リスト
     */
    private List<String> getShopNameList() {
        List<String> shopNameList = new ArrayList<String>();
        // 店データのカーソルを取得する。
        Cursor c = mResolver.query(LowestProvider.SHOP_CONTENT_URI, null, null, null, null);
        try {
            // カーソルが取得できた場合
            if (null != c) {
                // データがある場合
                if (c.moveToFirst()) {
                    do {
                        // 店データを生成し、店データリストに追加する。
                        ShopData shopData = new ShopData();
                        shopData.setId(c.getInt(c.getColumnIndex(ShopTable.ID)));
                        String shopName = c.getString(c.getColumnIndex(ShopTable.NAME));
                        shopData.setName(shopName);
                        mShopDataList.add(shopData);

                        // 店名データリストに設定する。
                        shopNameList.add(shopName);
                    } while (c.moveToNext());
                }
            }
        } finally {
            if (null != c) {
                c.close();
            }
        }
        return shopNameList;
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
            // 店追加ボタンの場合
            case R.id.addShopButton:
                // 店編集画面を呼び出す。
                Intent intent = new Intent(ShopListActivity.this, ShopEditActivity.class);
                ShopData shopData = new ShopData();
                shopData.setId(-1);
                intent.putExtra(ExtraKey.SHOP_DATA, shopData);
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
     * 店リストアイテムクリックリスナークラス
     *
     */
    private class ShopListOnItemClickListener implements OnItemClickListener {

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
            Intent intent = new Intent();
            intent.putExtra(ExtraKey.SHOP_DATA, mShopDataList.get(position));
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    /**************************************************************************/
    /**
     * 店リストアイテムロングクリックリスナークラス
     *
     */
    private class ShopListOnItemLongClickListener implements OnItemLongClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(ShopListOnItemLongClickListener.class);

        /** 店リスト位置 */
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

            AlertDialog.Builder builder = new AlertDialog.Builder(ShopListActivity.this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("店名削除確認");
            builder.setMessage("店名を削除しますか");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 店データを削除する。
                    deleteShopData();
                }
            });
            builder.setNegativeButton("キャンセル", null);
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }

        /**
         * 店データを削除する。
         */
        private void deleteShopData() {
            ArrayList<ContentProviderOperation> operationList =
                    new ArrayList<ContentProviderOperation>();

            ShopData shopData = mShopDataList.get(mPosition);
            String shopId = String.valueOf(shopData.getId());

            // 店テーブルのデータを削除する。
            ContentProviderOperation.Builder builder =
                       ContentProviderOperation.newDelete(LowestProvider.SHOP_CONTENT_URI);
            String selection = ShopTable.ID + " = ?";
            String[] selectionArgs = {shopId};
            builder.withSelection(selection, selectionArgs);
            operationList.add(builder.build());

            // 価格テーブルのデータを削除する。
            builder = ContentProviderOperation.newDelete(LowestProvider.PRICE_CONTENT_URI);
            selection = PriceTable.SHOP_ID + " = ?";
            builder.withSelection(selection, selectionArgs);
            operationList.add(builder.build());

            // バッチ処理を行う。
            try {
                mResolver.applyBatch(LowestProvider.AUTHORITY, operationList);
            } catch (Exception e) {
                mLogger.e(e);
            }
        }
    }
}
