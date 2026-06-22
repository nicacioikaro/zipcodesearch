package net.minddevelopment.zipcodesearch.shared.exception;

public class ZipcodeNotFound extends RuntimeException {
    private final String zipcode;

    public ZipcodeNotFound(String zipcode) {
        super(zipcode);
        this.zipcode = zipcode;
    }

    public String getZipcode() {
        return zipcode;
    }
}