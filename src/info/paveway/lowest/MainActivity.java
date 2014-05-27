package info.paveway.lowest;

import java.util.ArrayList;
import java.util.List;

import info.paveway.lowest.data.GoodsData;
import info.paveway.lowest.data.GoodsProvider;
import info.paveway.lowest.data.PriceData;
import info.paveway.lowest.data.PriceProvider;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
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
 * メイン画面
 *
 * @version 1.0 新規作成
 *
 */
public class MainActivity extends Activity {

	/** コンテントリゾルバ */
	private ContentResolver mResolver;

	/** 品物データリスト */
	private List<GoodsData> mGoodsDataList;

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

        // コンテントリゾルバを取得する。
        mResolver = getContentResolver();

        // 品物リストを取得する。
        mGoodsDataList = getGoodsDataList();

        // 各ウィジットを設定する。
        ((Button)findViewById(R.id.addButton)).setOnClickListener(new ButtonOnClickListener());

        // 品物リストビューを設定する。
        GoodsArrayAdapter goodsAdapter = new GoodsArrayAdapter(MainActivity.this, 0, mGoodsDataList);
        ListView goodsListView = (ListView)findViewById(R.id.goodsListView);
        goodsListView.setAdapter(goodsAdapter);
        goodsListView.setOnItemClickListener(new GoodsListOnItemClickListener());
        goodsListView.setOnItemLongClickListener(new GoodsListOnItemLongClickListener());
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
     * 品物データリストを取得する。
     *
     * @return 品物データリスト
     */
    private List<GoodsData> getGoodsDataList() {
    	List<GoodsData> goodsDataList = new ArrayList<GoodsData>();
    	// 品物データのカーソルを取得する。
        Cursor c = mResolver.query(GoodsProvider.CONTENT_URI, null, null, null, null);
        try {
        	// カーソルが取得できた場合
            if (null != c) {
            	// データがある場合
                if (c.moveToFirst()) {
                    do {
                    	// 品物データを生成し、データを設定する。
                        GoodsData goodsData = new GoodsData();
                        goodsData.setGoodsId(  c.getInt(   c.getColumnIndex(GoodsProvider.GOODS_ID)));
                        goodsData.setGoodsName(c.getString(c.getColumnIndex(GoodsProvider.GOODS_NAME)));
                        setPriceData(goodsData);

                        // 品物データリストに設定する。
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
     * @param goodsData 品物データ
     */
    private void setPriceData(GoodsData goodsData) {
    	String selection = PriceProvider.GOODS_ID + " = ?";
    	String[] selectionArgs = new String[]{String.valueOf(goodsData.getGoodsId())};
    	Cursor c = mResolver.query(PriceProvider.CONTENT_URI, null, selection, selectionArgs, null);
    	try {
    		// カーソルが取得できた場合
    		if (null != c) {
    			// データがある場合
    			if (c.moveToFirst()) {
    				do {
    					// 価格データを生成し、データを設定する。
    					PriceData priceData = new PriceData();
    					priceData.setPriceId( c.getInt(   c.getColumnIndex(PriceProvider.PRICE_ID)));
    					priceData.setGoodsId( c.getInt(   c.getColumnIndex(PriceProvider.GOODS_ID)));
    					priceData.setShopName(c.getString(c.getColumnIndex(PriceProvider.SHOP_NAME)));
    					priceData.setQuantity(c.getDouble(c.getColumnIndex(PriceProvider.QUANTITY)));
    					priceData.setPrice(   c.getLong(  c.getColumnIndex(PriceProvider.PRICE)));

    					// 品物データに価格データを追加する。
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
     * 品物配列アダプタークラス
     *
     */
    public class GoodsArrayAdapter extends ArrayAdapter<GoodsData> {

    	/** レイアウトインフレーター */
    	private LayoutInflater mLayoutInflater;

    	/**
    	 * コンストラクタ
    	 *
    	 * @param context コンテキスト
    	 * @param textViewResourceId テキストビューリソースID
    	 * @param objects 品物データリスト
    	 */
		public GoodsArrayAdapter(Context context, int textViewResourceId, List<GoodsData> objects) {
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
			// リスト位置の品物データを取得する。
			GoodsData goodsData = (GoodsData)getItem(position);

			// リストに設定されるビューが無い場合
			if (null == convertView) {
				// 新しく生成する。
				convertView = mLayoutInflater.inflate(R.layout.price_list_row, null);
			}

			// 各ウィジットを設定する。
			TextView goodsNameValue = (TextView)convertView.findViewById(R.id.goodsNameValue);
			TextView unitPriceValue = (TextView)convertView.findViewById(R.id.unitPriceValue);
			TextView shopNameValue  = (TextView)convertView.findViewById(R.id.shopNameValue);

			String goodsName    = goodsData.getGoodsName();
			PriceData priceData = goodsData.getLowestPriceData();
			String unitPrice    = String.valueOf(priceData.getUnitPrice());
			String shopName     = priceData.getShopName();

			goodsNameValue.setText(goodsName);
			unitPriceValue.setText(unitPrice);
			shopNameValue.setText(shopName);

			return convertView;
		}
    }

    /**************************************************************************/
    /**
     * 品物リストアイテムクリックリスナークラス
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
			Intent intent = new Intent(MainActivity.this, GoodsDetailActivity.class);
			intent.putExtra("goodsData", mGoodsDataList.get(position));
			startActivity(intent);
		}
    }

    /**************************************************************************/
    /**
     * 品物リストアイテムロングクリックリスナークラス
     *
     */
    private class GoodsListOnItemLongClickListener implements OnItemLongClickListener {

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
			return false;
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
			// 追加ボタンの場合
			case R.id.addButton:
				Intent intent = new Intent(MainActivity.this, GoodsEditActivity.class);
				startActivity(intent);
				break;

			default:
				break;
			}
		}
	}
}
