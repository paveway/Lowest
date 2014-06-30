package info.paveway.lowest.data;

/**
 * 最低価格記録アプリ
 * 支店データ保持クラス
 *
 * @version 1.0 新規作成
 *
 */
public class BranchData extends AbstractBaseData {

    /** 店ID */
    private long mShopId;

    /** 店名 */
    private String mShopName;

    /**
     * コンストラクタ
     */
    public BranchData() {
        super();
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
}
