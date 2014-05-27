package info.paveway.lowest;

import android.app.Activity;
import android.os.Bundle;

/**
 * 最低価格
 * 価格編集画面
 *
 * @version 1.0 新規作成
 *
 */
public class PriceEditActivity extends Activity {

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
        setContentView(R.layout.activity_price_edit);
    }
}
