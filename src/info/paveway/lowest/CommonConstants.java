package info.paveway.lowest;

/**
 * 最低価格
 * 共通定数クラス
 *
 * @version 1.0 新規作成
 */
public class CommonConstants {

    /** 要求コード */
    public class RequestCode {
        /** 商品リスト画面 */
        public static final int GOODS_LIST = 2;

        /** 商品編集画面 */
        public static final int GOODS_EDIT = 3;

        /** カテゴリリスト画面 */
        public static final int CATEGORY_LIST = 4;

        /** 価格編集画面 */
        public static final int PRICE_EDIT = 5;

        /** 店リスト画面 */
        public static final int SHOP_LIST = 6;

        /** カテゴリ編集画面 */
        public static final int CATEGORY_EDIT = 7;

        /** 店編集画面 */
        public static final int SHOP_EDIT = 8;
    }

    /** Extraキー */
    public class ExtraKey {
        /** カテゴリデータ */
        public static final String CATEGORY_DATA = "categoryData";

        /** 商品データ */
        public static final String GOODS_DATA = "goodsData";

        /** 店データ */
        public static final String SPOP_DATA = "shopData";

        /** 価格データ */
        public static final String PRICE_DATA = "priceData";
    }
}
