package info.paveway.lowest.data;


/**
 * 最低価格記録アプリ
 * 価格データ保持クラス
 *
 * @version 1.0 新規作成
 *
 */
public class PriceData extends AbstractBaseData {

    /** カテゴリID */
    private long mCategoryId;

    /** カテゴリ名 */
    private String mCategoryName;

    /** 商品ID */
    private long mGoodsId;

    /** 商品名 */
    private String mGoodsName;

    /** 店ID */
    private long mShopId;

    /** 店名 */
    private String mShopName;

    /** 数量 */
    private double mQuantity;

    /** 価格 */
    private long mPrice;

    /** メモ */
    private String mMemo;

    /**
     * コンストラクタ
     */
    public PriceData() {
        super();
    }

    /**
     * カテゴリIDを設定する。
     *
     * @param categoryId カテゴリID
     */
    public void setCategoryId(long categoryId) {
        mCategoryId = categoryId;
    }

    /**
     * カテゴリIDを返却する。
     *
     * @return カテゴリID
     */
    public long getCagetoryId() {
        return mCategoryId;
    }

    /**
     * カテゴリ名を設定する。
     *
     * @param categoryName カテゴリ名
     */
    public void setCategoryName(String categoryName) {
        mCategoryName = categoryName;
    }

    /**
     * カテゴリ名を返却する。
     *
     * @return カテゴリ名
     */
    public String getCategoryName() {
        return mCategoryName;
    }

    /**
     * 商品IDを設定する。
     *
     * @param goodsId 商品ID
     */
    public void setGoodsId(long goodsId) {
        mGoodsId = goodsId;
    }

    /**
     * 商品IDを返却する。
     *
     * @return 商品ID
     */
    public long getGoodsId() {
        return mGoodsId;
    }

    /**
     * 商品名を設定する。
     *
     * @param goodsName 商品名
     */
    public void setGoodsName(String goodsName) {
        mGoodsName = goodsName;
    }

    /**
     * 商品名を返却する。
     *
     * @return 商品名
     */
    public String getGoodsName() {
        return mGoodsName;
    }

    /**
     * 店IDを設定する。
     *
     * @param shopId 店ID
     */
    public void setShopId(long shopId) {
        mShopId = shopId;
    }

    /**
     * 店IDを返却する。
     *
     * @return 店ID
     */
    public long getShopId() {
        return mShopId;
    }

    /**
     * 店名を設定する。
     *
     * @param shopName 店名
     */
    public void setShopName(String shopName) {
        mShopName = shopName;
    }

    /**
     * 店名を返却する。
     *
     * @return 店名
     */
    public String getShopName() {
        return mShopName;
    }

    /**
     * 数量を設定する。
     *
     * @param quantity 数量
     */
    public void setQuantity(double quantity) {
        mQuantity = quantity;
    }

    /**
     * 数量を返却する。
     *
     * @return 数量
     */
    public double getQuantity() {
        return mQuantity;
    }

    /**
     * 価格を設定する。
     *
     * @param price 価格
     */
    public void setPrice(long price) {
        mPrice = price;
    }

    /**
     * 価格を返却する。
     *
     * @return 価格
     */
    public long getPrice() {
        return mPrice;
    }

    /**
     * 単価を返却する。
     *
     * @return 単価
     */
    public double getUnitPrice() {
        if (0 != mQuantity) {
            return mPrice / mQuantity;
        } else {
            return 0;
        }
    }

    /**
     * メモを設定する。
     *
     * @param memo メモ
     */
    public void setMemo(String memo) {
        mMemo = memo;
    }

    /**
     * メモを返却する。
     *
     * @return メモ
     */
    public String getMemo() {
        return mMemo;
    }
}
