package dto;

/**
 * خروجی‌ای که به‌جای message، به‌صورت generic یک data برمی‌گرداند.
 * ارث‌بری از ServiceResult باعث می‌شود متدهای فعلی که با ServiceResult کار می‌کنند 
 * دچار تغییر نشوند.
 */
public class DataResult<T> extends ServiceResult {
    private final T data;

    public DataResult(int status, T data) {
        // پیام را null می‌کنیم چون از data استفاده می‌کنیم
        super(status, (String) null);
        this.data = data;
    }

    public T getData() {
        return data;
    }
}