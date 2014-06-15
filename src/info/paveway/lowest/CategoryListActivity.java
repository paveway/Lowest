package info.paveway.lowest;

import info.paveway.lowest.CommonConstants.ExtraKey;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

/**
 * 最低価格
 * カテゴリリスト画面
 *
 * @version 1.0 新規作成
 *
 */
public class CategoryListActivity extends MainActivity {

    /**
     * 生成された時に呼び出される。
     *
     * @param savedInstanceState 保存された時のインスタンスの状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // スーパークラスのメソッドを呼び出す。
        super.onCreate(savedInstanceState);
    }

    /**
     * リストアイテムがクリックされた時に呼び出される。
     *
     * @param parent 親のビュー
     * @param view 対象のビュー
     * @param position リストの位置
     * @param id 対象のビューのID
     */
    @Override
    protected void itemClick(AdapterView<?> parent, View view, int position, long id) {
        // 呼び出し元の商品編集画面に戻る。
        Intent intent = new Intent();
        intent.putExtra(ExtraKey.CATEGORY_DATA, mCategoryDataList.get(position));
        setResult(RESULT_OK, intent);
        finish();
    }
}
