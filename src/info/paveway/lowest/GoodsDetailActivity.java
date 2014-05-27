package info.paveway.lowest;

import java.util.List;

import info.paveway.lowest.data.GoodsData;
import info.paveway.lowest.data.PriceData;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
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
public class GoodsDetailActivity extends Activity {

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
			finish();
			return;
		}

		// 引継ぎデータを取得する。
		mGoodsData = intent.getParcelableExtra("goodsData");
		// 引継ぎデータが取得できない場合
		if (null == mGoodsData) {
			// 終了する。
			finish();
			return;
		}

		// 各ウィジットを設定する。
		((Button)findViewById(R.id.editButton)).setOnClickListener(new ButtonOnClickListener());
		((Button)findViewById(R.id.deleteButton)).setOnClickListener(new ButtonOnClickListener());
		((TextView)findViewById(R.id.goodsNameValue)).setText(mGoodsData.getGoodsName());

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

			shopNameValue.setText(                priceData.getShopName());
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

		}
    }

    /**************************************************************************/
    /**
     * 価格リストアイテムロングクリックリスナークラス
     *
     */
    private class PriceListOnItemLongClickListener implements OnItemLongClickListener {

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
			// 編集ボタンの場合
			case R.id.editButton:
				Intent intent = new Intent(GoodsDetailActivity.this, GoodsEditActivity.class);
				intent.putExtra("goodsData", mGoodsData);
				startActivity(intent);
				break;

			// 削除ボタンの場合
			case R.id.deleteButton:
				break;

			default:
				break;
			}
		}
	}
}
