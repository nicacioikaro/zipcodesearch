package net.minddevelopment.zipcodesearch.shared;

import org.springframework.stereotype.Component;

@Component
public class ZipcodeHelper {
    public String normalizeZipcode(String zipcode) {
        String clean = zipcode.replaceAll("\\D", "");
        return clean.substring(0, 5) + "-" + clean.substring(5);
    }
}
