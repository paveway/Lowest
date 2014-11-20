package info.paveway.lowest;

/**
 * 最低価格記録アプリ
 * 共通定数クラス
 *
 * @version 1.0 新規作成
 *
 */
public class CommonConstants {

    /** 要求コード */
    public class RequestCode {
        /** メイン */
        public static final int MAIN = 1;

        /** 価格リスト */
        public static final int PRICE_LIST = 2;

        /** カテゴリリスト */
        public static final int CATEGORY_LIST = 3;

        /** 店リスト */
        public static final int SHOP_LIST = 4;
    }

    /** Extraキー */
    public class ExtraKey {
        /** カテゴリデータ */
        public static final String CATEGORY_DATA = "categoryData";

        /** 価格データ */
        public static final String PRICE_DATA = "priceData";

        /** 商品データ */
        public static final String GOODS_DATA = "goodsData";

        /** 店データ */
        public static final String SHOP_DATA = "shopData";
    }
}
