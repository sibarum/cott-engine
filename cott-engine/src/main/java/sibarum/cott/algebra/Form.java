package sibarum.cott.algebra;

import sibarum.cott.calculator.Mode;
import sibarum.cott.calculator.Modeset;

/**
 * Which way a sum algebra ({@code C}, {@code D}, {@code S}) and a product algebra ({@code Q}, {@code P}) nest when
 * they meet: {@code Q(1, 2) + C(3, 4)} is {@code C(Q, Q)}, a sum of products, or {@code Q(C, C)}, a product of sums.
 * Both hold the same values, and pay for division in different places.
 */
public enum Form implements Mode {

    SUM_OF_PRODUCTS("sum-of-products", "Sum of products"),
    PRODUCT_OF_SUMS("product-of-sums", "Product of sums");

    private final String key;
    private final String label;

    Form(String key, String label) {
        this.key = key;
        this.label = label;
    }

    @Override
    public Modeset modeset() {
        return Modeset.FORM;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String label() {
        return label;
    }
}
